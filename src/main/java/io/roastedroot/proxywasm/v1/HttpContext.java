package io.roastedroot.proxywasm.v1;

import java.util.Map;

public class HttpContext extends Context {

    private final Handler handler;
    private Map<String, String> requestHeaders;

    HttpContext(ProxyWasm proxyWasm, Handler handler) {
        super(proxyWasm);
        this.handler =
                new ChainedHandler() {
                    @Override
                    protected Handler next() {
                        return handler;
                    }

                    @Override
                    public Map<String, String> getHttpRequestHeader() {
                        return requestHeaders;
                    }
                };
    }

    Handler handler() {
        return handler;
    }

    public Action callOnRequestHeaders(Map<String, String> requestHeaders, boolean endOfStream) {
        this.requestHeaders = requestHeaders;
        int result =
                proxyWasm
                        .exports()
                        .proxyOnRequestHeaders(id, requestHeaders.size(), endOfStream ? 1 : 0);
        return Action.fromInt(result);
    }

    public Action callOnRequestBody(byte[] body, boolean endOfStream) {
        int result = proxyWasm.exports().proxyOnRequestBody(id, body.length, endOfStream ? 1 : 0);
        return Action.fromInt(result);
    }
}
