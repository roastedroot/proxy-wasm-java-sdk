package io.roastedroot.proxywasm.internal;

import io.roastedroot.proxywasm.ForeignFunction;
import io.roastedroot.proxywasm.LogLevel;
import io.roastedroot.proxywasm.MetricType;
import io.roastedroot.proxywasm.QueueName;
import io.roastedroot.proxywasm.SharedData;
import io.roastedroot.proxywasm.WasmException;
import java.util.List;

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
    public ProxyMap getHttpRequestHeaders() {
        return next().getHttpRequestHeaders();
    }

    @Override
    public ProxyMap getHttpRequestTrailers() {
        return next().getHttpRequestTrailers();
    }

    @Override
    public ProxyMap getHttpResponseHeaders() {
        return next().getHttpResponseHeaders();
    }

    @Override
    public ProxyMap getHttpResponseTrailers() {
        return next().getHttpResponseTrailers();
    }

    @Override
    public ProxyMap getHttpCallResponseHeaders() {
        return next().getHttpCallResponseHeaders();
    }

    @Override
    public ProxyMap getHttpCallResponseTrailers() {
        return next().getHttpCallResponseTrailers();
    }

    @Override
    public ProxyMap getGrpcReceiveInitialMetaData() {
        return next().getGrpcReceiveInitialMetaData();
    }

    @Override
    public ProxyMap getGrpcReceiveTrailerMetaData() {
        return next().getGrpcReceiveTrailerMetaData();
    }

    @Override
    public ProxyMap getCustomHeaders(int mapType) {
        return next().getCustomHeaders(mapType);
    }

    @Override
    public WasmResult setCustomHeaders(int mapType, ProxyMap map) {
        return next().setCustomHeaders(mapType, map);
    }

    @Override
    public byte[] getProperty(List<String> key) throws WasmException {
        return next().getProperty(key);
    }

    @Override
    public WasmResult setProperty(List<String> path, byte[] value) {
        return next().setProperty(path, value);
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
            ProxyMap additionalHeaders,
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
    public WasmResult setAction(StreamType streamType, Action action) {
        return next().setAction(streamType, action);
    }

    @Override
    public WasmResult clearRouteCache() {
        return next().clearRouteCache();
    }

    @Override
    public int httpCall(String uri, ProxyMap headers, byte[] body, ProxyMap trailers, int timeout)
            throws WasmException {
        return next().httpCall(uri, headers, body, trailers, timeout);
    }

    @Override
    public int dispatchHttpCall(
            String upstreamName,
            ProxyMap headers,
            byte[] body,
            ProxyMap trailers,
            int timeoutMilliseconds)
            throws WasmException {
        return next().dispatchHttpCall(upstreamName, headers, body, trailers, timeoutMilliseconds);
    }

    @Override
    public ForeignFunction getForeignFunction(String name) {
        return next().getForeignFunction(name);
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

    @Override
    public SharedData getSharedData(String key) throws WasmException {
        return next().getSharedData(key);
    }

    @Override
    public WasmResult setSharedData(String key, byte[] value, int cas) {
        return next().setSharedData(key, value, cas);
    }

    @Override
    public int registerSharedQueue(QueueName queueName) throws WasmException {
        return next().registerSharedQueue(queueName);
    }

    @Override
    public int resolveSharedQueue(QueueName queueName) throws WasmException {
        return next().resolveSharedQueue(queueName);
    }

    @Override
    public byte[] dequeueSharedQueue(int queueId) throws WasmException {
        return next().dequeueSharedQueue(queueId);
    }

    @Override
    public WasmResult enqueueSharedQueue(int queueId, byte[] value) {
        return next().enqueueSharedQueue(queueId, value);
    }

    @Override
    public LogLevel getLogLevel() throws WasmException {
        return next().getLogLevel();
    }

    @Override
    public int grpcCall(
            String upstreamName,
            String serviceName,
            String methodName,
            ProxyMap initialMetadata,
            byte[] message,
            int timeout)
            throws WasmException {
        return next().grpcCall(
                        upstreamName, serviceName, methodName, initialMetadata, message, timeout);
    }

    @Override
    public int grpcStream(
            String upstreamName, String serviceName, String methodName, ProxyMap initialMetadata)
            throws WasmException {
        return next().grpcStream(upstreamName, serviceName, methodName, initialMetadata);
    }

    @Override
    public WasmResult grpcSend(int streamId, byte[] message, int endStream) {
        return next().grpcSend(streamId, message, endStream);
    }

    @Override
    public WasmResult grpcCancel(int callOrstreamId) {
        return next().grpcCancel(callOrstreamId);
    }

    @Override
    public WasmResult grpcClose(int callOrstreamId) {
        return next().grpcClose(callOrstreamId);
    }
}
