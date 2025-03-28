package io.roastedroot.proxywasm.plugin;

import io.roastedroot.proxywasm.ProxyMap;

public class HttpCallResponse {

    public final int statusCode;
    public final ProxyMap headers;
    public final byte[] body;

    public HttpCallResponse(int statusCode, ProxyMap headers, byte[] body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }
}
