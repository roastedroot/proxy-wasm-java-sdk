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
 * Plugin is an instance of a Proxy-Wasm plugin.
 */
public abstract class Plugin {

    protected Plugin() {}

    public abstract String name();

    /**
     * Creates a new Plugin builder.
     *
     * @return a new Plugin builder
     */
    public static Plugin.Builder builder(WasmModule module) {
        return new Plugin.Builder(module);
    }

    /**
     * Builder for creating a Plugin instance.
     */
    public static final class Builder {

        private final WasmModule module;
        private final ProxyWasm.Builder proxyWasmBuilder = ProxyWasm.builder().withStart(false);
        private boolean shared = true;
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
         * Set the WASM module of the plugin.  The module contains the plugin instructions.
         *
         * @param module the WASM module of the plugin
         * @return this builder
         */
        private Builder(WasmModule module) {
            this.module = module;
        }

        /**
         * Set the name of the plugin.
         *
         * @param name the name of the plugin
         * @return this builder
         */
        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the foreign functions of that can be called from the plugin.
         *
         * @param functions the foreign functions of the plugin
         * @return this builder
         */
        public Builder withForeignFunctions(Map<String, ForeignFunction> functions) {
            this.foreignFunctions = new HashMap<>(functions);
            return this;
        }

        /**
         * Set the upstream server URL
         *
         * @param upstreams the upstream URI mappings.  When a http or grpc call is made
         *                  from the plugin, the upstream name is used to lookup the URL.
         * @return this builder
         */
        public Builder withUpstreams(Map<String, URI> upstreams) {
            this.upstreams = new HashMap<>(upstreams);
            return this;
        }

        /**
         * Set the strict upstreams mode of the plugin.  If strict upstreams is enabled,
         * then the plugin will throw an error if an upstream is not found.  If disabled,
         * then the upstream name is used as the URL.
         *
         * @param strictUpstreams the strict upstreams of the plugin
         * @return this builder
         */
        public Builder withStrictUpstreams(boolean strictUpstreams) {
            this.strictUpstreams = strictUpstreams;
            return this;
        }

        /**
         * Set the minimum tick period of the plugin.  A pluign that requests
         * a very small tick period will be ticked very frequently.  Use this
         * to protect the host from being overwhelmed by the plugin.
         *
         * @param minTickPeriodMilliseconds the minimum tick period of the plugin
         * @return this builder
         */
        public Builder withMinTickPeriodMilliseconds(int minTickPeriodMilliseconds) {
            this.minTickPeriodMilliseconds = minTickPeriodMilliseconds;
            return this;
        }

        /**
         * Set the logger of the plugin.
         *
         * @param logger the logger of the plugin
         * @return this builder
         */
        public Builder withLogger(LogHandler logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Set the metrics handler of the plugin.  If the metrics handler is not set,
         * then calls by the guest to define/use metrics will result in UNIMPLEMENTED errors
         * reported to the guest.
         *
         * @param metricsHandler the metrics handler of the plugin
         * @return this builder
         */
        public Builder withMetricsHandler(MetricsHandler metricsHandler) {
            this.metricsHandler = metricsHandler;
            return this;
        }

        /**
         * Set the shared queue handler of the plugin.  If the sahred queue handler is not set,
         * then calls by the guest to define/use shared queues will result in UNIMPLEMENTED errors
         * reported to the guest.
         *
         * @param sharedQueueHandler the shared queue handler of the plugin
         * @return this builder
         */
        public Builder withSharedQueueHandler(SharedQueueHandler sharedQueueHandler) {
            this.sharedQueueHandler = sharedQueueHandler;
            return this;
        }

        /**
         * Set the shared data handler of the plugin.  If the shared data handler is not set,
         * then calls by the guest to define/use shared data will result in UNIMPLEMENTED errors
         * reported to the guest.
         *
         * @param sharedDataHandler the shared data handler of the plugin
         * @return this builder
         */
        public Builder withSharedDataHandler(SharedDataHandler sharedDataHandler) {
            this.sharedDataHandler = sharedDataHandler;
            return this;
        }

        /**
         * Set whether the plugin is shared between host requests.  If the plugin is shared,
         * then the plugin will be created once and reused for each host request.  If the plugin
         * is not shared, then a new plugin MAY be use for each concurrent host request.
         *
         * @param shared whether the plugin is shared
         * @return this builder
         */
        public Builder withShared(boolean shared) {
            this.shared = shared;
            return this;
        }

        /**
         * Set the VM config of the plugin.
         *
         * @param vmConfig the VM config of the plugin
         * @return this builder
         */
        public Builder withVmConfig(byte[] vmConfig) {
            this.vmConfig = vmConfig;
            return this;
        }

        /**
         * Set the VM config of the plugin.
         *
         * @param vmConfig the VM config of the plugin
         * @return this builder
         */
        public Builder withVmConfig(String vmConfig) {
            this.vmConfig = bytes(vmConfig);
            return this;
        }

        /**
         * Set the plugin config of the plugin.
         *
         * @param pluginConfig the plugin config of the plugin
         * @return this builder
         */
        public Builder withPluginConfig(byte[] pluginConfig) {
            this.pluginConfig = pluginConfig;
            return this;
        }

        /**
         * Set the plugin config of the plugin.
         *
         * @param pluginConfig the plugin config of the plugin
         * @return this builder
         */
        public Builder withPluginConfig(String pluginConfig) {
            this.pluginConfig = bytes(pluginConfig);
            return this;
        }

        /**
         * Set the import memory of the plugin.
         *
         * @param memory the import memory of the plugin
         * @return this builder
         */
        public Builder withImportMemory(ImportMemory memory) {
            proxyWasmBuilder.withImportMemory(memory);
            return this;
        }

        /**
         * Set the machine factory of the plugin.  The machine factory is used to control
         * how instructions are executed.  By default instructions are executed in a
         * by an interpreter.  To increase performance, you can use compile the
         * was instructions to bytecode at runtime or at build time.  For more information
         * see https://chicory.dev/docs/experimental/aot
         *
         * @param machineFactory the machine factory of the plugin
         * @return this builder
         */
        public Builder withMachineFactory(Function<Instance, Machine> machineFactory) {
            proxyWasmBuilder.withMachineFactory(machineFactory);
            return this;
        }

        /**
         * Set the WASI options of the plugin.  A default WASI enviroment will be provided
         * to the pluign.  You can use this method to customize the WASI environment,
         * for example to provide it access to some file system resources.
         *
         * @param options the WASI options of the plugin
         * @return this builder
         */
        public Builder withWasiOptions(WasiOptions options) {
            proxyWasmBuilder.withWasiOptions(options);
            return this;
        }

        /**
         * Build the plugin.
         *
         * @return the plugin
         * @throws StartException if the plugin fails to start
         */
        public io.roastedroot.proxywasm.internal.Plugin build() throws StartException {
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
