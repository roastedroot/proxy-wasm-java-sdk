package io.roastedroot.proxywasm.jaxrs;

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

    private HashMap<String, WasmPlugin> plugins = new HashMap<>();

    @Inject
    public WasmPluginFeature(@Any Instance<WasmPlugin> plugins) {
        for (WasmPlugin plugin : plugins) {
            if (this.plugins.containsKey(plugin.name())) {
                throw new IllegalArgumentException("Duplicate wasm plugin name: " + plugin.name());
            }
            this.plugins.put(plugin.name(), plugin);
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
                WasmPlugin plugin = plugins.get(pluignNameAnnotation.value());
                if (plugin != null) {
                    context.register(new ProxyWasmFilter(plugin));
                }
            }
        }
    }
}
