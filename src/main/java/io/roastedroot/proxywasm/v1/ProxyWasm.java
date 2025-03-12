package io.roastedroot.proxywasm.v1;

import com.dylibso.chicory.runtime.ByteBufferMemory;
import com.dylibso.chicory.runtime.HostFunction;
import com.dylibso.chicory.runtime.ImportMemory;
import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;
import com.dylibso.chicory.wasm.InvalidException;
import com.dylibso.chicory.wasm.WasmModule;
import com.dylibso.chicory.wasm.types.MemoryLimits;
import io.roastedroot.proxywasm.impl.Exports;
import io.roastedroot.proxywasm.impl.Imports;
import io.roastedroot.proxywasm.impl.Imports_ModuleFactory;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class ProxyWasm implements Closeable {

    private final Exports exports;
    private final Imports imports;
    private final Handler pluginHandler;
    private final byte[] pluginConfig;
    private final byte[] vmConfig;
    private final HashMap<String, String> properties;
    private final WasiPreview1 wasi;

    private final AtomicInteger nextContextID = new AtomicInteger(1);
    private final ABIVersion abi_version;
    private Context pluginContext;
    private Context activeContext;

    private HashMap<Integer, Context> contexts = new HashMap<>();

    private ProxyWasm(Builder other) throws StartException {
        this.vmConfig = other.vmConfig;
        this.pluginConfig = other.pluginConfig;
        this.properties = Objects.requireNonNullElse(other.properties, new HashMap<>());
        this.pluginHandler = Objects.requireNonNullElse(other.pluginHandler, new Handler() {});
        this.wasi = other.wasi;
        this.exports = other.exports;
        this.imports = other.imports;
        this.imports.setHandler(createImportsHandler());

        this.abi_version = findAbiVersion();

        // Since 0_2_0, prefer proxy_on_memory_allocate over malloc
        if (instanceExportsFunction("proxy_on_memory_allocate")) {
            this.exports.setMallocFunctionName("proxy_on_memory_allocate");
        }

        // initialize/start the vm
        if (instanceExportsFunction("_initialize")) {
            this.exports.initialize();
            if (instanceExportsFunction("main")) {
                this.exports.main(0, 0);
            }
        } else {
            if (instanceExportsFunction("_start")) {
                this.exports.start();
            }
        }

        // start the vm with the vmHandler, it will receive stuff like log messages.
        this.pluginContext = new PluginContext(this, pluginHandler);
        registerContext(pluginContext, 0);
        if (!exports.proxyOnVmStart(pluginContext.id(), vmConfig.length)) {
            throw new StartException("proxy_on_vm_start failed");
        }
        if (!exports.proxyOnConfigure(pluginContext.id(), pluginConfig.length)) {
            throw new StartException("proxy_on_configure failed");
        }
    }

    private void registerContext(Context context, int parentContextID) {
        contexts.put(context.id(), context);
        activeContext = context;
        exports.proxyOnContextCreate(context.id(), parentContextID);
    }

    private ABIVersion findAbiVersion() throws StartException {
        for (var version : ABIVersion.values()) {
            if (instanceExportsFunction(version.getAbiMarkerFunction())) {
                return version;
            }
        }
        throw new StartException("wasm module does nto contain a supported proxy-wasm abi version");
    }

    private boolean instanceExportsFunction(String name) {
        try {
            this.imports.getInstance().exports().function(name);
            return true;
        } catch (InvalidException e) {
            return false;
        }
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
            public ByteBuffer getVmConfig() {
                return ByteBuffer.wrap(vmConfig);
            }

            @Override
            public ByteBuffer getPluginConfig() {
                return ByteBuffer.wrap(pluginConfig);
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
        };
    }

    HashMap<Integer, Context> contexts() {
        return contexts;
    }

    Exports exports() {
        return exports;
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

    /**
     * Delivers a tick event to the plugin.
     * <p>
     * tick() should be called in response to a Handler.setTickPeriodMilliseconds(int tick_period) callback.
     */
    public void tick() {
        exports.proxyOnTick(pluginContext.id());
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

    public static class Builder implements Cloneable {

        private Exports exports;
        private final Imports imports = new Imports();
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
            return Imports_ModuleFactory.toHostFunctions(imports);
        }

        public ProxyWasm.Builder withVmConfig(String vmConfig) {
            this.vmConfig = vmConfig.getBytes(StandardCharsets.UTF_8);
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
            exports = new Exports(instance.exports());
            imports.setInstance(instance);
            imports.setExports(exports);
            return new ProxyWasm(this.clone());
        }

        public ProxyWasm build(WasmModule module) throws StartException {
            var imports = ImportValues.builder();

            imports.addMemory(Objects.requireNonNullElseGet(memory, this::defaultImportMemory));
            imports.addFunction(toHostFunctions());

            wasi =
                    WasiPreview1.builder()
                            .withOptions(
                                    Objects.requireNonNullElseGet(
                                            wasiOptions, this::defaultWasiOptions))
                            .build();
            imports.addFunction(wasi.toHostFunctions());
            imports.addFunction(Helpers.withModuleName(wasi.toHostFunctions(), "wasi_unstable"));

            var instance =
                    Instance.builder(module)
                            .withStart(false) // we will start it manually
                            .withImportValues(imports.build())
                            .build();

            return build(instance);
        }

        ImportMemory defaultImportMemory() {
            return new ImportMemory(
                    "env",
                    "memory",
                    new ByteBufferMemory(new MemoryLimits(2, MemoryLimits.MAX_PAGES)));
        }

        WasiOptions defaultWasiOptions() {
            return WasiOptions.builder().inheritSystem().build();
        }
    }

    public static void start(int abi_version_ignored) {
        // ... existing code ...
    }
}
