package io.roastedroot.proxywasm;

import static io.roastedroot.proxywasm.internal.Helpers.bytes;

import com.dylibso.chicory.runtime.ImportMemory;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Machine;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.internal.ProxyWasm;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Represents a Proxy-WASM plugin, providing the bridge between the host
 * environment and the WASM module.
 *
 * <p>Concrete Plugin instances are created using the {@link Builder}.
 * The actual WASM instance and interaction logic are managed internally.
 */
public interface Plugin {

    /**
     * Returns the configured name of this plugin instance.
     *
     * @return the plugin name, which might be null if not explicitly set via the builder.
     */
    String name();

    /**
     * Creates a new {@link Builder} to configure and construct a {@link Plugin} instance
     * from the given WASM module.
     *
     * @param module the compiled {@link WasmModule} representing the plugin's code.
     * @return a new {@link Plugin.Builder} instance.
     */
    static Plugin.Builder builder(WasmModule module) {
        return new Plugin.Builder(module);
    }

    /**
     * Builder for creating a Plugin instance.
     */
    final class Builder {

        private final WasmModule module;
        private final ProxyWasm.Builder proxyWasmBuilder = ProxyWasm.builder().withStart(false);
        private boolean shared;
        private String name;
        private HashMap<String, ForeignFunction> foreignFunctions;
        private HashMap<String, URI> upstreams;
        private boolean strictUpstreams;
        private int minTickPeriodMilliseconds;
        private LogHandler logger;
        private byte[] vmConfig;
        private byte[] pluginConfig;
        private MetricsHandler metricsHandler;
        private SharedQueueHandler sharedQueueHandler;
        private SharedDataHandler sharedDataHandler;

        /**
         * Private constructor for the Builder.
         * Initializes the builder with the essential WASM module.
         *
         * @param module The compiled {@link WasmModule} containing the plugin code.
         */
        private Builder(WasmModule module) {
            this.module = module;
        }

        /**
         * Sets the optional name for this plugin instance.
         * This name can be used for identification and logging purposes.
         *
         * @param name the desired name for the plugin.
         * @return this {@code Builder} instance for method chaining.
         */
        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Registers foreign (host-provided) functions that can be called by the WASM plugin.
         * These functions allow the plugin to interact with the host environment beyond the standard
         * Proxy-WASM ABI calls.
         *
         * @param functions A map where keys are the function names expected by the WASM module,
         *                  and values are {@link ForeignFunction} implementations provided by the host.
         * @return this {@code Builder} instance for method chaining.
         * @see ForeignFunction
         */
        public Builder withForeignFunctions(Map<String, ForeignFunction> functions) {
            this.foreignFunctions = new HashMap<>(functions);
            return this;
        }

        /**
         * Defines mappings from logical upstream names (used within the plugin) to actual network URIs.
         * This allows the plugin to make network calls (e.g., HTTP, gRPC) to services known by name,
         * without needing to hardcode addresses.
         *
         * @param upstreams A map where keys are the logical upstream names used by the plugin,
         *                  and values are the corresponding {@link URI}s of the target services.
         * @return this {@code Builder} instance for method chaining.
         */
        public Builder withUpstreams(Map<String, URI> upstreams) {
            this.upstreams = new HashMap<>(upstreams);
            return this;
        }

        /**
         * Configures the behavior when a plugin attempts to call an upstream that is not defined
         * in the `upstreams` map provided via {@link #withUpstreams(Map)}.
         *
         * <p>If {@code strictUpstreams} is {@code true}, attempting to use an undefined upstream name
         * will result in an error being reported back to the plugin.
         *
         * <p>If {@code strictUpstreams} is {@code false} (the default behavior if this method is not called),
         * the host will try to parse the upstream name as URI.
         *
         * @param strictUpstreams {@code true} to enforce that all used upstream names must be explicitly mapped,
         *                        {@code false} to allow fallback resolution.
         * @return this {@code Builder} instance for method chaining.
         */
        public Builder withStrictUpstreams(boolean strictUpstreams) {
            this.strictUpstreams = strictUpstreams;
            return this;
        }

