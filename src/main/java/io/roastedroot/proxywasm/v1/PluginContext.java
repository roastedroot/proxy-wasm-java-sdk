package io.roastedroot.proxywasm.v1;

public class PluginContext extends Context {

    private final Handler handler;

    PluginContext(ProxyWasm proxyWasm, Handler handler) {
        super(proxyWasm);
        this.handler = handler;
    }

    public Handler handler() {
        return handler;
    }
}
