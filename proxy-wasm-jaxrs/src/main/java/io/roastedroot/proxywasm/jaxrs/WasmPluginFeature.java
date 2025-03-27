package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.plugin.Plugin;
import io.roastedroot.proxywasm.plugin.PluginFactory;
import io.roastedroot.proxywasm.plugin.Pool;
import io.roastedroot.proxywasm.plugin.ServerAdaptor;
import jakarta.annotation.PreDestroy;
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

    private final HashMap<String, Pool> pluginPools = new HashMap<>();

    @Inject @Any Instance<JaxrsHttpRequestAdaptor> httpServerRequest;

    @Inject
    public WasmPluginFeature(Instance<PluginFactory> factories, @Any ServerAdaptor httpServer)
            throws StartException {
        for (var factory : factories) {
            Plugin plugin = null;
            plugin = factory.create();
            plugin.setHttpServer(httpServer);
            String name = plugin.name();
            if (this.pluginPools.containsKey(name)) {
                throw new IllegalArgumentException("Duplicate wasm plugin name: " + name);
            }
            Pool pool =
                    plugin.isShared()
                            ? new Pool.AppScoped(plugin)
                            : new Pool.RequestScoped(factory, plugin);
            this.pluginPools.put(name, pool);
        }
    }

    @PreDestroy
    public void destroy() {
        for (var pool : pluginPools.values()) {
            pool.close();
        }
    }

    public Collection<Pool> getPluginPools() {
        return pluginPools.values();
    }

    public Pool pool(String name) {
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
                Pool factory = pluginPools.get(pluignNameAnnotation.value());
                if (factory != null) {
                    context.register(new ProxyWasmFilter(factory, httpServerRequest));
                }
            }
        }
    }
}
