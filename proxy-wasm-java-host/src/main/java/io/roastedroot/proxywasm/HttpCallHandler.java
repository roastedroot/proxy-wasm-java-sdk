package io.roastedroot.proxywasm;

public interface HttpCallHandler {

    default int httpCall(
            String uri, ProxyMap headers, byte[] body, ProxyMap trailers, int timeoutMilliseconds)
            throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    default int dispatchHttpCall(
            String upstreamName,
            ProxyMap headers,
            byte[] body,
            ProxyMap trailers,
            int timeoutMilliseconds)
            throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    /**
     * Set the HTTP call response body.
     *
     * @param body The HTTP call response body as a byte[]
     * @return WasmResult indicating success or failure
     */
    default WasmResult setHttpCallResponseBody(byte[] body) {
        return WasmResult.UNIMPLEMENTED;
    }

    default ProxyMap getHttpCallResponseHeaders() {
        return null;
    }

    default ProxyMap getHttpCallResponseTrailers() {
        return null;
    }

    /**
     * Get the HTTP call response body.
     *
     * @return The HTTP call response body as a byte[], or null if not available
     */
    default byte[] getHttpCallResponseBody() {
        return null;
    }
}
