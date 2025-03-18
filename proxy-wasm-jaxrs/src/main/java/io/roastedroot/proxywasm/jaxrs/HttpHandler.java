package io.roastedroot.proxywasm.jaxrs;

import static io.roastedroot.proxywasm.Helpers.bytes;
import static io.roastedroot.proxywasm.Helpers.string;
import static io.roastedroot.proxywasm.WellKnownProperties.CONNECTION_DNS_SAN_LOCAL_CERTIFICATE;
import static io.roastedroot.proxywasm.WellKnownProperties.CONNECTION_DNS_SAN_PEER_CERTIFICATE;
import static io.roastedroot.proxywasm.WellKnownProperties.CONNECTION_ID;
import static io.roastedroot.proxywasm.WellKnownProperties.CONNECTION_MTLS;
import static io.roastedroot.proxywasm.WellKnownProperties.CONNECTION_REQUESTED_SERVER_NAME;
import static io.roastedroot.proxywasm.WellKnownProperties.CONNECTION_SHA256_PEER_CERTIFICATE_DIGEST;
import static io.roastedroot.proxywasm.WellKnownProperties.CONNECTION_SUBJECT_LOCAL_CERTIFICATE;
import static io.roastedroot.proxywasm.WellKnownProperties.CONNECTION_SUBJECT_PEER_CERTIFICATE;
import static io.roastedroot.proxywasm.WellKnownProperties.CONNECTION_TLS_VERSION;
import static io.roastedroot.proxywasm.WellKnownProperties.CONNECTION_URI_SAN_LOCAL_CERTIFICATE;
import static io.roastedroot.proxywasm.WellKnownProperties.CONNECTION_URI_SAN_PEER_CERTIFICATE;
import static io.roastedroot.proxywasm.WellKnownProperties.DESTINATION_ADDRESS;
import static io.roastedroot.proxywasm.WellKnownProperties.DESTINATION_PORT;
import static io.roastedroot.proxywasm.WellKnownProperties.REQUEST_DURATION;
import static io.roastedroot.proxywasm.WellKnownProperties.REQUEST_PROTOCOL;
import static io.roastedroot.proxywasm.WellKnownProperties.REQUEST_SIZE;
import static io.roastedroot.proxywasm.WellKnownProperties.REQUEST_TIME;
import static io.roastedroot.proxywasm.WellKnownProperties.REQUEST_TOTAL_SIZE;
import static io.roastedroot.proxywasm.WellKnownProperties.RESPONSE_SIZE;
import static io.roastedroot.proxywasm.WellKnownProperties.RESPONSE_TOTAL_SIZE;
import static io.roastedroot.proxywasm.WellKnownProperties.SOURCE_ADDRESS;
import static io.roastedroot.proxywasm.WellKnownProperties.SOURCE_PORT;

import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.ChainedHandler;
import io.roastedroot.proxywasm.Handler;
import io.roastedroot.proxywasm.Helpers;
import io.roastedroot.proxywasm.ProxyMap;
import io.roastedroot.proxywasm.StreamType;
import io.roastedroot.proxywasm.WasmException;
import io.roastedroot.proxywasm.WasmResult;
import io.roastedroot.proxywasm.jaxrs.spi.HttpServer;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

class HttpHandler extends ChainedHandler {

    private final PluginHandler next;
    private final HttpServer httpServer;
    private final long startedAt;

