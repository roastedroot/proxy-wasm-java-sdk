package io.quarkiverse.proxywasm.runtime;

import io.roastedroot.proxywasm.jaxrs.internal.JaxrsHttpRequestAdaptor;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

/**
 * Vert.x specific implementation of {@link JaxrsHttpRequestAdaptor}.
 * Adapts a Vert.x {@link io.vertx.core.http.HttpServerRequest} to the generic request interface.
 */
@Alternative
@Priority(200)
@RequestScoped
public class VertxHttpRequestAdaptor extends JaxrsHttpRequestAdaptor {

    /**
     * Default constructor.
     */
    public VertxHttpRequestAdaptor() {
        // Default constructor for VertxHttpRequestAdaptor
    }

    @Inject io.vertx.core.http.HttpServerRequest request;

    @Override
    public String remoteAddress() {
        return request.remoteAddress().hostAddress() + ":" + request.remoteAddress().port();
    }

    @Override
    public int remotePort() {
        return request.remoteAddress().port();
    }

    @Override
    public String localAddress() {
        return request.localAddress().hostAddress() + ":" + request.localAddress().port();
    }

    @Override
    public int localPort() {
        return request.localAddress().port();
    }

    @Override
    public String protocol() {
        switch (request.version()) {
            case HTTP_1_0:
                return "HTTP/1.0";
            case HTTP_1_1:
                return "HTTP/1.1";
            case HTTP_2:
                return "HTTP/2";
            default:
                return "unknown";
        }
    }
}
