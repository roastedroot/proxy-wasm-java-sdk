package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.ChainedHandler;
import io.roastedroot.proxywasm.Handler;
import io.roastedroot.proxywasm.Helpers;
import io.roastedroot.proxywasm.LogLevel;
import io.roastedroot.proxywasm.MetricType;
import io.roastedroot.proxywasm.ProxyMap;
import io.roastedroot.proxywasm.StreamType;
import io.roastedroot.proxywasm.WasmException;
import io.roastedroot.proxywasm.WasmResult;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class JaxrsHandler extends ChainedHandler {

    private Handler next;

    public static class HttpResponse {

        public final int statusCode;
        public final byte[] statusCodeDetails;
        public final byte[] body;
        public final ProxyMap headers;
        public final int grpcStatus;

        public HttpResponse(
                int responseCode,
                byte[] responseCodeDetails,
                byte[] responseBody,
                ProxyMap additionalHeaders,
                int grpcStatus) {
            this.statusCode = responseCode;
            this.statusCodeDetails = responseCodeDetails;
            this.body = responseBody;
            this.headers = additionalHeaders;
            this.grpcStatus = grpcStatus;
        }
    }

    private int tickPeriodMilliseconds;
    private ProxyMap httpRequestHeaders = new JaxrsProxyMap();
    private ProxyMap httpRequestTrailers = new JaxrsProxyMap();
    private ProxyMap httpResponseHeaders = new JaxrsProxyMap();
    private ProxyMap httpResponseTrailers = new JaxrsProxyMap();
    private ProxyMap grpcReceiveInitialMetadata = new JaxrsProxyMap();
    private ProxyMap grpcReceiveTrailerMetadata = new JaxrsProxyMap();
    private HttpResponse senthttpResponse;

    private byte[] funcCallData = new byte[0];
    private byte[] httpRequestBody = new byte[0];
    private byte[] httpResponseBody = new byte[0];
    private byte[] downStreamData = new byte[0];
    private byte[] upstreamData = new byte[0];
    private byte[] grpcReceiveBuffer = new byte[0];

    static final boolean DEBUG = "true".equals(System.getenv("DEBUG"));

    JaxrsHandler() {
        this(new Handler() {});
    }

    JaxrsHandler(Handler next) {
        this.next = next;
    }

    @Override
    protected Handler next() {
        return next;
    }

    @Override
    public void log(LogLevel level, String message) throws WasmException {
        if (DEBUG) {
            System.out.println(level + ": " + message);
        }
    }

    @Override
    public WasmResult setTickPeriodMilliseconds(int tickPeriodMilliseconds) {
        this.tickPeriodMilliseconds = tickPeriodMilliseconds;
        return WasmResult.OK;
    }

    public int getTickPeriodMilliseconds() {
        return tickPeriodMilliseconds;
    }

    @Override
    public ProxyMap getHttpRequestHeaders() {
        return httpRequestHeaders;
    }

    @Override
    public ProxyMap getHttpRequestTrailers() {
        return httpRequestTrailers;
    }

    @Override
    public ProxyMap getHttpResponseHeaders() {
        return httpResponseHeaders;
    }

    @Override
    public ProxyMap getHttpResponseTrailers() {
        return httpResponseTrailers;
    }

    @Override
    public ProxyMap getGrpcReceiveInitialMetaData() {
        return grpcReceiveInitialMetadata;
    }

    @Override
    public ProxyMap getGrpcReceiveTrailerMetaData() {
        return grpcReceiveTrailerMetadata;
    }

    @Override
    public WasmResult setHttpRequestHeaders(ProxyMap headers) {
        this.httpRequestHeaders = headers;
        return WasmResult.OK;
    }

    public WasmResult setHttpRequestHeaders(MultivaluedMap<String, String> headers) {
        return this.setHttpRequestHeaders(new JaxrsProxyMap(headers));
    }

    @Override
    public WasmResult setHttpRequestTrailers(ProxyMap trailers) {
        this.httpRequestTrailers = trailers;
        return WasmResult.OK;
    }

    public WasmResult setHttpRequestTrailers(MultivaluedMap<String, String> headers) {
        return this.setHttpRequestTrailers(new JaxrsProxyMap(headers));
    }

    @Override
    public WasmResult setHttpResponseHeaders(ProxyMap headers) {
        this.httpResponseHeaders = headers;
        return WasmResult.OK;
    }

    public WasmResult setHttpResponseHeaders(MultivaluedMap<String, String> headers) {
        return this.setHttpResponseHeaders(new JaxrsProxyMap(headers));
    }

    @Override
    public WasmResult setHttpResponseTrailers(ProxyMap trailers) {
        this.httpResponseTrailers = trailers;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setGrpcReceiveInitialMetaData(ProxyMap metadata) {
        this.grpcReceiveInitialMetadata = metadata;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setGrpcReceiveTrailerMetaData(ProxyMap metadata) {
        this.grpcReceiveTrailerMetadata = metadata;
        return WasmResult.OK;
    }

    @Override
    public byte[] getFuncCallData() {
        return this.funcCallData;
    }

    @Override
    public byte[] getGrpcReceiveBuffer() {
        return this.grpcReceiveBuffer;
    }

    @Override
    public byte[] getUpstreamData() {
        return this.upstreamData;
    }

    @Override
    public byte[] getDownStreamData() {
        return this.downStreamData;
    }

    @Override
    public byte[] getHttpResponseBody() {
        return this.httpResponseBody;
    }

    @Override
    public byte[] getHttpRequestBody() {
        return this.httpRequestBody;
    }

    @Override
    public WasmResult setHttpRequestBody(byte[] body) {
        this.httpRequestBody = body;
        return WasmResult.OK;
    }

    public void appendHttpRequestBody(byte[] body) {
        this.httpRequestBody = Helpers.append(this.httpRequestBody, body);
    }

    @Override
    public WasmResult setHttpResponseBody(byte[] body) {
        this.httpResponseBody = body;
        return WasmResult.OK;
    }

    public void appendHttpResponseBody(byte[] body) {
        this.httpResponseBody = Helpers.append(this.httpResponseBody, body);
    }

    @Override
    public WasmResult setDownStreamData(byte[] data) {
        this.downStreamData = data;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setUpstreamData(byte[] data) {
        this.upstreamData = data;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setGrpcReceiveBuffer(byte[] buffer) {
        this.grpcReceiveBuffer = buffer;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setFuncCallData(byte[] data) {
        this.funcCallData = data;
        return WasmResult.OK;
    }

    @Override
    public WasmResult sendHttpResponse(
            int responseCode,
            byte[] responseCodeDetails,
            byte[] responseBody,
            ProxyMap additionalHeaders,
            int grpcStatus) {
        this.senthttpResponse =
                new HttpResponse(
                        responseCode,
                        responseCodeDetails,
                        responseBody,
                        additionalHeaders,
                        grpcStatus);
        return WasmResult.OK;
    }

    public HttpResponse getSentHttpResponse() {
        return senthttpResponse;
    }

    public static class HttpCall {
        public enum Type {
            REGULAR,
            DISPATCH
        }

        public final int id;
        public final Type callType;
        public final String uri;
        public final Object headers;
        public final byte[] body;
        public final ProxyMap trailers;
        public final int timeoutMilliseconds;

        public HttpCall(
                int id,
                Type callType,
                String uri,
                ProxyMap headers,
                byte[] body,
                ProxyMap trailers,
                int timeoutMilliseconds) {
            this.id = id;
            this.callType = callType;
            this.uri = uri;
            this.headers = headers;
            this.body = body;
            this.trailers = trailers;
            this.timeoutMilliseconds = timeoutMilliseconds;
        }
    }

    private final AtomicInteger lastCallId = new AtomicInteger(0);
    private final HashMap<Integer, HttpCall> httpCalls = new HashMap();

    public HashMap<Integer, HttpCall> getHttpCalls() {
        return httpCalls;
    }

    @Override
    public int httpCall(
            String uri, ProxyMap headers, byte[] body, ProxyMap trailers, int timeoutMilliseconds)
            throws WasmException {
        var id = lastCallId.incrementAndGet();
        HttpCall value =
                new HttpCall(
                        id,
                        HttpCall.Type.REGULAR,
                        uri,
                        headers,
                        body,
                        trailers,
                        timeoutMilliseconds);
        httpCalls.put(id, value);
        return id;
    }

    @Override
    public int dispatchHttpCall(
            String upstreamName,
            ProxyMap headers,
            byte[] body,
            ProxyMap trailers,
            int timeoutMilliseconds)
            throws WasmException {
        var id = lastCallId.incrementAndGet();
        HttpCall value =
                new HttpCall(
                        id,
                        HttpCall.Type.DISPATCH,
                        upstreamName,
                        headers,
                        body,
                        trailers,
                        timeoutMilliseconds);
        httpCalls.put(id, value);
        return id;
    }

    public static class Metric {

        public final int id;
        public final MetricType type;
        public final String name;
        public long value;

        public Metric(int id, MetricType type, String name) {
            this.id = id;
            this.type = type;
            this.name = name;
        }
    }

    private final AtomicInteger lastMetricId = new AtomicInteger(0);
    private HashMap<Integer, Metric> metrics = new HashMap();
    private HashMap<String, Metric> metricsByName = new HashMap();

    @Override
    public int defineMetric(MetricType type, String name) throws WasmException {
        var id = lastMetricId.incrementAndGet();
        Metric value = new Metric(id, type, name);
        metrics.put(id, value);
        metricsByName.put(name, value);
        return id;
    }

    @Override
    public long getMetric(int metricId) throws WasmException {
        var metric = metrics.get(metricId);
        if (metric == null) {
            throw new WasmException(WasmResult.NOT_FOUND);
        }
        return metric.value;
    }

    public Metric getMetric(String name) {
        return metricsByName.get(name);
    }

    @Override
    public WasmResult incrementMetric(int metricId, long value) {
        var metric = metrics.get(metricId);
        if (metric == null) {
            return WasmResult.NOT_FOUND;
        }
        metric.value += value;
        return WasmResult.OK;
    }

    @Override
    public WasmResult recordMetric(int metricId, long value) {
        var metric = metrics.get(metricId);
        if (metric == null) {
            return WasmResult.NOT_FOUND;
        }
        metric.value = value;
        return WasmResult.OK;
    }

    @Override
    public WasmResult removeMetric(int metricId) {
        Metric metric = metrics.remove(metricId);
        if (metric == null) {
            return WasmResult.NOT_FOUND;
        }
        metricsByName.remove(metric.name);
        return WasmResult.OK;
    }

    private Action action;

    @Override
    public WasmResult setAction(StreamType streamType, Action action) {
        this.action = action;
        return WasmResult.OK;
    }

    public Action getAction() {
        return action;
    }

    final HashMap<List<String>, byte[]> properties = new HashMap<>();

    @Override
    public byte[] getProperty(List<String> path) throws WasmException {
        return properties.get(path);
    }

    @Override
    public WasmResult setProperty(List<String> path, byte[] value) {
        properties.put(path, value);
        return WasmResult.OK;
    }
}
