package io.roastedroot.proxywasm.v1;

import java.util.HashMap;
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
    public Map<String, String> getHttpRequestHeaders() {
        return next().getHttpRequestHeaders();
    }

    @Override
    public Map<String, String> getHttpRequestTrailers() {
        return next().getHttpRequestTrailers();
    }

    @Override
    public Map<String, String> getHttpResponseHeaders() {
        return next().getHttpResponseHeaders();
    }

    @Override
    public Map<String, String> getHttpResponseTrailers() {
        return next().getHttpResponseTrailers();
    }

    @Override
    public Map<String, String> getHttpCallResponseHeaders() {
        return next().getHttpCallResponseHeaders();
    }

    @Override
    public Map<String, String> getHttpCallResponseTrailers() {
        return next().getHttpCallResponseTrailers();
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
    public Map<String, String> getCustomHeaders(int mapType) {
        return next().getCustomHeaders(mapType);
    }

    @Override
    public WasmResult setCustomHeaders(int mapType, Map<String, String> map) {
        return next().setCustomHeaders(mapType, map);
    }

    @Override
    public WasmResult setHttpRequestHeaders(Map<String, String> headers) {
        return next().setHttpRequestHeaders(headers);
    }

    @Override
    public WasmResult setHttpRequestTrailers(Map<String, String> trailers) {
        return next().setHttpRequestTrailers(trailers);
    }

    @Override
    public WasmResult setHttpResponseHeaders(Map<String, String> headers) {
        return next().setHttpResponseHeaders(headers);
    }

    @Override
    public WasmResult setHttpResponseTrailers(Map<String, String> trailers) {
        return next().setHttpResponseTrailers(trailers);
    }

    @Override
    public WasmResult setHttpCallResponseHeaders(Map<String, String> headers) {
        return next().setHttpCallResponseHeaders(headers);
    }

    @Override
    public WasmResult setHttpCallResponseTrailers(Map<String, String> trailers) {
        return next().setHttpCallResponseTrailers(trailers);
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

    @Override
    public WasmResult continueRequest() {
        return next().continueRequest();
    }

    @Override
    public WasmResult continueResponse() {
        return next().continueResponse();
    }

    @Override
    public WasmResult continueDownstream() {
        return next().continueDownstream();
    }

    @Override
    public WasmResult continueUpstream() {
        return next().continueUpstream();
    }

    @Override
    public int httpCall(
            String uri,
            HashMap<String, String> headers,
            byte[] body,
            HashMap<String, String> trailers,
            int timeout)
            throws WasmException {
        return next().httpCall(uri, headers, body, trailers, timeout);
    }

    @Override
    public int dispatchHttpCall(
            String upstreamName,
            HashMap<String, String> headers,
            byte[] body,
            HashMap<String, String> trailers,
            int timeoutMilliseconds)
            throws WasmException {
        return next().dispatchHttpCall(upstreamName, headers, body, trailers, timeoutMilliseconds);
    }

    @Override
    public byte[] callForeignFunction(String name, byte[] bytes) throws WasmException {
        return next().callForeignFunction(name, bytes);
    }

    @Override
    public int defineMetric(MetricType metricType, String name) throws WasmException {
        return next().defineMetric(metricType, name);
    }

    @Override
    public WasmResult removeMetric(int metricId) {
        return next().removeMetric(metricId);
    }

    @Override
    public WasmResult recordMetric(int metricId, long value) {
        return next().recordMetric(metricId, value);
    }

    @Override
    public WasmResult incrementMetric(int metricId, long value) {
        return next().incrementMetric(metricId, value);
    }

    @Override
    public long getMetric(int metricId) throws WasmException {
        return next().getMetric(metricId);
    }
}
