package io.roastedroot.proxywasm.internal;

public interface GrpcCallResponseHandler {

    void onHeaders(ArrayBytesProxyMap trailerMap);

    void onMessage(byte[] data);

    void onTrailers(ArrayBytesProxyMap trailers);

    void onClose(int status);
}
