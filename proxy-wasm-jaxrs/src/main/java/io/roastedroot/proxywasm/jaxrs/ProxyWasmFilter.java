package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.StartException;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

@PreMatching
public class ProxyWasmFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private final WasmPluginFactory pluginFactory;

    @Inject
    public ProxyWasmFilter(WasmPluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        WasmPlugin plugin = null;
        try {
            plugin = pluginFactory.create();
        } catch (StartException e) {
            requestContext.abortWith(
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        }

        // so we can continue to use the plugin in the other filter methods.
        requestContext.setProperty("WasmPlugin", plugin);
        var wasmHttpContext = plugin.createHttpContext();
        requestContext.setProperty("WasmHttpContext", wasmHttpContext);

        plugin.handler().setHttpRequestHeaders(requestContext.getHeaders());
        wasmHttpContext.callOnRequestHeaders(false);
    }

    @Override
    public void filter(
            ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {}
}
