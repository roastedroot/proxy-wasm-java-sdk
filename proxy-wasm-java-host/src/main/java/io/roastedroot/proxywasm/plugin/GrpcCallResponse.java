package io.roastedroot.proxywasm.plugin;

import io.roastedroot.proxywasm.ProxyMap;

public class GrpcCallResponse {

    public final int statusCode;
    public final ProxyMap headers;
    public final byte[] body;

    public GrpcCallResponse(int statusCode, ProxyMap headers, byte[] body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }
}
