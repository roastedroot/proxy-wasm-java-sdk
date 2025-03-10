package io.roastedroot.proxywasm.v1;

import com.dylibso.chicory.runtime.*;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyWasm implements Closeable {

    private final Exports exports;
    private final Imports imports;
    private final Handler vmHandler;
    private final byte[] pluginConfig;
    private final byte[] vmConfig;
    private final HashMap<String, String> properties;
    private final WasiPreview1 wasi;

    private final AtomicInteger nextContextID = new AtomicInteger(1);
    private final ABIVersion abi_version;
    private int rootContextID = 0;
    private Handler handler;

    private ProxyWasm(Builder other) throws StartException {
        this.vmConfig = other.vmConfig;
        this.pluginConfig = other.pluginConfig;
        this.properties = Objects.requireNonNullElse(other.properties, new HashMap<>());
        this.vmHandler = Objects.requireNonNullElse(other.vmHandler, new DefaultHandler());
        this.wasi = other.wasi;

        // start the vm with the vmHandler, it will receive stuff like log messages.
        this.handler = vmHandler;

        this.exports = other.exports;
        this.imports = other.imports;
        this.imports.setHandler(createImportsHandler());
        this.rootContextID = nextContextID.getAndIncrement();

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

        exports.proxyOnContextCreate(rootContextID, 0);
        if (!exports.proxyOnVmStart(rootContextID, vmConfig.length)) {
            throw new StartException("proxy_on_vm_start failed");
        }
        if (!exports.proxyOnConfigure(rootContextID, pluginConfig.length)) {
            throw new StartException("proxy_on_vm_start failed");
        }
    }

    private ABIVersion findAbiVersion() throws StartException {
        final ABIVersion abi_version;
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
    private AbstractChainedHandler createImportsHandler() {
        return new AbstractChainedHandler() {
            @Override
            protected Handler next() {
                return handler;
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
                if (handler != null) {
                    return handler.getProperty(key);
                }
                return null;
            }
        };
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public Context createContext() {
        var contextID = nextContextID.getAndIncrement();
        exports.proxyOnContextCreate(contextID, this.rootContextID);
        return new Context(exports, contextID);
    }

    @Override
    public void close() {
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
        private Handler vmHandler;
        private ImportMemory memory;
        private WasiOptions wasiOptions;

        @Override
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

        public ProxyWasm.Builder withVmConfig(byte[] vmConfig) {
            this.vmConfig = vmConfig.clone();
            return this;
        }

        public ProxyWasm.Builder withPluginConfig(byte[] pluginConfig) {
            this.pluginConfig = pluginConfig.clone();
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

        public ProxyWasm.Builder withVmHandler(Handler vmHandler) {
            this.vmHandler = vmHandler;
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

        Builder() {
        }

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

            wasi = WasiPreview1.builder().withOptions(
                    Objects.requireNonNullElseGet(wasiOptions, this::defaultWasiOptions)
            ).build();
            imports.addFunction(wasi.toHostFunctions());
            imports.addFunction(Helpers.withModuleName(wasi.toHostFunctions(), "wasi_unstable"));

            var instance = Instance.builder(module)
                    .withStart(false) // we will start it manually
                    .withImportValues(imports.build())
                    .build();

            return build(instance);
        }

        ImportMemory defaultImportMemory() {
            return new ImportMemory("env", "memory",
                    new ByteBufferMemory(new MemoryLimits(2, MemoryLimits.MAX_PAGES)));
        }

        WasiOptions defaultWasiOptions() {
            return WasiOptions.builder().inheritSystem().build();
        }
    }


}