package io.roastedroot.proxywasm.jaxrs.vertx;

import io.roastedroot.proxywasm.jaxrs.spi.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.core.Context;

@Alternative
@Priority(200)
@RequestScoped
public class VertxHttpServer implements HttpServer {

    private final HttpServerRequest request;

    public VertxHttpServer(@Context Instance<HttpServerRequest> request) {
        this.request = request.get();
    }

    @Override
    public String remoteAddress() {
        return request.remoteAddress().hostAddress();
    }

    @Override
    public String remotePort() {
        return "" + request.remoteAddress().port();
    }

    @Override
    public String localAddress() {
        return request.localAddress().hostAddress();
    }

    @Override
    public String localPort() {
        return "" + request.localAddress().port();
    }
}
