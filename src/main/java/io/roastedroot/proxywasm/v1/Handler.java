package io.roastedroot.proxywasm.v1;

import java.nio.ByteBuffer;
import java.util.Map;

public interface Handler {

    default void log(LogLevel level, String message) throws WasmException {}

    default Map<String, String> getHttpRequestHeader() {
        return null;
    }

    default Map<String, String> getHttpRequestTrailer() {
        return null;
    }

    default Map<String, String> getHttpResponseHeader() {
        return null;
    }

    default Map<String, String> getHttpResponseTrailer() {
        return null;
    }

    default Map<String, String> getHttpCallResponseHeaders() {
        return null;
    }

    default Map<String, String> getHttpCallResponseTrailer() {
        return null;
    }

    default Map<String, String> getGrpcReceiveInitialMetaData() {
        return null;
    }

    default Map<String, String> getGrpcReceiveTrailerMetaData() {
        return null;
    }

    default Map<String, String> getCustomHeader(int mapType) {
        return null;
    }

    default String getProperty(String key) throws WasmException {
        return null;
    }

    /**
     * Get the HTTP request body.
     *
     * @return The HTTP request body as a ByteBuffer, or null if not available
     */
    default ByteBuffer getHttpRequestBody() {
        return null;
    }

    /**
     * Get the HTTP response body.
     *
     * @return The HTTP response body as a ByteBuffer, or null if not available
     */
    default ByteBuffer getHttpResponseBody() {
        return null;
    }

    /**
     * Get the downstream data.
     *
     * @return The downstream data as a ByteBuffer, or null if not available
     */
    default ByteBuffer getDownStreamData() {
        return null;
    }

    /**
     * Get the upstream data.
     *
     * @return The upstream data as a ByteBuffer, or null if not available
     */
    default ByteBuffer getUpstreamData() {
        return null;
    }

    /**
     * Get the HTTP call response body.
     *
     * @return The HTTP call response body as a ByteBuffer, or null if not available
     */
    default ByteBuffer getHttpCallResponseBody() {
        return null;
    }

    /**
     * Get the gRPC receive buffer.
     *
     * @return The gRPC receive buffer as a ByteBuffer, or null if not available
     */
    default ByteBuffer getGrpcReceiveBuffer() {
        return null;
    }

    /**
     * Get the plugin configuration.
     *
     * @return The plugin configuration as a ByteBuffer, or null if not available
     */
    default ByteBuffer getPluginConfig() {
        return null;
    }

    /**
     * Get the VM configuration.
     *
     * @return The VM configuration as a ByteBuffer, or null if not available
     */
    default ByteBuffer getVmConfig() {
        return null;
    }

    /**
     * Get the function call data.
     *
     * @return The function call data as a ByteBuffer, or null if not available
     */
    default ByteBuffer getFuncCallData() {
        return null;
    }

    /**
     * Get a custom buffer.
     *
     * @param bufferType The buffer type
     * @return The custom buffer as a ByteBuffer, or null if not available
     */
    default ByteBuffer getCustomBuffer(int bufferType) {
        return null;
    }

    /**
     * Set the effective context ID.
     *
     * @param contextID The context ID
     * @return The result of the operation
     */
    default WasmResult setEffectiveContextID(int contextID) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Indicates to the host that the plugin is done processing active context.
     *
     * @return The result of the operation
     */
    default WasmResult done() {
        return WasmResult.NOT_FOUND;
    }

    /**
     * Sets a low-resolution timer period (tick_period).
     *
     * When set, the host will call proxy_on_tick every tick_period milliseconds. Setting tick_period to 0 disables the timer.
     *
     * @return The current time in nanoseconds
     */
    default WasmResult setTickPeriodMilliseconds(int tick_period) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Retrieves current time  or the approximation of it.
     *
     * Note Hosts might return approximate time (e.g. frozen at the context creation) to improve performance and/or prevent various attacks.
     *
     * @return The current time in nanoseconds
     */
    default int getCurrentTimeNanoseconds() throws WasmException {
        return (int) System.currentTimeMillis() * 1000000;
    }

    /**
     * Send an HTTP response.
     *
     * @param responseCode The HTTP response code
     * @param responseCodeDetails The response code details
     * @param responseBody The response body
     * @param additionalHeaders Additional headers to include
     * @param grpcStatus The gRPC status code (-1 for non-gRPC responses)
     * @return The result of sending the response
     */
    default WasmResult sendHttpResp(
            int responseCode,
            ByteBuffer responseCodeDetails,
            ByteBuffer responseBody,
            Map<String, String> additionalHeaders,
            int grpcStatus) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the HTTP request body.
     *
     * @param body The HTTP request body as a ByteBuffer
     * @return WasmResult indicating success or failure
     */
    default WasmResult setHttpRequestBody(ByteBuffer body) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the HTTP response body.
     *
     * @param body The HTTP response body as a ByteBuffer
     * @return WasmResult indicating success or failure
     */
    default WasmResult setHttpResponseBody(ByteBuffer body) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the downstream data.
     *
     * @param data The downstream data as a ByteBuffer
     * @return WasmResult indicating success or failure
     */
    default WasmResult setDownStreamData(ByteBuffer data) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the upstream data.
     *
     * @param data The upstream data as a ByteBuffer
     * @return WasmResult indicating success or failure
     */
    default WasmResult setUpstreamData(ByteBuffer data) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the HTTP call response body.
     *
     * @param body The HTTP call response body as a ByteBuffer
     * @return WasmResult indicating success or failure
     */
    default WasmResult setHttpCallResponseBody(ByteBuffer body) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the gRPC receive buffer.
     *
     * @param buffer The gRPC receive buffer as a ByteBuffer
     * @return WasmResult indicating success or failure
     */
    default WasmResult setGrpcReceiveBuffer(ByteBuffer buffer) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the function call data.
     *
     * @param data The function call data as a ByteBuffer
     * @return WasmResult indicating success or failure
     */
    default WasmResult setFuncCallData(ByteBuffer data) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set a custom buffer.
     *
     * @param bufferType The buffer type
     * @param buffer The custom buffer as a ByteBuffer
     * @return WasmResult indicating success or failure
     */
    default WasmResult setCustomBuffer(int bufferType, ByteBuffer buffer) {
        return WasmResult.UNIMPLEMENTED;
    }
}