    HttpHandler(PluginHandler pluginHandler, HttpServer httpServer) {
        this.next = pluginHandler;
        this.httpServer = httpServer;
        this.startedAt = System.currentTimeMillis();
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

        // Check to see if it's a well known property

        // Downstream connection properties
        if (CONNECTION_ID.equals(path)) {
            // Do we need to generate one?
            return null;
        } else if (SOURCE_ADDRESS.equals(path)) {
            return bytes(httpServer.remoteAddress());
        } else if (SOURCE_PORT.equals(path)) {
            return bytes(httpServer.remotePort());
        } else if (DESTINATION_ADDRESS.equals(path)) {
            return bytes(httpServer.localAddress());
        } else if (DESTINATION_PORT.equals(path)) {
            return bytes(httpServer.localPort());
        }

        // TLS connection properties
        else if (CONNECTION_TLS_VERSION.equals(path)) {
            return null;
        } else if (CONNECTION_REQUESTED_SERVER_NAME.equals(path)) {
            return null;
        } else if (CONNECTION_MTLS.equals(path)) {
            return null;
        } else if (CONNECTION_SUBJECT_LOCAL_CERTIFICATE.equals(path)) {
            return null;
        } else if (CONNECTION_SUBJECT_PEER_CERTIFICATE.equals(path)) {
            return null;
        } else if (CONNECTION_DNS_SAN_LOCAL_CERTIFICATE.equals(path)) {
            return null;
        } else if (CONNECTION_DNS_SAN_PEER_CERTIFICATE.equals(path)) {
            return null;
        } else if (CONNECTION_URI_SAN_LOCAL_CERTIFICATE.equals(path)) {
            return null;
        } else if (CONNECTION_URI_SAN_PEER_CERTIFICATE.equals(path)) {
            return null;
        } else if (CONNECTION_SHA256_PEER_CERTIFICATE_DIGEST.equals(path)) {
            return null;
        }

        // Upstream connection properties: we are not directly connecting to an upstream server, so
        // these are not implemented.
        //        else if (UPSTREAM_ADDRESS.equals(path)) {
        //            return null;
        //        } else if (UPSTREAM_PORT.equals(path)) {
        //            return null;
        //        } else if (UPSTREAM_LOCAL_ADDRESS.equals(path)) {
        //            return null;
        //        } else if (UPSTREAM_LOCAL_PORT.equals(path)) {
        //            return null;
        //        } else if (UPSTREAM_TLS_VERSION.equals(path)) {
        //            return null;
        //        } else if (UPSTREAM_SUBJECT_LOCAL_CERTIFICATE.equals(path)) {
        //            return null;
        //        } else if (UPSTREAM_SUBJECT_PEER_CERTIFICATE.equals(path)) {
        //            return null;
        //        } else if (UPSTREAM_DNS_SAN_LOCAL_CERTIFICATE.equals(path)) {
        //            return null;
        //        } else if (UPSTREAM_DNS_SAN_PEER_CERTIFICATE.equals(path)) {
        //            return null;
        //        } else if (UPSTREAM_URI_SAN_LOCAL_CERTIFICATE.equals(path)) {
        //            return null;
        //        } else if (UPSTREAM_URI_SAN_PEER_CERTIFICATE.equals(path)) {
        //            return null;
        //        } else if (UPSTREAM_SHA256_PEER_CERTIFICATE_DIGEST.equals(path)) {
        //            return null;
        //        }

        // HTTP request properties
        else if (REQUEST_PROTOCOL.equals(path)) {
            if (requestContext == null) {
                return null;
            }
            return bytes(requestContext.getUriInfo().getRequestUri().getScheme());
        } else if (REQUEST_TIME.equals(path)) {
            // TODO: check encoding /w other impls
            return bytes(new Date(startedAt).toString());
        } else if (REQUEST_DURATION.equals(path)) {
            // TODO: check encoding /w other impls
            return bytes("" + (System.currentTimeMillis() - startedAt));
        } else if (REQUEST_SIZE.equals(path)) {
            if (httpRequestBody == null) {
                return null;
            }
            // TODO: check encoding /w other impls
            return bytes("" + httpRequestBody.length);
        } else if (REQUEST_TOTAL_SIZE.equals(path)) {
            return null;
        }

        // HTTP response properties
        else if (RESPONSE_SIZE.equals(path)) {
            if (httpResponseBody == null) {
                return null;
            }
            // TODO: check encoding /w other impls
            return bytes("" + httpResponseBody.length);
        } else if (RESPONSE_TOTAL_SIZE.equals(path)) {
            // TODO: how can we do this?
            return null;
        }

        byte[] result = properties.get(path);
        if (result != null) {
            return result;
        }
        return next().getProperty(path);
    }

    @Override
    public WasmResult setProperty(List<String> path, byte[] value) {
        properties.put(path, value);
        return WasmResult.OK;
    }
}
