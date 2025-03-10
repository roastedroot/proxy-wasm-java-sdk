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
}
