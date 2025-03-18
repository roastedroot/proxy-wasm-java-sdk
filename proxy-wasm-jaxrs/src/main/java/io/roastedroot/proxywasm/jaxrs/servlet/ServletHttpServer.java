package io.roastedroot.proxywasm.jaxrs.servlet;

import io.roastedroot.proxywasm.jaxrs.spi.HttpServer;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Instance;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;

@Alternative
@Priority(100)
public class ServletHttpServer implements HttpServer {

    private final HttpServletRequest request;

    public ServletHttpServer(@Context Instance<HttpServletRequest> request) {
        this.request = request.get();
    }

    @Override
    public String remoteAddress() {
        return request.getRemoteAddr();
    }

    @Override
    public String remotePort() {
        return "" + request.getRemotePort();
    }

    @Override
    public String localAddress() {
        return request.getLocalAddr();
    }

    @Override
    public String localPort() {
        return "" + request.getLocalPort();
    }
}
