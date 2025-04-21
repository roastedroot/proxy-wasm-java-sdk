package io.roastedroot.proxywasm.jaxrs.internal;

import static io.roastedroot.proxywasm.internal.Helpers.bytes;
import static io.roastedroot.proxywasm.internal.Helpers.int32;
import static io.roastedroot.proxywasm.internal.Helpers.string;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.CONNECTION_DNS_SAN_LOCAL_CERTIFICATE;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.CONNECTION_DNS_SAN_PEER_CERTIFICATE;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.CONNECTION_ID;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.CONNECTION_MTLS;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.CONNECTION_REQUESTED_SERVER_NAME;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.CONNECTION_SHA256_PEER_CERTIFICATE_DIGEST;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.CONNECTION_SUBJECT_LOCAL_CERTIFICATE;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.CONNECTION_SUBJECT_PEER_CERTIFICATE;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.CONNECTION_URI_SAN_LOCAL_CERTIFICATE;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.CONNECTION_URI_SAN_PEER_CERTIFICATE;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.DESTINATION_ADDRESS;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.DESTINATION_PORT;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.REQUEST_DURATION;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.REQUEST_HEADERS;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.REQUEST_HOST;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.REQUEST_METHOD;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.REQUEST_PATH;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.REQUEST_PROTOCOL;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.REQUEST_QUERY;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.REQUEST_REFERER;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.REQUEST_SCHEME;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.REQUEST_SIZE;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.REQUEST_TIME;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.REQUEST_TOTAL_SIZE;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.REQUEST_URL_PATH;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.REQUEST_USERAGENT;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.RESPONSE_BACKEND_LATENCY;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.RESPONSE_CODE;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.RESPONSE_CODE_DETAILS;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.RESPONSE_FLAGS;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.RESPONSE_GRPC_STATUS;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.RESPONSE_HEADERS;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.RESPONSE_SIZE;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.RESPONSE_TOTAL_SIZE;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.RESPONSE_TRAILERS;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.SOURCE_ADDRESS;
import static io.roastedroot.proxywasm.internal.WellKnownProperties.SOURCE_PORT;

import io.roastedroot.proxywasm.WasmException;
import io.roastedroot.proxywasm.internal.ArrayProxyMap;
import io.roastedroot.proxywasm.internal.HttpRequestAdaptor;
import io.roastedroot.proxywasm.internal.PluginHttpContext;
import io.roastedroot.proxywasm.internal.ProxyMap;
import io.roastedroot.proxywasm.internal.SplitProxyMap;
import io.roastedroot.proxywasm.internal.WasmResult;
import io.roastedroot.proxywasm.internal.WellKnownHeaders;
import io.roastedroot.proxywasm.internal.WellKnownProperties;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.Duration;
import java.util.Date;
import java.util.List;

public class JaxrsHttpRequestAdaptor implements HttpRequestAdaptor {

    private ContainerRequestContext requestContext;
    private ContainerResponseContext responseContext;

    private final long startedAt = System.currentTimeMillis();

    public ContainerRequestContext getRequestContext() {
        return requestContext;
    }

