package io.roastedroot.proxywasm;

public interface HttpContextHandler extends StreamContextHandler {

    // TODO: use a better type than Map so that we can support repeated headers
    default ProxyMap getHttpRequestHeaders() {
        return null;
    }

    default ProxyMap getHttpRequestTrailers() {
        return null;
    }

    default ProxyMap getHttpResponseHeaders() {
        return null;
    }

    default ProxyMap getHttpResponseTrailers() {
        return null;
    }

    /**
     * Get the HTTP request body.
     *
     * @return The HTTP request body as a byte[], or null if not available
     */
    default byte[] getHttpRequestBody() {
        return null;
    }

    /**
     * Get the HTTP response body.
     *
     * @return The HTTP response body as a byte[], or null if not available
     */
    default byte[] getHttpResponseBody() {
        return null;
    }

    /**
     * Set the HTTP request body.
     *
     * @param body The HTTP request body as a byte[]
     * @return WasmResult indicating success or failure
     */
    default WasmResult setHttpRequestBody(byte[] body) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the HTTP response body.
     *
     * @param body The HTTP response body as a byte[]
     * @return WasmResult indicating success or failure
     */
    default WasmResult setHttpResponseBody(byte[] body) {
        return WasmResult.UNIMPLEMENTED;
    }
}
