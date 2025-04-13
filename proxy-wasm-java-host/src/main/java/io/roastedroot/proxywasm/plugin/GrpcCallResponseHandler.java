package io.roastedroot.proxywasm.plugin;

import io.roastedroot.proxywasm.ArrayBytesProxyMap;

public interface GrpcCallResponseHandler {

    void onHeaders(ArrayBytesProxyMap trailerMap);

    void onMessage(byte[] data);

    void onTrailers(ArrayBytesProxyMap trailers);

    void onClose(int status);
}
