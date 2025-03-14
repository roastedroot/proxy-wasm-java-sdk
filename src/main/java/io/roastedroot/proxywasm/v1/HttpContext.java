package io.roastedroot.proxywasm.v1;

import static io.roastedroot.proxywasm.v1.Helpers.len;

public class HttpContext extends Context {

    private final Handler handler;
    Action action;

    HttpContext(ProxyWasm proxyWasm, Handler handler) {
        super(proxyWasm);
        this.handler = handler;
    }

    Handler handler() {
        return handler;
    }

    public Action callOnRequestHeaders(boolean endOfStream) {
        var headers = handler.getHttpRequestHeaders();
        int result =
                proxyWasm.exports().proxyOnRequestHeaders(id, len(headers), endOfStream ? 1 : 0);
        return Action.fromInt(result);
    }

    public Action callOnResponseHeaders(boolean endOfStream) {
        var headers = handler.getHttpResponseHeaders();
        int result =
                proxyWasm.exports().proxyOnResponseHeaders(id, len(headers), endOfStream ? 1 : 0);
        return Action.fromInt(result);
    }

    public Action callOnRequestBody(boolean endOfStream) {
        var requestBody = handler.getHttpRequestBody();
        int result =
                proxyWasm
                        .exports()
                        .proxyOnRequestBody(id, Helpers.len(requestBody), endOfStream ? 1 : 0);
        return Action.fromInt(result);
    }

    public Action callOnResponseBody(boolean endOfStream) {
        var responseBody = handler.getHttpResponseBody();
        int result =
                proxyWasm
                        .exports()
                        .proxyOnResponseBody(id, Helpers.len(responseBody), endOfStream ? 1 : 0);
        return Action.fromInt(result);
    }
}
