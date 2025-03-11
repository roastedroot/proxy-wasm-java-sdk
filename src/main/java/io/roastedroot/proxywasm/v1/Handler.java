package io.roastedroot.proxywasm.v1;

import java.nio.ByteBuffer;
import java.util.Map;

public interface Handler {

    void log(LogLevel level, String message) throws WasmException;

    Map<String, String> getHttpRequestHeader() ;

    Map<String, String> getHttpRequestTrailer() ;

    Map<String, String> getHttpResponseHeader() ;

    Map<String, String> getHttpResponseTrailer() ;

    Map<String, String> getHttpCallResponseHeaders();

    Map<String, String> getHttpCallResponseTrailer() ;

    Map<String, String> getGrpcReceiveInitialMetaData() ;

    Map<String, String> getGrpcReceiveTrailerMetaData() ;

    Map<String, String> getCustomHeader(int mapType);

    String getProperty(String key) throws WasmException;

    /**
     * Get the HTTP request body.
     *
     * @return The HTTP request body as a ByteBuffer, or null if not available
     */
    ByteBuffer getHttpRequestBody();

    /**
     * Get the HTTP response body.
     *
     * @return The HTTP response body as a ByteBuffer, or null if not available
     */
    ByteBuffer getHttpResponseBody();

    /**
     * Get the downstream data.
     *
     * @return The downstream data as a ByteBuffer, or null if not available
     */
    ByteBuffer getDownStreamData();

    /**
     * Get the upstream data.
     *
     * @return The upstream data as a ByteBuffer, or null if not available
     */
    ByteBuffer getUpstreamData();

    /**
     * Get the HTTP call response body.
     *
     * @return The HTTP call response body as a ByteBuffer, or null if not available
     */
    ByteBuffer getHttpCallResponseBody();

    /**
     * Get the gRPC receive buffer.
     *
     * @return The gRPC receive buffer as a ByteBuffer, or null if not available
     */
    ByteBuffer getGrpcReceiveBuffer();

    /**
     * Get the plugin configuration.
     *
     * @return The plugin configuration as a ByteBuffer, or null if not available
     */
    ByteBuffer getPluginConfig();

    /**
     * Get the VM configuration.
     *
     * @return The VM configuration as a ByteBuffer, or null if not available
     */
    ByteBuffer getVmConfig();

    /**
     * Get the function call data.
     *
     * @return The function call data as a ByteBuffer, or null if not available
     */
    ByteBuffer getFuncCallData();

    /**
     * Get a custom buffer.
     *
     * @param bufferType The buffer type
     * @return The custom buffer as a ByteBuffer, or null if not available
     */
    ByteBuffer getCustomBuffer(int bufferType);

    /**
     * Set the effective context ID.
     *
     * @param contextID The context ID
     * @return The result of the operation
     */
    WasmResult setEffectiveContextID(int contextID);

    /**
     * Indicates to the host that the plugin is done processing active context.
     *
     * @return The result of the operation
     */
    WasmResult done();

    /**
     * Sets a low-resolution timer period (tick_period).
     *
     * When set, the host will call proxy_on_tick every tick_period milliseconds. Setting tick_period to 0 disables the timer.
     *
     * @return The current time in nanoseconds
     */
    WasmResult setTickPeriodMilliseconds(int tick_period);

    /**
     * Retrieves current time  or the approximation of it.
     *
     * Note Hosts might return approximate time (e.g. frozen at the context creation) to improve performance and/or prevent various attacks.
     *
     * @return The current time in nanoseconds
     */
    int getCurrentTimeNanoseconds() throws WasmException;
}
