package io.roastedroot.proxywasm.internal;

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
