package io.quarkiverse.proxywasm.runtime;

import io.roastedroot.proxywasm.jaxrs.JaxrsHttpRequestAdaptor;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;

@Alternative
@Priority(200)
@RequestScoped
public class VertxHttpRequestAdaptor extends JaxrsHttpRequestAdaptor {

    @Inject io.vertx.core.http.HttpServerRequest request;

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
