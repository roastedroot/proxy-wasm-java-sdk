package io.roastedroot.proxywasm;

public interface Handler {

    default void log(LogLevel level, String message) throws WasmException {}

    default LogLevel getLogLevel() throws WasmException {
        return LogLevel.TRACE;
    }

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

    default ProxyMap getHttpCallResponseHeaders() {
        return null;
    }

    default ProxyMap getHttpCallResponseTrailers() {
        return null;
    }

    default ProxyMap getGrpcReceiveInitialMetaData() {
        return null;
    }

    default ProxyMap getGrpcReceiveTrailerMetaData() {
        return null;
    }

    default ProxyMap getCustomHeaders(int mapType) {
        return null;
    }

    default String getProperty(String key) throws WasmException {
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
     * Get the downstream data.
     *
     * @return The downstream data as a byte[], or null if not available
     */
    default byte[] getDownStreamData() {
        return null;
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
     * Get the HTTP call response body.
     *
     * @return The HTTP call response body as a byte[], or null if not available
     */
    default byte[] getHttpCallResponseBody() {
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
     * Get the plugin configuration.
     *
     * @return The plugin configuration as a byte[], or null if not available
     */
    default byte[] getPluginConfig() {
        return null;
    }

    /**
     * Get the VM configuration.
     *
     * @return The VM configuration as a byte[], or null if not available
     */
    default byte[] getVmConfig() {
        return null;
    }

    /**
     * Get the function call data.
     *
     * @return The function call data as a byte[], or null if not available
     */
    default byte[] getFuncCallData() {
        return null;
    }

    /**
     * Get a custom buffer.
     *
     * @param bufferType The buffer type
     * @return The custom buffer as a byte[], or null if not available
     */
    default byte[] getCustomBuffer(int bufferType) {
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
     * When set, the host will call proxy_on_tick every tickPeriodMilliseconds milliseconds. Setting tickPeriodMilliseconds to 0 disables the timer.
     *
     * @return The current time in nanoseconds
     */
    default WasmResult setTickPeriodMilliseconds(int tickPeriodMilliseconds) {
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
     * Set the upstream data.
     *
     * @param data The upstream data as a byte[]
     * @return WasmResult indicating success or failure
     */
    default WasmResult setUpstreamData(byte[] data) {
        return WasmResult.UNIMPLEMENTED;
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

    /**
     * Set the gRPC receive buffer.
     *
     * @param buffer The gRPC receive buffer as a byte[]
     * @return WasmResult indicating success or failure
     */
    default WasmResult setGrpcReceiveBuffer(byte[] buffer) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the function call data.
     *
     * @param data The function call data as a byte[]
     * @return WasmResult indicating success or failure
     */
    default WasmResult setFuncCallData(byte[] data) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set a custom buffer.
     *
     * @param bufferType The buffer type
     * @param buffer     The custom buffer as a byte[]
     * @return WasmResult indicating success or failure
     */
    default WasmResult setCustomBuffer(int bufferType, byte[] buffer) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set a custom header map.
     *
     * @param mapType The type of map to set
     * @param map     The header map to set
     * @return WasmResult indicating success or failure
     */
    default WasmResult setCustomHeaders(int mapType, ProxyMap map) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the HTTP request headers.
     *
     * @param headers The headers to set
     * @return WasmResult indicating success or failure
     */
    default WasmResult setHttpRequestHeaders(ProxyMap headers) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the HTTP request trailers.
     *
     * @param trailers The trailers to set
     * @return WasmResult indicating success or failure
     */
    default WasmResult setHttpRequestTrailers(ProxyMap trailers) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the HTTP response headers.
     *
     * @param headers The headers to set
     * @return WasmResult indicating success or failure
     */
    default WasmResult setHttpResponseHeaders(ProxyMap headers) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the HTTP response trailers.
     *
     * @param trailers The trailers to set
     * @return WasmResult indicating success or failure
     */
    default WasmResult setHttpResponseTrailers(ProxyMap trailers) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the HTTP call response headers.
     *
     * @param headers The headers to set
     * @return WasmResult indicating success or failure
     */
    default WasmResult setHttpCallResponseHeaders(ProxyMap headers) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the HTTP call response trailers.
     *
     * @param trailers The trailers to set
     * @return WasmResult indicating success or failure
     */
    default WasmResult setHttpCallResponseTrailers(ProxyMap trailers) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the gRPC receive initial metadata.
     *
     * @param metadata The metadata to set
     * @return WasmResult indicating success or failure
     */
    default WasmResult setGrpcReceiveInitialMetaData(ProxyMap metadata) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Set the gRPC receive trailer metadata.
     *
     * @param metadata The metadata to set
     * @return WasmResult indicating success or failure
     */
    default WasmResult setGrpcReceiveTrailerMetaData(ProxyMap metadata) {
        return WasmResult.UNIMPLEMENTED;
    }

    default WasmResult setAction(StreamType streamType, Action action) {
        return WasmResult.UNIMPLEMENTED;
    }

    default WasmResult clearRouteCache() {
        return WasmResult.UNIMPLEMENTED;
    }

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

    default byte[] callForeignFunction(String name, byte[] bytes) throws WasmException {
        throw new WasmException(WasmResult.NOT_FOUND);
    }

    default int defineMetric(MetricType metricType, String name) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    default WasmResult removeMetric(int metricId) {
        return WasmResult.UNIMPLEMENTED;
    }

    default WasmResult recordMetric(int metricId, long value) {
        return WasmResult.UNIMPLEMENTED;
    }

    default WasmResult incrementMetric(int metricId, long value) {
        return WasmResult.UNIMPLEMENTED;
    }

    default long getMetric(int metricId) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    class SharedData {
        public byte[] data;
        public int cas;

        public SharedData(byte[] data, int cas) {
            this.data = data;
            this.cas = cas;
        }
    }

    default SharedData getSharedData(String key) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    default WasmResult setSharedData(String key, byte[] value, int cas) {
        return WasmResult.UNIMPLEMENTED;
    }

    default int registerSharedQueue(QueueName name) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    default int resolveSharedQueue(QueueName name) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    default byte[] dequeueSharedQueue(int queueId) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    default WasmResult enqueueSharedQueue(int queueId, byte[] value) {
        return WasmResult.UNIMPLEMENTED;
    }
}