    public void setRequestContext(ContainerRequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public ContainerResponseContext getResponseContext() {
        return responseContext;
    }

    public void setResponseContext(ContainerResponseContext responseContext) {
        this.responseContext = responseContext;
    }

    @Override
    public String remoteAddress() {
        return "";
    }

    @Override
    public int remotePort() {
        return 0;
    }

    @Override
    public String localAddress() {
        return "";
    }

    @Override
    public int localPort() {
        return 0;
    }

    @Override
    public String protocol() {
        String protocol = requestContext.getUriInfo().getRequestUri().getScheme();
        if (protocol == null) {
            protocol = "HTTP";
        }
        protocol = protocol.toUpperCase();
        // technically it should be something like "HTTP/1.1" or "HTTP/2" (but JAX-RS doesn't give
        // us that info).
        return protocol;
    }

    // //////////////////////////////////////////////////////////////////////
    // HTTP fields
    // //////////////////////////////////////////////////////////////////////

    @Override
    public ProxyMap getHttpRequestHeaders() {
        URI requestUri = requestContext.getUriInfo().getRequestUri();
        ArrayProxyMap wellKnownHeaders = new ArrayProxyMap();
        wellKnownHeaders.add(WellKnownHeaders.AUTHORITY, requestUri.getAuthority());
        wellKnownHeaders.add(WellKnownHeaders.SCHEME, requestUri.getScheme());
        wellKnownHeaders.add(WellKnownHeaders.METHOD, requestContext.getMethod());
        var path = requestUri.getRawPath();
        if (path == null) {
            path = "";
        }
        if (requestUri.getRawQuery() != null) {
            path += "?" + requestUri.getRawQuery();
        }
        wellKnownHeaders.add(WellKnownHeaders.PATH, path);
        return new SplitProxyMap(
                wellKnownHeaders, new MultivaluedMapAdaptor<>(requestContext.getHeaders()));
    }

    @Override
    public ProxyMap getHttpRequestTrailers() {
        return null;
    }

    @Override
    public ProxyMap getHttpResponseHeaders() {
        ArrayProxyMap wellKnownHeaders = new ArrayProxyMap();
        wellKnownHeaders.add(WellKnownHeaders.STATUS, "" + responseContext.getStatus());
        return new SplitProxyMap(
                wellKnownHeaders, new MultivaluedMapAdaptor<>(responseContext.getHeaders()));
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

    @Override
    public byte[] getProperty(PluginHttpContext pluginRequest, List<String> path)
            throws WasmException {

        // Check to see if it's a well known property

        // Downstream connection properties
        if (CONNECTION_ID.equals(path)) {
            // Do we need to generate one?
            return null;
        } else if (SOURCE_ADDRESS.equals(path)) {
            return bytes(remoteAddress());
        } else if (SOURCE_PORT.equals(path)) {
            // Port attribute is populated as uint64 (8 byte)
            // Ref:
            // https://github.com/envoyproxy/envoy/blob/1b3da361279a54956f01abba830fc5d3a5421828/source/common/network/utility.cc#L201
            return bytes((long) remotePort());
        } else if (DESTINATION_ADDRESS.equals(path)) {
            return bytes(localAddress());
        } else if (DESTINATION_PORT.equals(path)) {
            // Port attribute is populated as uint64 (8 byte)
            // Ref:
            // https://github.com/envoyproxy/envoy/blob/1b3da361279a54956f01abba830fc5d3a5421828/source/common/network/utility.cc#L201
            return bytes((long) localPort());
        }

        // TODO: get TLS connection properties
        else if (WellKnownProperties.CONNECTION_TLS_VERSION.equals(path)) {
            // TODO:
            return null;
        } else if (CONNECTION_REQUESTED_SERVER_NAME.equals(path)) {
            // TODO:
            return null;
        } else if (CONNECTION_MTLS.equals(path)) {
            // TODO:
            return null;
        } else if (CONNECTION_SUBJECT_LOCAL_CERTIFICATE.equals(path)) {
            // TODO:
            return null;
        } else if (CONNECTION_SUBJECT_PEER_CERTIFICATE.equals(path)) {
            // TODO:
            return null;
        } else if (CONNECTION_DNS_SAN_LOCAL_CERTIFICATE.equals(path)) {
            // TODO:
            return null;
        } else if (CONNECTION_DNS_SAN_PEER_CERTIFICATE.equals(path)) {
            // TODO:
            return null;
        } else if (CONNECTION_URI_SAN_LOCAL_CERTIFICATE.equals(path)) {
            // TODO:
            return null;
        } else if (CONNECTION_URI_SAN_PEER_CERTIFICATE.equals(path)) {
            // TODO:
            return null;
        } else if (CONNECTION_SHA256_PEER_CERTIFICATE_DIGEST.equals(path)) {
            // TODO:
            return null;

        } else if (REQUEST_PATH.equals(path)) {
            // The path + query portion of the URL

            if (requestContext == null) {
                return null;
            }

            URI requestUri = requestContext.getUriInfo().getRequestUri();
            var result =
                    requestUri.getRawPath()
                            + (requestUri.getRawQuery() != null
                                    ? "?" + requestUri.getRawQuery()
                                    : "");
            return bytes(result);
        } else if (REQUEST_URL_PATH.equals(path)) {

            // The path without query portion of the URL
            if (requestContext == null) {
                return null;
            }
            URI requestUri = requestContext.getUriInfo().getRequestUri();
            return bytes(requestUri.getRawPath());

        } else if (REQUEST_QUERY.equals(path)) {
            if (requestContext == null) {
                return null;
            }
            return bytes(requestContext.getUriInfo().getRequestUri().getQuery());

        } else if (REQUEST_HOST.equals(path)) {
            if (requestContext == null) {
                return null;
            }
            return bytes(requestContext.getUriInfo().getRequestUri().getHost());
        } else if (REQUEST_SCHEME.equals(path)) {
            if (requestContext == null) {
                return null;
            }
            return bytes(requestContext.getUriInfo().getRequestUri().getScheme());
        } else if (REQUEST_METHOD.equals(path)) {
            if (requestContext == null) {
                return null;
            }
            return bytes(requestContext.getMethod());
        } else if (REQUEST_HEADERS.equals(path)) {
            var headers = getHttpRequestHeaders();
            if (headers == null) {
                return null;
            }
            return headers.encode();

        } else if (REQUEST_REFERER.equals(path)) {
            if (requestContext == null) {
                return null;
            }
            return bytes(requestContext.getHeaderString("Referer"));
        } else if (REQUEST_USERAGENT.equals(path)) {
            if (requestContext == null) {
                return null;
            }
            return bytes(requestContext.getHeaderString("User-Agent"));
        }

        // HTTP request properties
        else if (REQUEST_PROTOCOL.equals(path)) {
            return bytes(protocol());
        } else if (REQUEST_TIME.equals(path)) {
            return bytes(new Date(startedAt));
        } else if (REQUEST_DURATION.equals(path)) {
            return bytes(Duration.ofMillis((System.currentTimeMillis() - startedAt)));
        } else if (REQUEST_SIZE.equals(path)) {
            var httpRequestBody = pluginRequest.getHttpRequestBody();
            if (httpRequestBody == null) {
                return null;
            }
            return bytes(httpRequestBody.length);
        } else if (REQUEST_TOTAL_SIZE.equals(path)) {
            // TODO: how can we do this?
            return null;
        }

        // HTTP response properties
        else if (RESPONSE_CODE.equals(path)) {
            return bytes(responseContext.getStatus());
        } else if (RESPONSE_CODE_DETAILS.equals(path)) {
            return bytes(responseContext.getStatusInfo().getReasonPhrase());
        } else if (RESPONSE_FLAGS.equals(path)) {
            // TODO: implement response flags retrieval
            return null;
        } else if (RESPONSE_GRPC_STATUS.equals(path)) {
            // TODO: implement gRPC status retrieval
            return null;
        } else if (RESPONSE_HEADERS.equals(path)) {
            var headers = getHttpResponseHeaders();
            if (headers == null) {
                return null;
            }
            return headers.encode();
        } else if (RESPONSE_TRAILERS.equals(path)) {
            var headers = getHttpResponseTrailers();
            if (headers == null) {
                return null;
            }
            return headers.encode();
        } else if (RESPONSE_BACKEND_LATENCY.equals(path)) {
            // TODO: implement backend latency retrieval
            return null;
        } else if (RESPONSE_SIZE.equals(path)) {
            var httpResponseBody = pluginRequest.getHttpResponseBody();
            if (httpResponseBody == null) {
                return null;
            }
            return bytes(httpResponseBody.length);
        } else if (RESPONSE_TOTAL_SIZE.equals(path)) {
            // TODO: how can we do this?
            return null;
        }

        return null;
    }

    @Override
    public WasmResult setProperty(
            PluginHttpContext pluginRequest, List<String> path, byte[] value) {

        // Check to see if it's a well known property
        if (REQUEST_PATH.equals(path)) {
            // The path + query portion of the URL
            if (requestContext == null) {
                return null;
            }

            var pathAndQuery = URI.create(string(value));
            var uri = requestContext.getUriInfo().getRequestUri();
            uri =
                    UriBuilder.fromUri(uri)
                            .replacePath(pathAndQuery.getPath())
                            .replaceQuery(pathAndQuery.getQuery())
                            .build();
            requestContext.setRequestUri(uri);

        } else if (REQUEST_URL_PATH.equals(path)) {
            // The path portion of the URL
            if (requestContext == null) {
                return null;
            }

            var uri = requestContext.getUriInfo().getRequestUri();
            uri = UriBuilder.fromUri(uri).replacePath(string(value)).build();
            requestContext.setRequestUri(uri);
        } else if (REQUEST_QUERY.equals(path)) {
            if (requestContext == null) {
                return null;
            }

            var uri = requestContext.getUriInfo().getRequestUri();
            uri = UriBuilder.fromUri(uri).replaceQuery(string(value)).build();
            requestContext.setRequestUri(uri);

        } else if (REQUEST_HOST.equals(path)) {
            if (requestContext == null) {
                return null;
            }
            var uri = requestContext.getUriInfo().getRequestUri();
            uri = UriBuilder.fromUri(uri).host(string(value)).build();
            requestContext.setRequestUri(uri);

        } else if (REQUEST_SCHEME.equals(path)) {

            if (requestContext == null) {
                return null;
            }
            var uri = requestContext.getUriInfo().getRequestUri();
            uri = UriBuilder.fromUri(uri).scheme(string(value)).build();
            requestContext.setRequestUri(uri);
        } else if (REQUEST_METHOD.equals(path)) {
            if (requestContext == null) {
                return null;
            }
            requestContext.setMethod(string(value));
        } else if (REQUEST_HEADERS.equals(path)) {
            // TODO:
        } else if (REQUEST_REFERER.equals(path)) {
            if (requestContext == null) {
                return null;
            }
            requestContext.getHeaders().putSingle("Referer", string(value));
        } else if (REQUEST_USERAGENT.equals(path)) {
            if (requestContext == null) {
                return null;
            }
            requestContext.getHeaders().putSingle("User-Agent", string(value));
        }

        // HTTP request properties
        else if (REQUEST_PROTOCOL.equals(path)) {
            if (requestContext == null) {
                return null;
            }
            var uri = requestContext.getUriInfo().getRequestUri();
            uri = UriBuilder.fromUri(uri).scheme(string(value)).build();
            requestContext.setRequestUri(uri);
        }

        // HTTP response properties
        else if (RESPONSE_CODE.equals(path)) {
            responseContext.setStatus(int32(value));
        } else if (RESPONSE_CODE_DETAILS.equals(path)) {
            // TODO:
        } else if (RESPONSE_HEADERS.equals(path)) {
            // TODO:
        } else if (RESPONSE_TRAILERS.equals(path)) {
            // TODO:
        } else {
            return WasmResult.NOT_FOUND;
        }
        return WasmResult.OK;
    }
}
