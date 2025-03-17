package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.StartException;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import java.io.IOException;

@PreMatching
public class ProxyWasmFilter implements ContainerRequestFilter {

    private final WasmPluginFactory pluginFactory;

    @Inject
    public ProxyWasmFilter(WasmPluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        try {
            var plugin = pluginFactory.create();
            System.out.println("Filtering request with plugin: " + plugin.name());
        } catch (StartException ignored) {
        }
    }
}
