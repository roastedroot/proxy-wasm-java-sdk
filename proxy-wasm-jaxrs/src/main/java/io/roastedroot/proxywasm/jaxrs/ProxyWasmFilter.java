package io.roastedroot.proxywasm.jaxrs;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import java.io.IOException;

@PreMatching
public class ProxyWasmFilter implements ContainerRequestFilter {

    private final WasmPlugin plugin;

    @Inject
    public ProxyWasmFilter(WasmPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        System.out.println("Filtering request with plugin: " + plugin.name());
    }
}
