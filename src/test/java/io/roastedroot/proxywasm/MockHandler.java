package io.roastedroot.proxywasm;

import io.roastedroot.proxywasm.v1.Handler;
import io.roastedroot.proxywasm.v1.LogLevel;
import io.roastedroot.proxywasm.v1.WasmException;
import io.roastedroot.proxywasm.v1.WasmResult;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MockHandler implements Handler {

    public static class HttpResponse {

        public final int statusCode;
        public final ByteBuffer statusCodeDetails;
        public final ByteBuffer body;
        public final Map<String, String> headers;
        public final int grpcStatus;

        public HttpResponse(
                int responseCode,
                ByteBuffer responseCodeDetails,
                ByteBuffer responseBody,
                Map<String, String> additionalHeaders,
                int grpcStatus) {
            this.statusCode = responseCode;
            this.statusCodeDetails = responseCodeDetails;
            this.body = responseBody;
            this.headers = additionalHeaders;
            this.grpcStatus = grpcStatus;
        }
    }

    final ArrayList<String> loggedMessages = new ArrayList<String>();

    private Map<String, String> httpRequestHeaders = new HashMap<>();
    private Map<String, String> httpRequestTrailers = new HashMap<>();
    private Map<String, String> httpResponseHeaders = new HashMap<>();
    private Map<String, String> httpResponseTrailers = new HashMap<>();
    private Map<String, String> httpCallResponseHeaders = new HashMap<>();
    private Map<String, String> httpCallResponseTrailers = new HashMap<>();
    private Map<String, String> grpcReceiveInitialMetadata = new HashMap<>();
    private Map<String, String> grpcReceiveTrailerMetadata = new HashMap<>();
    private HttpResponse senthttpResponse;

    @Override
    public void log(LogLevel level, String message) throws WasmException {
        loggedMessages.add(message);
    }

    public ArrayList<String> loggedMessages() {
        return loggedMessages;
    }

    @Override
    public Map<String, String> getHttpRequestHeader() {
        return httpRequestHeaders;
    }

    @Override
    public Map<String, String> getHttpRequestTrailer() {
        return httpRequestTrailers;
    }

    @Override
    public Map<String, String> getHttpResponseHeader() {
        return httpResponseHeaders;
    }

    @Override
    public Map<String, String> getHttpResponseTrailer() {
        return httpResponseTrailers;
    }

    @Override
    public Map<String, String> getHttpCallResponseHeaders() {
        return httpCallResponseHeaders;
    }

    @Override
    public Map<String, String> getHttpCallResponseTrailer() {
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
    public WasmResult setHttpRequestHeader(Map<String, String> headers) {
        this.httpRequestHeaders = headers;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setHttpRequestTrailer(Map<String, String> trailers) {
        this.httpRequestTrailers = trailers;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setHttpResponseHeader(Map<String, String> headers) {
        this.httpResponseHeaders = headers;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setHttpResponseTrailer(Map<String, String> trailers) {
        this.httpResponseTrailers = trailers;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setHttpCallResponseHeaders(Map<String, String> headers) {
        this.httpCallResponseHeaders = headers;
        return WasmResult.OK;
    }

    @Override
    public WasmResult setHttpCallResponseTrailer(Map<String, String> trailers) {
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
    public WasmResult sendHttpResponse(
            int responseCode,
            ByteBuffer responseCodeDetails,
            ByteBuffer responseBody,
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

    public HttpResponse getSenthttpResponse() {
        return senthttpResponse;
    }
}
