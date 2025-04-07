package io.roastedroot.proxywasm.jaxrs.servlet;

import io.roastedroot.proxywasm.jaxrs.JaxrsHttpRequestAdaptor;
import jakarta.servlet.http.HttpServletRequest;

public class ServletJaxrsHttpRequestAdaptor extends JaxrsHttpRequestAdaptor {

    private final HttpServletRequest request;

    public ServletJaxrsHttpRequestAdaptor(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String remoteAddress() {
        if (request == null) {
            return "";
        }
        return request.getRemoteAddr();
    }

    @Override
    public int remotePort() {
        if (request == null) {
            return 0;
        }
        return request.getRemotePort();
    }

    @Override
    public String localAddress() {
        if (request == null) {
            return "";
        }
        return request.getLocalAddr();
    }

    @Override
    public int localPort() {
        if (request == null) {
            return 0;
        }
        return request.getLocalPort();
    }

    @Override
    public String protocol() {
        return request.getProtocol();
    }
}
