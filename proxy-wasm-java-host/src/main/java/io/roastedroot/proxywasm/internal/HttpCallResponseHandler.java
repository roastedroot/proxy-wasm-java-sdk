package io.roastedroot.proxywasm.internal;

public interface HttpCallResponseHandler {
    void call(int statusCode, ProxyMap headers, byte[] body);
}
