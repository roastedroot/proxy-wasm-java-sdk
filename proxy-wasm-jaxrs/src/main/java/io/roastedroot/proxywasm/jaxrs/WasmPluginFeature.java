package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.jaxrs.spi.HttpServer;
import io.roastedroot.proxywasm.jaxrs.spi.HttpServerRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;
import java.util.Collection;
import java.util.HashMap;

@Provider
@ApplicationScoped
public class WasmPluginFeature implements DynamicFeature {

    private final HashMap<String, WasmPluginPool> pluginPools = new HashMap<>();

    @Inject @Any Instance<HttpServerRequest> httpServerRequest;

    @Inject
    public WasmPluginFeature(Instance<WasmPluginFactory> factories, @Any HttpServer httpServer)
            throws StartException {
        for (var factory : factories) {
            WasmPlugin plugin = null;
            plugin = factory.create();
            plugin.setHttpServer(httpServer);
            String name = plugin.name();
            if (this.pluginPools.containsKey(name)) {
                throw new IllegalArgumentException("Duplicate wasm plugin name: " + name);
            }
            WasmPluginPool pool =
                    plugin.isShared()
                            ? new WasmPluginPool.AppScoped(plugin)
                            : new WasmPluginPool.RequestScoped(factory, plugin);
            this.pluginPools.put(name, pool);
        }
    }

    public Collection<WasmPluginPool> getPluginPools() {
        return pluginPools.values();
    }

    public WasmPluginPool pool(String name) {
        return pluginPools.get(name);
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        var resourceMethod = resourceInfo.getResourceMethod();
        if (resourceMethod != null) {
            NamedWasmPlugin pluignNameAnnotation =
                    resourceMethod.getAnnotation(NamedWasmPlugin.class);
            if (pluignNameAnnotation == null) {
                // If no annotation on method, check the class level
                pluignNameAnnotation =
                        resourceInfo.getResourceClass().getAnnotation(NamedWasmPlugin.class);
            }
            if (pluignNameAnnotation != null) {
                WasmPluginPool factory = pluginPools.get(pluignNameAnnotation.value());
                if (factory != null) {
                    context.register(new ProxyWasmFilter(factory, httpServerRequest));
                }
            }
        }
    }
}
