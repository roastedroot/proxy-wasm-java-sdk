package io.roastedroot.proxywasm;

public interface GRPCContextHandler extends StreamContextHandler {

    default ProxyMap getGrpcReceiveInitialMetaData() {
        return null;
    }

    default ProxyMap getGrpcReceiveTrailerMetaData() {
        return null;
    }

    /**
     * Get the gRPC receive buffer.
     *
     * @return The gRPC receive buffer as a byte[], or null if not available
     */
    default byte[] getGrpcReceiveBuffer() {
        return null;
    }

    /**
     * Set the gRPC receive buffer.
     *
     * @param buffer The gRPC receive buffer as a byte[]
     * @return WasmResult indicating success or failure
     */
    default WasmResult setGrpcReceiveBuffer(byte[] buffer) {
        return WasmResult.UNIMPLEMENTED;
    }

    default int grpcCall(
            String upstreamName,
            String serviceName,
            String methodName,
            ProxyMap initialMetadata,
            byte[] message,
            int timeout)
            throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    default int grpcStream(
            String upstreamName, String serviceName, String methodName, ProxyMap initialMetadata)
            throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    default WasmResult grpcSend(int streamId, byte[] message, int endStream) {
        return WasmResult.UNIMPLEMENTED;
    }

    default WasmResult grpcCancel(int callOrstreamId) {
        return WasmResult.UNIMPLEMENTED;
    }

    default WasmResult grpcClose(int callOrstreamId) {
        return WasmResult.UNIMPLEMENTED;
    }
}
