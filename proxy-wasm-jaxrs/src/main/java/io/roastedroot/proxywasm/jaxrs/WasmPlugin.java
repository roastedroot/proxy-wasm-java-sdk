package io.roastedroot.proxywasm.jaxrs;

import com.dylibso.chicory.runtime.ImportMemory;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.HttpContext;
import io.roastedroot.proxywasm.ProxyWasm;
import io.roastedroot.proxywasm.StartException;
import java.util.Objects;

public class WasmPlugin {

    private final String name;
    private final ProxyWasm proxyWasm;
    private final JaxrsHandler handler;

    public WasmPlugin(String name, ProxyWasm proxyWasm, JaxrsHandler handler) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(proxyWasm);
        Objects.requireNonNull(handler);
        this.name = name;
        this.proxyWasm = proxyWasm;
        this.handler = handler;
    }

    public String name() {
        return name;
    }

    ProxyWasm proxyWasm() {
        return proxyWasm;
    }

    HttpContext createHttpContext() {
        return proxyWasm.createHttpContext(this.handler);
    }

    JaxrsHandler handler() {
        return handler;
    }

    public static WasmPlugin.Builder builder() {
        return new WasmPlugin.Builder();
    }

    public static class Builder implements Cloneable {

        JaxrsHandler handler = new JaxrsHandler();
        String name = "default";
        ProxyWasm.Builder proxyWasmBuilder = ProxyWasm.builder().withPluginHandler(handler);

        public WasmPlugin.Builder withName(String name) {
            this.name = name;
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
            return new WasmPlugin(name, proxyWasm, handler);
        }
    }
}
