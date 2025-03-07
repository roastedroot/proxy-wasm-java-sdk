package io.roastedroot.proxywasm.v1;

import com.dylibso.chicory.runtime.*;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;
import com.dylibso.chicory.wasm.WasmModule;
import com.dylibso.chicory.wasm.types.MemoryLimits;

import java.io.Closeable;
import java.util.Objects;

public class ProxyWasmEnv extends ProxyWasm implements Closeable {

    private final WasiPreview1 wasi;

    private ProxyWasmEnv(WasiPreview1 wasi, ProxyWasm proxyWasm) {
        super(proxyWasm);
        this.wasi = wasi;
    }

    public static ProxyWasmEnv.Builder builder(WasmModule module) {
        return new ProxyWasmEnv.Builder(module);
    }

    public static final class Builder {

        private final WasmModule module;
        private ImportMemory memory;
        private WasiOptions wasiOptions;

        Builder(WasmModule module) {
            this.module = module;
        }

        public ProxyWasmEnv.Builder withImportMemory(ImportMemory memory) {
            this.memory = memory;
            return this;
        }

        public ProxyWasmEnv.Builder withWasiOptions(WasiOptions options) {
            this.wasiOptions = options;
            return this;
        }

        public ProxyWasmEnv build() {

            var store = new Store();
            store.addMemory(Objects.requireNonNullElseGet(memory, this::defaultImportMemory));

            var wasi = WasiPreview1.builder().withOptions(
                    Objects.requireNonNullElseGet(wasiOptions, this::defaultWasiOptions)
            ).build();
            store.addFunction(wasi.toHostFunctions());
            store.addFunction(Helpers.withModuleName(wasi.toHostFunctions(), "wasi_unstable"));

            var proxyWasmBuilder = ProxyWasm.builder();
            store.addFunction(proxyWasmBuilder.toHostFunctions());

            var instance = store.instantiate("test", module);
            return new ProxyWasmEnv(wasi, proxyWasmBuilder.build(instance));

        }

        ImportMemory defaultImportMemory() {
            return new ImportMemory("env", "memory",
                    new ByteBufferMemory(new MemoryLimits(2, MemoryLimits.MAX_PAGES)));
        }

        WasiOptions defaultWasiOptions() {
            return WasiOptions.builder().inheritSystem().build();
        }
    }

    @Override
    public void close() {
        wasi.close();
    }


}
