package io.roastedroot.proxywasm.jaxrs.servlet;

import io.roastedroot.proxywasm.jaxrs.internal.JaxrsHttpRequestAdaptor;
import jakarta.servlet.http.HttpServletRequest;

/**
 * A {@link JaxrsHttpRequestAdaptor} specifically designed to work with the Jakarta Servlet API based
 * JAX-RS implementations.
 *
 * <p>This adaptor implementation extracts request details (like remote/local addresses and ports,
 * protocol) directly from the underlying {@link HttpServletRequest} object.
 */
public class ServletJaxrsHttpRequestAdaptor extends JaxrsHttpRequestAdaptor {

    private final HttpServletRequest request;

    /**
     * Constructs a new adaptor.
     *
     * @param request The {@link HttpServletRequest} associated with the current request.
     */
    public ServletJaxrsHttpRequestAdaptor(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * Retrieves the remote IP address from the underlying {@link HttpServletRequest}.
     *
     * @return The remote IP address, or an empty string if the request object is null.
     */
    @Override
    public String remoteAddress() {
        if (request == null) {
            return "";
        }
        return request.getRemoteAddr();
    }

    /**
     * Retrieves the remote port from the underlying {@link HttpServletRequest}.
     *
     * @return The remote port number, or 0 if the request object is null.
     */
    @Override
    public int remotePort() {
        if (request == null) {
            return 0;
        }
        return request.getRemotePort();
    }

    /**
     * Retrieves the local IP address from the underlying {@link HttpServletRequest}.
     *
     * @return The local IP address, or an empty string if the request object is null.
     */
    @Override
    public String localAddress() {
        if (request == null) {
            return "";
        }
        return request.getLocalAddr();
    }

    /**
     * Retrieves the local port from the underlying {@link HttpServletRequest}.
     *
     * @return The local port number, or 0 if the request object is null.
     */
    @Override
    public int localPort() {
        if (request == null) {
            return 0;
        }
        return request.getLocalPort();
    }

    /**
     * Retrieves the protocol string (e.g., "HTTP/1.1") from the underlying {@link HttpServletRequest}.
     *
     * @return The protocol string.
     */
    @Override
    public String protocol() {
        return request.getProtocol();
    }
}
