package io.roastedroot.proxywasm.jaxrs;

import static io.roastedroot.proxywasm.Helpers.string;

import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.ChainedHandler;
import io.roastedroot.proxywasm.Handler;
import io.roastedroot.proxywasm.Helpers;
import io.roastedroot.proxywasm.ProxyMap;
import io.roastedroot.proxywasm.StreamType;
import io.roastedroot.proxywasm.WasmException;
import io.roastedroot.proxywasm.WasmResult;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;

class HttpHandler extends ChainedHandler {

    private final PluginHandler next;

    HttpHandler(PluginHandler pluginHandler) {
        this.next = pluginHandler;
    }

    @Override
    protected Handler next() {
        return next;
    }

    // //////////////////////////////////////////////////////////////////////
    // HTTP fields
    // //////////////////////////////////////////////////////////////////////

    private ContainerRequestContext requestContext;

    public ContainerRequestContext getRequestContext() {
        return requestContext;
    }

    public void setRequestContext(ContainerRequestContext requestContext) {
        this.requestContext = requestContext;
    }

    @Override
    public ProxyMap getHttpRequestHeaders() {
        return new JaxrsProxyMap(requestContext.getHeaders());
    }

    @Override
    public ProxyMap getHttpRequestTrailers() {
        return null;
    }

    private ContainerResponseContext responseContext;

    public void setResponseContext(ContainerResponseContext responseContext) {
        this.responseContext = responseContext;
    }

    @Override
    public ProxyMap getHttpResponseHeaders() {
        return new JaxrsProxyMap(responseContext.getHeaders());
    }

    @Override
    public ProxyMap getHttpResponseTrailers() {
        return null;
    }

    @Override
    public ProxyMap getGrpcReceiveInitialMetaData() {
        return null;
    }

    @Override
    public ProxyMap getGrpcReceiveTrailerMetaData() {
        return null;
    }

    // //////////////////////////////////////////////////////////////////////
    // Buffers
    // //////////////////////////////////////////////////////////////////////

    private byte[] httpRequestBody = new byte[0];

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

    private byte[] grpcReceiveBuffer = new byte[0];

    @Override
    public byte[] getGrpcReceiveBuffer() {
        return this.grpcReceiveBuffer;
    }

    @Override
    public WasmResult setGrpcReceiveBuffer(byte[] buffer) {
        this.grpcReceiveBuffer = buffer;
        return WasmResult.OK;
    }

    private byte[] upstreamData = new byte[0];

    @Override
    public byte[] getUpstreamData() {
        return this.upstreamData;
    }

    @Override
    public WasmResult setUpstreamData(byte[] data) {
        this.upstreamData = data;
        return WasmResult.OK;
    }

    private byte[] downStreamData = new byte[0];

    @Override
    public byte[] getDownStreamData() {
        return this.downStreamData;
    }

    @Override
    public WasmResult setDownStreamData(byte[] data) {
        this.downStreamData = data;
        return WasmResult.OK;
    }

    private byte[] httpResponseBody = new byte[0];

    @Override
    public byte[] getHttpResponseBody() {
        return this.httpResponseBody;
    }

    @Override
    public WasmResult setHttpResponseBody(byte[] body) {
        this.httpResponseBody = body;
        return WasmResult.OK;
    }

    public void appendHttpResponseBody(byte[] body) {
        this.httpResponseBody = Helpers.append(this.httpResponseBody, body);
    }

    // //////////////////////////////////////////////////////////////////////
    // HTTP streams
    // //////////////////////////////////////////////////////////////////////

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

        public Response toResponse() {
            Response.ResponseBuilder builder =
                    Response.status(statusCode, string(statusCodeDetails));
            if (headers != null) {
                for (var entry : headers.entries()) {
                    builder = builder.header(entry.getKey(), entry.getValue());
                }
            }
            builder.entity(body);
            return builder.build();
        }
    }

    private HttpResponse senthttpResponse;

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

    private Action action;

    @Override
    public WasmResult setAction(StreamType streamType, Action action) {
        this.action = action;
        return WasmResult.OK;
    }

    public Action getAction() {
        return action;
    }

    // //////////////////////////////////////////////////////////////////////
    // Properties
    // //////////////////////////////////////////////////////////////////////

    final HashMap<List<String>, byte[]> properties = new HashMap<>();

    @Override
    public byte[] getProperty(List<String> path) throws WasmException {
        byte[] result = properties.get(path);
        if (result == null) {
            return next().getProperty(path);
        }
        return result;
    }

    @Override
    public WasmResult setProperty(List<String> path, byte[] value) {
        properties.put(path, value);
        return WasmResult.OK;
    }
}