        /**
         * Sets a minimum interval for the plugin's periodic timer ticks ({@code proxy_on_tick}).
         * The Proxy-WASM ABI allows plugins to request a timer tick period. This setting enforces
         * a lower bound on that period to prevent plugins from requesting excessively frequent ticks,
         * which could overload the host.
         *
         * <p>If the plugin requests a tick period shorter than this minimum, the host will use
         * this minimum value instead.
         *
         * @param minTickPeriodMilliseconds the minimum allowed tick period in milliseconds. A value of 0 or less
         *                                  implies no minimum enforcement (host default behavior).
         * @return this {@code Builder} instance for method chaining.
         */
        public Builder withMinTickPeriodMilliseconds(int minTickPeriodMilliseconds) {
            this.minTickPeriodMilliseconds = minTickPeriodMilliseconds;
            return this;
        }

        /**
         * Provides a {@link LogHandler} implementation for the plugin to use.
         * This handler receives log messages generated by the WASM module via the {@code proxy_log} ABI call.
         * If no logger is provided, {@link LogHandler#DEFAULT} (a no-op logger) is used.
         *
         * @param logger the {@link LogHandler} implementation to handle plugin logs.
         * @return this {@code Builder} instance for method chaining.
         * @see LogHandler
         */
        public Builder withLogger(LogHandler logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Provides a {@link MetricsHandler} implementation for the plugin to use.
         * This handler manages metric definition, recording, and retrieval requested by the WASM module
         * via the relevant {@code proxy_*} ABI calls (e.g., {@code proxy_define_metric}).
         * If no handler is provided, {@link MetricsHandler#DEFAULT} (which returns UNIMPLEMENTED)
         * might be used implicitly.
         *
         * @param metricsHandler the {@link MetricsHandler} implementation to manage plugin metrics.
         * @return this {@code Builder} instance for method chaining.
         * @see MetricsHandler
         */
        public Builder withMetricsHandler(MetricsHandler metricsHandler) {
            this.metricsHandler = metricsHandler;
            return this;
        }

        /**
         * Provides a {@link SharedQueueHandler} implementation for the plugin to use.
         * This handler manages operations on shared message queues requested by the WASM module
         * via the relevant {@code proxy_*} ABI calls (e.g., {@code proxy_register_shared_queue}).
         * If no handler is provided, {@link SharedQueueHandler#DEFAULT} (which returns UNIMPLEMENTED)
         * might be used implicitly.
         *
         * @param sharedQueueHandler the {@link SharedQueueHandler} implementation to manage shared queues.
         * @return this {@code Builder} instance for method chaining.
         * @see SharedQueueHandler
         */
        public Builder withSharedQueueHandler(SharedQueueHandler sharedQueueHandler) {
            this.sharedQueueHandler = sharedQueueHandler;
            return this;
        }

        /**
         * Provides a {@link SharedDataHandler} implementation for the plugin to use.
         * This handler manages operations on shared key-value data requested by the WASM module
         * via the relevant {@code proxy_*} ABI calls (e.g., {@code proxy_get_shared_data}).
         * If no handler is provided, {@link SharedDataHandler#DEFAULT} (which returns UNIMPLEMENTED)
         * might be used implicitly.
         *
         * @param sharedDataHandler the {@link SharedDataHandler} implementation to manage shared data.
         * @return this {@code Builder} instance for method chaining.
         * @see SharedDataHandler
         */
        public Builder withSharedDataHandler(SharedDataHandler sharedDataHandler) {
            this.sharedDataHandler = sharedDataHandler;
            return this;
        }

        /**
         * Configures whether the plugin instance should be shared across multiple host requests or contexts.
         *
         * <p>If {@code shared} is {@code true}, a single WASM instance will be created and reused.
         * across multiple concurrent requests.  Since Proxy-Wasm plugins are not thread-safe, the requests will
         * contend on an access lock for the plugin.  Using a shared plugin allows the plugin to maintain state
         * between the requests.  It will use less memory but will have a performance impact due to the contention.
         *
         * <p>If {@code shared} is {@code false} (the default), the host will create a new, separate WASM instance for each
         * request or context (depending on the host implementation and threading model). This provides better
         * isolation, eliminates contention, but consumes more memory.
         *
         * @param shared {@code true} to indicate the plugin instance can be shared, {@code false} otherwise.
         * @return this {@code Builder} instance for method chaining.
         */
        public Builder withShared(boolean shared) {
            this.shared = shared;
            return this;
        }

        /**
         * Sets the Virtual Machine (VM) configuration data for the plugin.
         * This configuration is typically provided once when the VM (and the plugin) is initialized.
         * It's accessible to the plugin via the {@code proxy_get_vm_configuration} ABI call.
         *
         * @param vmConfig A byte array containing the VM configuration data.
         * @return this {@code Builder} instance for method chaining.
         */
        public Builder withVmConfig(byte[] vmConfig) {
            this.vmConfig = vmConfig;
            return this;
        }

        /**
         * Sets the Virtual Machine (VM) configuration data for the plugin using a String.
         * The string will be converted to bytes using the platform's default charset.
         * This configuration is accessible via the {@code proxy_get_vm_configuration} ABI call.
         *
         * @param vmConfig A String containing the VM configuration data.
         * @return this {@code Builder} instance for method chaining.
         * @see #withVmConfig(byte[])
         */
        public Builder withVmConfig(String vmConfig) {
            this.vmConfig = bytes(vmConfig);
            return this;
        }

        /**
         * Sets the specific configuration data for this plugin instance.
         * This configuration is provided during the plugin's initialization phase
         * (via {@code proxy_on_configure}) and allows tailoring the plugin's behavior.
         * It's accessible to the plugin via the {@code proxy_get_plugin_configuration} ABI call.
         *
         * @param pluginConfig A byte array containing the plugin-specific configuration data.
         * @return this {@code Builder} instance for method chaining.
         */
        public Builder withPluginConfig(byte[] pluginConfig) {
            this.pluginConfig = pluginConfig;
            return this;
        }

        /**
         * Sets the specific configuration data for this plugin instance using a String.
         * The string will be converted to bytes using the platform's default charset.
         * This configuration is accessible via the {@code proxy_get_plugin_configuration} ABI call.
         *
         * @param pluginConfig A String containing the plugin-specific configuration data.
         * @return this {@code Builder} instance for method chaining.
         * @see #withPluginConfig(byte[])
         */
        public Builder withPluginConfig(String pluginConfig) {
            this.pluginConfig = bytes(pluginConfig);
            return this;
        }

        /**
         * Provides an explicit memory instance to be used by the WASM module.
         *
         * @param memory The {@link ImportMemory} instance to be used by the WASM module.
         * @return this {@code Builder} instance for method chaining.
         */
        public Builder withImportMemory(ImportMemory memory) {
            proxyWasmBuilder.withImportMemory(memory);
            return this;
        }

        /**
         * Configures a custom factory for creating the {@link Machine} used to execute the WASM code.
         * The {@link Machine} controls the low-level execution of WASM instructions.
         * By default, an interpreter-based machine is used.
         * Providing a custom factory allows using alternative execution strategies, such as
         * wasm to bytecode compilation to improve execution performance.
         *
         * <p>See the Chicory documentation (https://chicory.dev/docs/usage/runtime-compiler) for more details
         * on WASM to bytecode compilation and execution.
         *
         * @param machineFactory A function that takes a WASM {@link Instance} and returns a {@link Machine}.
         * @return this {@code Builder} instance for method chaining.
         */
        public Builder withMachineFactory(Function<Instance, Machine> machineFactory) {
            proxyWasmBuilder.withMachineFactory(machineFactory);
            return this;
        }

        /**
         * Configures WebAssembly System Interface (WASI) options for the plugin instance.
         * WASI provides a standard interface for WASM modules to interact with the underlying operating system
         * for tasks like file system access, environment variables, etc. While Proxy-WASM defines its own ABI,
         * some modules might also utilize WASI features.
         *
         * @param options The {@link WasiOptions} to configure for the WASI environment.
         * @return this {@code Builder} instance for method chaining.
         */
        public Builder withWasiOptions(WasiOptions options) {
            proxyWasmBuilder.withWasiOptions(options);
            return this;
        }

        /**
         * Constructs and initializes the {@link Plugin} instance based on the configuration
         * provided to this builder.
         *
         * <p>This involves setting up the WASM environment, linking host functions, applying configurations,
         * and calling the necessary Proxy-WASM lifecycle functions (like {@code _start} and
         * {@code proxy_on_vm_start}).
         *
         * @return The fully configured and initialized {@link Plugin} instance.
         * @throws StartException If any error occurs during the plugin initialization process
         *                        (e.g., WASM instantiation failure, error during {@code proxy_on_vm_start}).
         */
        public Plugin build() throws StartException {
            return new io.roastedroot.proxywasm.internal.Plugin(
                    proxyWasmBuilder.build(module),
                    shared,
                    name,
                    foreignFunctions,
                    upstreams,
                    strictUpstreams,
                    minTickPeriodMilliseconds,
                    logger,
                    vmConfig,
                    pluginConfig,
                    metricsHandler,
                    sharedQueueHandler,
                    sharedDataHandler);
        }
    }
}
