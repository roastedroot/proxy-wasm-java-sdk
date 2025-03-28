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
    public String remotePort() {
        if (request == null) {
            return "";
        }
        return "" + request.getRemotePort();
    }

    @Override
    public String localAddress() {
        if (request == null) {
            return "";
        }
        return request.getLocalAddr();
    }

    @Override
    public String localPort() {
        if (request == null) {
            return "";
        }
        return "" + request.getLocalPort();
    }
}
