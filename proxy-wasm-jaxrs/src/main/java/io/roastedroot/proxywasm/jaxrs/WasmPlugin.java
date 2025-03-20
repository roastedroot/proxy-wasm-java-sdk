package io.roastedroot.proxywasm.jaxrs;

import com.dylibso.chicory.runtime.ImportMemory;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.ForeignFunction;
import io.roastedroot.proxywasm.ProxyWasm;
import io.roastedroot.proxywasm.StartException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class WasmPlugin {

    private final ProxyWasm proxyWasm;
    private final PluginHandler handler;
    private final ReentrantLock lock;

    private WasmPlugin(ProxyWasm proxyWasm, PluginHandler handler, boolean shared) {
        Objects.requireNonNull(proxyWasm);
        Objects.requireNonNull(handler);
        this.proxyWasm = proxyWasm;
        this.handler = handler;
        this.lock = shared ? new ReentrantLock() : null;
    }

    public String name() {
        return handler.getName();
    }

    ProxyWasm proxyWasm() {
        return proxyWasm;
    }

    PluginHandler pluginHandler() {
        return handler;
    }

    public static WasmPlugin.Builder builder() {
        return new WasmPlugin.Builder();
    }

    public void lock() {
        if (lock == null) {
            return;
        }
        lock.lock();
    }

    public void unlock() {
        if (lock == null) {
            return;
        }
        lock.unlock();
    }

    public boolean isShared() {
        return lock != null;
    }

    public static class Builder implements Cloneable {

        private PluginHandler handler = new PluginHandler();
        private ProxyWasm.Builder proxyWasmBuilder = ProxyWasm.builder().withPluginHandler(handler);
        private boolean shared = true;

        public WasmPlugin.Builder withName(String name) {
            this.handler.name = name;
            return this;
        }

        public Builder withForeignFunctions(Map<String, ForeignFunction> functions) {
            this.handler.foreignFunctions = new HashMap<>(functions);
            return this;
        }

        public Builder withLogger(Logger logger) {
            this.handler.logger = logger;
            return this;
        }

        public WasmPlugin.Builder withShared(boolean shared) {
            this.shared = shared;
            return this;
        }

        public WasmPlugin.Builder withVmConfig(byte[] vmConfig) {
            proxyWasmBuilder = proxyWasmBuilder.withVmConfig(vmConfig);
            return this;
        }

        public WasmPlugin.Builder withVmConfig(String vmConfig) {
            proxyWasmBuilder = proxyWasmBuilder.withVmConfig(vmConfig);
            return this;
        }

        public WasmPlugin.Builder withPluginConfig(byte[] pluginConfig) {
            proxyWasmBuilder = proxyWasmBuilder.withPluginConfig(pluginConfig);
            return this;
        }

        public WasmPlugin.Builder withPluginConfig(String pluginConfig) {
            proxyWasmBuilder = proxyWasmBuilder.withPluginConfig(pluginConfig);
            return this;
        }

        public WasmPlugin.Builder withImportMemory(ImportMemory memory) {
            proxyWasmBuilder = proxyWasmBuilder.withImportMemory(memory);
            return this;
        }

        public WasmPlugin build(WasmModule module) throws StartException {
            return build(proxyWasmBuilder.build(module));
        }

        public WasmPlugin build(Instance.Builder instanceBuilder) throws StartException {
            return build(proxyWasmBuilder.build(instanceBuilder));
        }

        public WasmPlugin build(Instance instance) throws StartException {
            return build(proxyWasmBuilder.build(instance));
        }

        public WasmPlugin build(ProxyWasm proxyWasm) throws StartException {
            return new WasmPlugin(proxyWasm, handler, shared);
        }
    }
}
