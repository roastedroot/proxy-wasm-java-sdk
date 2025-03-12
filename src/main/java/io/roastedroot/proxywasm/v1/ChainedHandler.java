package io.roastedroot.proxywasm.v1;

import java.util.Map;

/**
 * A Handler implementation that chains to another handler if it can't handle the request.
 */
public abstract class ChainedHandler implements Handler {

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
    public WasmResult setCustomHeader(int mapType, Map<String, String> map) {
        return next().setCustomHeader(mapType, map);
    }

    @Override
    public WasmResult setHttpRequestHeader(Map<String, String> headers) {
        return next().setHttpRequestHeader(headers);
    }

    @Override
    public WasmResult setHttpRequestTrailer(Map<String, String> trailers) {
        return next().setHttpRequestTrailer(trailers);
    }

    @Override
    public WasmResult setHttpResponseHeader(Map<String, String> headers) {
        return next().setHttpResponseHeader(headers);
    }

    @Override
    public WasmResult setHttpResponseTrailer(Map<String, String> trailers) {
        return next().setHttpResponseTrailer(trailers);
    }

    @Override
    public WasmResult setHttpCallResponseHeaders(Map<String, String> headers) {
        return next().setHttpCallResponseHeaders(headers);
    }

    @Override
    public WasmResult setHttpCallResponseTrailer(Map<String, String> trailers) {
        return next().setHttpCallResponseTrailer(trailers);
    }

    @Override
    public WasmResult setGrpcReceiveInitialMetaData(Map<String, String> metadata) {
        return next().setGrpcReceiveInitialMetaData(metadata);
    }

    @Override
    public WasmResult setGrpcReceiveTrailerMetaData(Map<String, String> metadata) {
        return next().setGrpcReceiveTrailerMetaData(metadata);
    }

    @Override
    public String getProperty(String key) throws WasmException {
        return next().getProperty(key);
    }

    @Override
    public byte[] getHttpRequestBody() {
        return next().getHttpRequestBody();
    }

    @Override
    public byte[] getHttpResponseBody() {
        return next().getHttpResponseBody();
    }

    @Override
    public byte[] getDownStreamData() {
        return next().getDownStreamData();
    }

    @Override
    public byte[] getUpstreamData() {
        return next().getUpstreamData();
    }

    @Override
    public byte[] getHttpCallResponseBody() {
        return next().getHttpCallResponseBody();
    }

    @Override
    public byte[] getGrpcReceiveBuffer() {
        return next().getGrpcReceiveBuffer();
    }

    @Override
    public byte[] getPluginConfig() {
        return next().getPluginConfig();
    }

    @Override
    public byte[] getVmConfig() {
        return next().getVmConfig();
    }

    @Override
    public byte[] getFuncCallData() {
        return next().getFuncCallData();
    }

    @Override
    public byte[] getCustomBuffer(int bufferType) {
        return next().getCustomBuffer(bufferType);
    }

    @Override
    public WasmResult setEffectiveContextID(int contextID) {
        return next().setEffectiveContextID(contextID);
    }

    @Override
    public WasmResult done() {
        return next().done();
    }

    @Override
    public WasmResult setTickPeriodMilliseconds(int tick_period) {
        return next().setTickPeriodMilliseconds(tick_period);
    }

    @Override
    public int getCurrentTimeNanoseconds() throws WasmException {
        return next().getCurrentTimeNanoseconds();
    }

    @Override
    public WasmResult sendHttpResponse(
            int responseCode,
            byte[] responseCodeDetails,
            byte[] responseBody,
            Map<String, String> additionalHeaders,
            int grpcStatus) {
        return next().sendHttpResponse(
                        responseCode,
                        responseCodeDetails,
                        responseBody,
                        additionalHeaders,
                        grpcStatus);
    }

    @Override
    public WasmResult setCustomBuffer(int bufferType, byte[] buffer) {
        return next().setCustomBuffer(bufferType, buffer);
    }

    @Override
    public WasmResult setFuncCallData(byte[] data) {
        return next().setFuncCallData(data);
    }

    @Override
    public WasmResult setGrpcReceiveBuffer(byte[] buffer) {
        return next().setGrpcReceiveBuffer(buffer);
    }

    @Override
    public WasmResult setHttpCallResponseBody(byte[] body) {
        return next().setHttpCallResponseBody(body);
    }

    @Override
    public WasmResult setUpstreamData(byte[] data) {
        return next().setUpstreamData(data);
    }

    @Override
    public WasmResult setDownStreamData(byte[] data) {
        return next().setDownStreamData(data);
    }

    @Override
    public WasmResult setHttpResponseBody(byte[] body) {
        return next().setHttpResponseBody(body);
    }

    @Override
    public WasmResult setHttpRequestBody(byte[] body) {
        return next().setHttpRequestBody(body);
    }
}
