package io.roastedroot.proxywasm;

import static io.roastedroot.proxywasm.Helpers.len;

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
        var headers = handler.getHttpRequestHeaders();
        int result = proxyWasm.abi().proxyOnRequestHeaders(id, len(headers), endOfStream ? 1 : 0);
        Action action = Action.fromInt(result);
        handler.setAction(StreamType.REQUEST, action);
        return action;
    }

    public Action callOnResponseHeaders(boolean endOfStream) {
        var headers = handler.getHttpResponseHeaders();
        int result = proxyWasm.abi().proxyOnResponseHeaders(id, len(headers), endOfStream ? 1 : 0);
        Action action = Action.fromInt(result);
        handler.setAction(StreamType.RESPONSE, action);
        return action;
    }

    public Action callOnRequestBody(boolean endOfStream) {
        var requestBody = handler.getHttpRequestBody();
        int result =
                proxyWasm
                        .abi()
                        .proxyOnRequestBody(id, Helpers.len(requestBody), endOfStream ? 1 : 0);
        Action action = Action.fromInt(result);
        handler.setAction(StreamType.REQUEST, action);
        return action;
    }

    public Action callOnResponseBody(boolean endOfStream) {
        var responseBody = handler.getHttpResponseBody();
        int result =
                proxyWasm
                        .abi()
                        .proxyOnResponseBody(id, Helpers.len(responseBody), endOfStream ? 1 : 0);
        Action action = Action.fromInt(result);
        handler.setAction(StreamType.RESPONSE, action);
        return action;
    }
}
