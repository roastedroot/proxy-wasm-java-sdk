package io.roastedroot.proxywasm.v1;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;

/**
 * A Handler implementation that chains to another handler if it can't handle the request.
 */
public abstract class AbstractChainedHandler implements Handler {

    protected abstract Handler next();

    @Override
    public void log(LogLevel level, String message) throws WasmException {
        next().log(level, message);
    }

    @Override
    public Map<String, String> getHttpRequestHeader() {
        return next().getHttpRequestHeader();
    }

    @Override
    public Map<String, String> getHttpRequestTrailer() {
        return next().getHttpRequestTrailer();
    }

    @Override
    public Map<String, String> getHttpResponseHeader() {
        return next().getHttpResponseHeader();
    }

    @Override
    public Map<String, String> getHttpResponseTrailer() {
        return next().getHttpResponseTrailer();
    }

    @Override
    public Map<String, String> getHttpCallResponseHeaders() {
        return next().getHttpCallResponseHeaders();
    }

    @Override
    public Map<String, String> getHttpCallResponseTrailer() {
        return next().getHttpCallResponseTrailer();
    }

    @Override
    public Map<String, String> getGrpcReceiveInitialMetaData() {
        return next().getGrpcReceiveInitialMetaData();
    }

    @Override
    public Map<String, String> getGrpcReceiveTrailerMetaData() {
        return next().getGrpcReceiveTrailerMetaData();
    }

    @Override
    public Map<String, String> getCustomHeader(int mapType) {
        return next().getCustomHeader(mapType);
    }

    @Override
    public String getProperty(String key) throws WasmException {
        return next().getProperty(key);
    }

    @Override
    public ByteBuffer getHttpRequestBody() {
        return next().getHttpRequestBody();
    }

    @Override
    public ByteBuffer getHttpResponseBody() {
        return next().getHttpResponseBody();
    }

    @Override
    public ByteBuffer getDownStreamData() {
        return next().getDownStreamData();
    }

    @Override
    public ByteBuffer getUpstreamData() {
        return next().getUpstreamData();
    }

    @Override
    public ByteBuffer getHttpCallResponseBody() {
        return next().getHttpCallResponseBody();
    }

    @Override
    public ByteBuffer getGrpcReceiveBuffer() {
        return next().getGrpcReceiveBuffer();
    }

    @Override
    public ByteBuffer getPluginConfig() {
        return next().getPluginConfig();
    }

    @Override
    public ByteBuffer getVmConfig() {
        return next().getVmConfig();
    }

    @Override
    public ByteBuffer getFuncCallData() {
        return next().getFuncCallData();
    }

    @Override
    public ByteBuffer getCustomBuffer(int bufferType) {
        return next().getCustomBuffer(bufferType);
    }

    @Override
    public WasmResult setEffectiveContextID(int contextID) {
        return next().setEffectiveContextID(contextID);
    }

}