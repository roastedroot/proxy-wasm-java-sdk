package io.roastedroot.proxywasm.internal;

public interface StreamContextHandler {

    /**
     * Send an HTTP response.
     *
     * @param responseCode        The HTTP response code
     * @param responseCodeDetails The response code details
     * @param responseBody        The response body
     * @param additionalHeaders   Additional headers to include
     * @param grpcStatus          The gRPC status code (-1 for non-gRPC responses)
     * @return The result of sending the response
     */
    default WasmResult sendHttpResponse(
            int responseCode,
            byte[] responseCodeDetails,
            byte[] responseBody,
            ProxyMap additionalHeaders,
            int grpcStatus) {
        return WasmResult.UNIMPLEMENTED;
    }

    default WasmResult setAction(StreamType streamType, Action action) {
        return WasmResult.UNIMPLEMENTED;
    }

    default WasmResult clearRouteCache() {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Get the downstream data.
     *
     * @return The downstream data as a byte[], or null if not available
     */
    default byte[] getDownStreamData() {
        return null;
    }

    /**
     * Set the downstream data.
     *
     * @param data The downstream data as a byte[]
     * @return WasmResult indicating success or failure
     */
    default WasmResult setDownStreamData(byte[] data) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Get the upstream data.
     *
     * @return The upstream data as a byte[], or null if not available
     */
    default byte[] getUpstreamData() {
        return null;
    }

    /**
     * Set the upstream data.
     *
     * @param data The upstream data as a byte[]
     * @return WasmResult indicating success or failure
     */
    default WasmResult setUpstreamData(byte[] data) {
        return WasmResult.UNIMPLEMENTED;
    }
}
