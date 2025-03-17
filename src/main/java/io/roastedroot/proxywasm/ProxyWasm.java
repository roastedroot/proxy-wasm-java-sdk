package io.roastedroot.proxywasm;

import static io.roastedroot.proxywasm.Helpers.len;

import com.dylibso.chicory.runtime.ByteArrayMemory;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.ImportMemory;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;
import com.dylibso.chicory.wasm.WasmModule;
import com.dylibso.chicory.wasm.types.MemoryLimits;
import com.dylibso.chicory.wasm.types.ValueType;
import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class ProxyWasm implements Closeable {

    private final ABI abi;
    private final Handler pluginHandler;
    private final byte[] pluginConfig;
    private final byte[] vmConfig;
    private final HashMap<String, String> properties;
    private final WasiPreview1 wasi;

    private final AtomicInteger nextContextID = new AtomicInteger(1);
    private Context pluginContext;
    private Context activeContext;

    private HashMap<Integer, Context> contexts = new HashMap<>();
    private ProxyMap httpCallResponseHeaders;
    private ProxyMap httpCallResponseTrailers;
    private byte[] httpCallResponseBody;
    private HashMap<String, ForeignFunction> foreignFunctions = new HashMap<>();

    private ProxyWasm(Builder other) throws StartException {
        this.vmConfig = other.vmConfig;
        this.pluginConfig = other.pluginConfig;
        this.properties = Objects.requireNonNullElse(other.properties, new HashMap<>());
        this.pluginHandler = Objects.requireNonNullElse(other.pluginHandler, new Handler() {});
        this.wasi = other.wasi;
        this.abi = other.abi;
        this.abi.setHandler(createImportsHandler());

        // initialize/start the vm
        if (this.abi.initialize()) {
            this.abi.main(0, 0);
        } else {
            this.abi.start();
        }

        // start the vm with the vmHandler, it will receive stuff like log messages.
        this.pluginContext = new PluginContext(this, pluginHandler);
        registerContext(pluginContext, 0);
        if (!this.abi.proxyOnVmStart(pluginContext.id(), vmConfig.length)) {
            throw new StartException("proxy_on_vm_start failed");
        }
        if (!this.abi.proxyOnConfigure(pluginContext.id(), pluginConfig.length)) {
            throw new StartException("proxy_on_configure failed");
        }
    }

    private void registerContext(Context context, int parentContextID) {
        contexts.put(context.id(), context);
        activeContext = context;
        this.abi.proxyOnContextCreate(context.id(), parentContextID);
    }

    // Let's implement some of the handler functions to make life easier for the user.
    // The user's handler will be the last handler in the chain.
    private ChainedHandler createImportsHandler() {
        return new ChainedHandler() {
            @Override
            protected Handler next() {
                return activeContext.handler();
            }

            @Override
            public byte[] getVmConfig() {
                return vmConfig;
            }

            @Override
            public byte[] getPluginConfig() {
                return pluginConfig;
            }

            @Override
            public String getProperty(String key) throws WasmException {
                if (properties.containsKey(key)) {
                    return properties.get(key);
                }

                var handler = activeContext.handler();
                if (handler != null) {
                    return handler.getProperty(key);
                }
                return null;
            }

            @Override
            public WasmResult setEffectiveContextID(int contextID) {
                Context context = contexts.get(contextID);
                if (context == null) {
                    return WasmResult.BAD_ARGUMENT;
                }
                activeContext = context;
                return WasmResult.OK;
            }

            @Override
            public WasmResult done() {
                return activeContext.done();
            }

            @Override
            public ProxyMap getHttpCallResponseHeaders() {
                return httpCallResponseHeaders;
            }

            @Override
            public ProxyMap getHttpCallResponseTrailers() {
                return httpCallResponseTrailers;
            }

            @Override
            public byte[] getHttpCallResponseBody() {
                return httpCallResponseBody;
            }

            @Override
            public byte[] callForeignFunction(String name, byte[] bytes) throws WasmException {
                ForeignFunction func = foreignFunctions.get(name);
                if (func == null) {
                    throw new WasmException(WasmResult.NOT_FOUND);
                }
                return func.apply(bytes);
            }
        };
    }

    HashMap<Integer, Context> contexts() {
        return contexts;
    }

    Context getActiveContext() {
        return activeContext;
    }

    void setActiveContext(Context activeContext) {
        if (activeContext == null) {
            // this happens when a context is finishes closing...
            // assuming the current context should be the plugin context after that,
            // but maybe it would be better to error out if a new context is not set.
            activeContext = this.pluginContext;
        }
        this.activeContext = activeContext;
    }

    int nextContextID() {
        return nextContextID.getAndIncrement();
    }

    public HttpContext createHttpContext(Handler handler) {
        HttpContext context = new HttpContext(this, handler);
        registerContext(context, this.pluginContext.id());
        return context;
    }

    public NetworkContext createNetworkContext(Handler handler) {
        NetworkContext context = new NetworkContext(this, handler);
        registerContext(context, this.pluginContext.id());
        return context;
    }

    /**
     * Delivers a tick event to the plugin.
     * <p>
     * tick() should be called in response to a Handler.setTickPeriodMilliseconds(int tick_period) callback.
     */
    public void tick() {
        this.abi.proxyOnTick(pluginContext.id());
    }

    public void setProperty(String[] path, String data) {
        if (len(path) == 0) {
            throw new IllegalArgumentException("path must not be empty");
        }
        if (len(data) == 0) {
            throw new IllegalArgumentException("data must not be empty");
        }

        this.properties.put(String.join("\u0000", path), data);
    }

    public String getProperty(String[] path) {
        if (len(path) == 0) {
            throw new IllegalArgumentException("path must not be empty");
        }
        return this.properties.get(String.join("\u0000", path));
    }

    @Override
    public void close() {
        this.pluginContext.close();
        if (wasi != null) {
            wasi.close();
        }
    }

    public static ProxyWasm.Builder builder() {
        return new ProxyWasm.Builder();
    }

    public void sendHttpCallResponse(
            int calloutID, Map<String, String> headers, Map<String, String> trailers, byte[] body) {
        this.sendHttpCallResponse(
                calloutID, ProxyMap.copyOf(headers), ProxyMap.copyOf(trailers), body);
    }

    public void sendHttpCallResponse(
            int calloutID, ProxyMap headers, ProxyMap trailers, byte[] body) {

        this.httpCallResponseHeaders = headers;
        this.httpCallResponseTrailers = trailers;
        this.httpCallResponseBody = body;

        this.abi.proxyOnHttpCallResponse(
                pluginContext.id(), calloutID, len(headers), len(body), len(trailers));

        this.httpCallResponseHeaders = null;
        this.httpCallResponseTrailers = null;
        this.httpCallResponseBody = null;
    }

    public void sendOnQueueReady(int queueId) {
        this.abi.proxyOnQueueReady(pluginContext.id(), queueId);
    }

    public int contextId() {
        return pluginContext.id();
    }

    public void registerForeignFunction(String name, ForeignFunction func) {
        foreignFunctions.put(name, func);
    }

    ABI abi() {
        return abi;
    }

    public static class Builder implements Cloneable {

        private final ABI abi = new ABI();
        private WasiPreview1 wasi;

        private byte[] vmConfig = new byte[0];
        private byte[] pluginConfig = new byte[0];
        private HashMap<String, String> properties;
        private Handler pluginHandler;
        private ImportMemory memory;
        private WasiOptions wasiOptions;

        @Override
        @SuppressWarnings("NoClone")
        protected Builder clone() {
            try {
                return (Builder) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        public HostFunction[] toHostFunctions() {
            return ABI_ModuleFactory.toHostFunctions(abi);
        }

        public ProxyWasm.Builder withVmConfig(byte[] vmConfig) {
            this.vmConfig = vmConfig;
            return this;
        }

        public ProxyWasm.Builder withVmConfig(String vmConfig) {
            this.vmConfig = vmConfig.getBytes(StandardCharsets.UTF_8);
            return this;
        }

        public ProxyWasm.Builder withPluginConfig(byte[] pluginConfig) {
            this.pluginConfig = pluginConfig;
            return this;
        }

        public ProxyWasm.Builder withPluginConfig(String pluginConfig) {
            this.pluginConfig = pluginConfig.getBytes(StandardCharsets.UTF_8);
            return this;
        }

        public ProxyWasm.Builder withProperties(Map<String, String> properties) {
            if (properties != null) {
                this.properties = new HashMap<>(properties);
            } else {
                this.properties = null;
            }
            return this;
        }

        public ProxyWasm.Builder withPluginHandler(Handler vmHandler) {
            this.pluginHandler = vmHandler;
            return this;
        }

        public ProxyWasm.Builder withImportMemory(ImportMemory memory) {
            this.memory = memory;
            return this;
        }

        public ProxyWasm.Builder withWasiOptions(WasiOptions options) {
            this.wasiOptions = options;
            return this;
        }

        Builder() {}

        public ProxyWasm build(Instance instance) throws StartException {
            abi.setInstance(instance);
            return new ProxyWasm(this.clone());
        }

        public ProxyWasm build(WasmModule module) throws StartException {
            return this.build(Instance.builder(module));
        }

        public ProxyWasm build(Instance.Builder instanceBuilder) throws StartException {
            var imports = ImportValues.builder();

            imports.addMemory(Objects.requireNonNullElseGet(memory, this::defaultImportMemory));
            imports.addFunction(toHostFunctions());
            imports.addFunction(
                    new HostFunction(
                            "env",
                            "emscripten_notify_memory_growth",
                            List.of(ValueType.I32),
                            List.of(),
                            (inst, args) -> {
                                return null;
                            }));

            wasi =
                    WasiPreview1.builder()
                            .withOptions(
                                    Objects.requireNonNullElseGet(
                                            wasiOptions,
                                            () ->
                                                    WasiOptions.builder()
                                                            .inheritSystem()
                                                            .withArguments(List.of())
                                                            .build()))
                            .build();
            imports.addFunction(wasi.toHostFunctions());
            imports.addFunction(Helpers.withModuleName(wasi.toHostFunctions(), "wasi_unstable"));

            var instance =
                    instanceBuilder
                            .withStart(false) // we will start it manually
                            .withImportValues(imports.build())
                            .build();

            return build(instance);
        }

        ImportMemory defaultImportMemory() {
            return new ImportMemory(
                    "env",
                    "memory",
                    new ByteArrayMemory(new MemoryLimits(2, MemoryLimits.MAX_PAGES)));
        }

        WasiOptions defaultWasiOptions() {
            return WasiOptions.builder().inheritSystem().build();
        }
    }

    public static void start(int abi_version_ignored) {
        // ... existing code ...
    }
}
