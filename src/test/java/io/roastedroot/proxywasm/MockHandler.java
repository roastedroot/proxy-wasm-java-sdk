package io.roastedroot.proxywasm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.roastedroot.proxywasm.v1.Handler;
import io.roastedroot.proxywasm.v1.Helpers;
import io.roastedroot.proxywasm.v1.LogLevel;
import io.roastedroot.proxywasm.v1.WasmException;
import io.roastedroot.proxywasm.v1.WasmResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private Map<String, String> httpRequestHeaders = new HashMap<>();
    private Map<String, String> httpRequestTrailers = new HashMap<>();
    private Map<String, String> httpResponseHeaders = new HashMap<>();
    private Map<String, String> httpResponseTrailers = new HashMap<>();
    private Map<String, String> httpCallResponseHeaders = new HashMap<>();
    private Map<String, String> httpCallResponseTrailers = new HashMap<>();
    private Map<String, String> grpcReceiveInitialMetadata = new HashMap<>();
    private Map<String, String> grpcReceiveTrailerMetadata = new HashMap<>();
    private HttpResponse senthttpResponse;

    private byte[] funcCallData = new byte[0];
    private byte[] httpRequestBody = new byte[0];
    private byte[] httpResponseBody = new byte[0];
    private byte[] downStreamData = new byte[0];
    private byte[] upstreamData = new byte[0];
    private byte[] httpCallResponseBody = new byte[0];
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

    public void assertLogsContain(String message) {
        assertTrue(
                loggedMessages().contains(message), "logged messages does not contain: " + message);
    }

    public void assertLogsDoNotContain(String message) {
        for (String log : loggedMessages()) {
            assertFalse(log.contains(message), "logged messages contains: " + message);
        }
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
    public Map<String, String> getHttpCallResponseHeaders() {
        return httpCallResponseHeaders;
    }

    @Override
    public Map<String, String> getHttpCallResponseTrailers() {
        return httpCallResponseTrailers;
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
    public WasmResult setHttpCallResponseHeaders(Map<String, String> headers) {
        this.httpCallResponseHeaders = headers;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setHttpCallResponseTrailers(Map<String, String> trailers) {
        this.httpCallResponseTrailers = trailers;
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
    public byte[] getHttpCallResponseBody() {
        return this.httpCallResponseBody;
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
    public WasmResult setHttpCallResponseBody(byte[] body) {
        this.httpCallResponseBody = body;
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
}
