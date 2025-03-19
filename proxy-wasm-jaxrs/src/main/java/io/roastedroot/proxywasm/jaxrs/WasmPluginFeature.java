package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.jaxrs.spi.HttpServer;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;

@Provider
public class WasmPluginFeature implements DynamicFeature {

    private HashMap<String, WasmPluginPool> plugins = new HashMap<>();

    @Inject @Any Instance<HttpServer> requestAdaptor;

    @Inject
    public WasmPluginFeature(@Any Instance<WasmPluginFactory> factories) throws StartException {
        for (var factory : factories) {
            var plugin = factory.create();
            String name = plugin.name();
            if (this.plugins.containsKey(name)) {
                throw new IllegalArgumentException("Duplicate wasm plugin name: " + name);
            }
            WasmPluginPool pool =
                    plugin.isShared()
                            ? new WasmPluginPool.AppScoped(plugin)
                            : new WasmPluginPool.RequestScoped(factory, plugin);
            this.plugins.put(name, pool);
        }
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
                WasmPluginPool factory = plugins.get(pluignNameAnnotation.value());
                if (factory != null) {
                    context.register(new ProxyWasmFilter(factory, requestAdaptor));
                }
            }
        }
    }
}
