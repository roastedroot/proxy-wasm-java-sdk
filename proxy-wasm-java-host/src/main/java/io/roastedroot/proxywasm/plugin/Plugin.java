package io.roastedroot.proxywasm.plugin;

import static io.roastedroot.proxywasm.Helpers.bytes;

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

public final class Plugin {

    final PluginHandler handler;
    private final ReentrantLock lock = new ReentrantLock();
    private final boolean shared;
    final ProxyWasm wasm;
    ServerAdaptor httpServer;

    public Logger logger() {
        return handler.logger;
    }

    private Plugin(ProxyWasm proxyWasm, PluginHandler handler, boolean shared) {
        Objects.requireNonNull(proxyWasm);
        Objects.requireNonNull(handler);
        this.shared = shared;
        this.wasm = proxyWasm;
        this.handler = handler;
        this.handler.setPlugin(this);
    }

    public String name() {
        return handler.getName();
    }

    public static Plugin.Builder builder() {
        return new Plugin.Builder();
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public boolean isShared() {
        return shared;
    }

    public void setHttpServer(ServerAdaptor httpServer) {
        this.httpServer = httpServer;
    }

    public HttpContext createHttpContext(HttpRequestAdaptor requestAdaptor) {
        return new HttpContext(this, requestAdaptor);
    }

    public void close() {
        lock();
        try {
            wasm.close();
            handler.close();
        } finally {
            unlock();
        }
    }

    public static class Builder implements Cloneable {

        private PluginHandler handler = new PluginHandler();
        private ProxyWasm.Builder proxyWasmBuilder =
                ProxyWasm.builder().withPluginHandler(handler).withStart(false);
        private boolean shared = true;

        public Plugin.Builder withName(String name) {
            this.handler.name = name;
            return this;
        }

        public Builder withForeignFunctions(Map<String, ForeignFunction> functions) {
            this.handler.foreignFunctions = new HashMap<>(functions);
            return this;
        }

        public Builder withUpstreams(Map<String, String> upstreams) {
            this.handler.upstreams = new HashMap<>(upstreams);
            return this;
        }

        public Builder withStrictUpstreams(boolean strictUpstreams) {
            this.handler.strictUpstreams = strictUpstreams;
            return this;
        }

        public Builder withMinTickPeriodMilliseconds(int minTickPeriodMilliseconds) {
            this.handler.minTickPeriodMilliseconds = minTickPeriodMilliseconds;
            return this;
        }

        public Builder withLogger(Logger logger) {
            this.handler.logger = logger;
            return this;
        }

        public Plugin.Builder withShared(boolean shared) {
            this.shared = shared;
            return this;
        }

        public Plugin.Builder withVmConfig(byte[] vmConfig) {
            this.handler.vmConfig = vmConfig;
            return this;
        }

        public Plugin.Builder withVmConfig(String vmConfig) {
            this.handler.vmConfig = bytes(vmConfig);
            return this;
        }

        public Plugin.Builder withPluginConfig(byte[] pluginConfig) {
            this.handler.pluginConfig = pluginConfig;
            return this;
        }

        public Plugin.Builder withPluginConfig(String pluginConfig) {
            this.handler.pluginConfig = bytes(pluginConfig);
            return this;
        }

        public Plugin.Builder withImportMemory(ImportMemory memory) {
            proxyWasmBuilder = proxyWasmBuilder.withImportMemory(memory);
            return this;
        }

        public Plugin build(WasmModule module) throws StartException {
            return build(proxyWasmBuilder.build(module));
        }

        public Plugin build(Instance.Builder instanceBuilder) throws StartException {
            return build(proxyWasmBuilder.build(instanceBuilder));
        }

        public Plugin build(Instance instance) throws StartException {
            return build(proxyWasmBuilder.build(instance));
        }

        public Plugin build(ProxyWasm proxyWasm) throws StartException {
            return new Plugin(proxyWasm, handler, shared);
        }
    }
}
