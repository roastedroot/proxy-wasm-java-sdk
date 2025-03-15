package io.roastedroot.proxywasm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.roastedroot.proxywasm.v1.Action;
import io.roastedroot.proxywasm.v1.Handler;
import io.roastedroot.proxywasm.v1.Helpers;
import io.roastedroot.proxywasm.v1.LogLevel;
import io.roastedroot.proxywasm.v1.MetricType;
import io.roastedroot.proxywasm.v1.StreamType;
import io.roastedroot.proxywasm.v1.WasmException;
import io.roastedroot.proxywasm.v1.WasmResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MockHandler implements Handler {

    public static class HttpResponse {

        public final int statusCode;
        public final byte[] statusCodeDetails;
        public final byte[] body;
        public final Map<String, String> headers;
        public final int grpcStatus;

        public HttpResponse(
                int responseCode,
                byte[] responseCodeDetails,
                byte[] responseBody,
                Map<String, String> additionalHeaders,
                int grpcStatus) {
            this.statusCode = responseCode;
            this.statusCodeDetails = responseCodeDetails;
            this.body = responseBody;
            this.headers = additionalHeaders;
            this.grpcStatus = grpcStatus;
        }
    }

    final ArrayList<String> loggedMessages = new ArrayList<>();

    private int tickPeriodMilliseconds;
    private Map<String, String> httpRequestHeaders = new HashMap<>();
    private Map<String, String> httpRequestTrailers = new HashMap<>();
    private Map<String, String> httpResponseHeaders = new HashMap<>();
    private Map<String, String> httpResponseTrailers = new HashMap<>();
    private Map<String, String> grpcReceiveInitialMetadata = new HashMap<>();
    private Map<String, String> grpcReceiveTrailerMetadata = new HashMap<>();
    private HttpResponse senthttpResponse;

    private byte[] funcCallData = new byte[0];
    private byte[] httpRequestBody = new byte[0];
    private byte[] httpResponseBody = new byte[0];
    private byte[] downStreamData = new byte[0];
    private byte[] upstreamData = new byte[0];
    private byte[] grpcReceiveBuffer = new byte[0];

    static final boolean DEBUG = "true".equals(System.getenv("DEBUG"));

    @Override
    public void log(LogLevel level, String message) throws WasmException {
        if (DEBUG) {
            System.out.println(level + ": " + message);
        }
        loggedMessages.add(message);
    }

    public ArrayList<String> loggedMessages() {
        return loggedMessages;
    }

    public void assertLogsEqual(String... messages) {
        assertEquals(List.of(messages), loggedMessages());
    }

    public void assertSortedLogsEqual(String... messages) {
        assertEquals(
                Stream.of(messages).sorted().collect(Collectors.toList()),
                loggedMessages().stream().sorted().collect(Collectors.toList()));
    }

    public void assertLogsContain(String... message) {
        for (String m : message) {
            assertTrue(loggedMessages().contains(m), "logged messages does not contain: " + m);
        }
    }

    public void assertLogsDoNotContain(String... message) {
        for (String log : loggedMessages()) {
            for (String m : message) {
                assertFalse(log.contains(m), "logged messages contains: " + m);
            }
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
    public Map<String, String> getHttpRequestHeaders() {
        return httpRequestHeaders;
    }

    @Override
    public Map<String, String> getHttpRequestTrailers() {
        return httpRequestTrailers;
    }

    @Override
    public Map<String, String> getHttpResponseHeaders() {
        return httpResponseHeaders;
    }

    @Override
    public Map<String, String> getHttpResponseTrailers() {
        return httpResponseTrailers;
    }

    @Override
    public Map<String, String> getGrpcReceiveInitialMetaData() {
        return grpcReceiveInitialMetadata;
    }

    @Override
    public Map<String, String> getGrpcReceiveTrailerMetaData() {
        return grpcReceiveTrailerMetadata;
    }

    @Override
    public WasmResult setHttpRequestHeaders(Map<String, String> headers) {
        this.httpRequestHeaders = headers;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setHttpRequestTrailers(Map<String, String> trailers) {
        this.httpRequestTrailers = trailers;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setHttpResponseHeaders(Map<String, String> headers) {
        this.httpResponseHeaders = headers;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setHttpResponseTrailers(Map<String, String> trailers) {
        this.httpResponseTrailers = trailers;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setGrpcReceiveInitialMetaData(Map<String, String> metadata) {
        this.grpcReceiveInitialMetadata = metadata;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setGrpcReceiveTrailerMetaData(Map<String, String> metadata) {
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
            Map<String, String> additionalHeaders,
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
        public final HashMap<String, String> trailers;
        public final int timeoutMilliseconds;

        public HttpCall(
                int id,
                Type callType,
                String uri,
                HashMap<String, String> headers,
                byte[] body,
                HashMap<String, String> trailers,
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
    private final HashMap<Integer, HttpCall> httpCalls = new HashMap<>();

    public HashMap<Integer, HttpCall> getHttpCalls() {
        return httpCalls;
    }

    @Override
    public int httpCall(
            String uri,
            HashMap<String, String> headers,
            byte[] body,
            HashMap<String, String> trailers,
            int timeoutMilliseconds)
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
            HashMap<String, String> headers,
            byte[] body,
            HashMap<String, String> trailers,
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
    private HashMap<Integer, Metric> metrics = new HashMap<>();
    private HashMap<String, Metric> metricsByName = new HashMap<>();

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

    private final HashMap<String, SharedData> sharedData = new HashMap<>();

    @Override
    public SharedData getSharedData(String key) throws WasmException {
        return sharedData.get(key);
    }

    @Override
    public WasmResult setSharedData(String key, byte[] value, int cas) {
        SharedData prev = sharedData.get(key);
        if (prev == null) {
            if (cas == 0) {
                sharedData.put(key, new SharedData(value, 0));
                return WasmResult.OK;
            } else {
                return WasmResult.CAS_MISMATCH;
            }
        } else {
            if (cas == 0 || prev.cas == cas) {
                sharedData.put(key, new SharedData(value, prev.cas + 1));
                return WasmResult.OK;
            } else {
                return WasmResult.CAS_MISMATCH;
            }
        }
    }
}
