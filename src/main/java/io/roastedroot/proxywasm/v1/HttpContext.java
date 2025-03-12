package io.roastedroot.proxywasm.v1;

import static io.roastedroot.proxywasm.v1.Helpers.length;

public class HttpContext extends Context {

    private final Handler handler;

    HttpContext(ProxyWasm proxyWasm, Handler handler) {
        super(proxyWasm);
        this.handler = handler;
    }

    Handler handler() {
        return handler;
    }

    public Action callOnRequestHeaders(boolean endOfStream) {
        var requestHeaders = handler.getHttpRequestHeader();
        int result =
                proxyWasm
                        .exports()
                        .proxyOnRequestHeaders(id, length(requestHeaders), endOfStream ? 1 : 0);
        return Action.fromInt(result);
    }

    public Action callOnRequestBody(boolean endOfStream) {
        var requestBody = handler.getHttpRequestBody();
        int result =
                proxyWasm
                        .exports()
                        .proxyOnRequestBody(id, length(requestBody), endOfStream ? 1 : 0);
        return Action.fromInt(result);
    }

    public Action callOnResponseBody(boolean endOfStream) {
        var responseBody = handler.getHttpResponseBody();
        int result =
                proxyWasm
                        .exports()
                        .proxyOnResponseBody(id, length(responseBody), endOfStream ? 1 : 0);
        return Action.fromInt(result);
    }
}
