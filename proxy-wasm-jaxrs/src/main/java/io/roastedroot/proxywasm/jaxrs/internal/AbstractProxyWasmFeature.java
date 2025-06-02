package io.roastedroot.proxywasm.jaxrs.internal;

import io.roastedroot.proxywasm.PluginFactory;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.internal.Pool;
import io.roastedroot.proxywasm.internal.ServerAdaptor;
import io.roastedroot.proxywasm.jaxrs.ProxyWasm;
import io.roastedroot.proxywasm.jaxrs.ProxyWasmFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.FeatureContext;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

public abstract class AbstractProxyWasmFeature implements DynamicFeature {

    private final HashMap<String, Pool> pluginPools = new HashMap<>();

    public void init(Iterable<PluginFactory> factories, ServerAdaptor serverAdaptor)
            throws StartException {

        if (!pluginPools.isEmpty()) {
            return;
        }

        for (var factory : factories) {
            String name = factory.name();
            if (this.pluginPools.containsKey(name)) {
                throw new IllegalArgumentException("Duplicate wasm plugin name: " + name);
            }
            Pool pool =
                    factory.shared()
                            ? new Pool.SharedPlugin(serverAdaptor, factory)
                            : new Pool.PluginPerRequest(serverAdaptor, factory);
            this.pluginPools.put(name, pool);
        }
    }

    /**
     * Destroy all plugin pools. This method should be called when the application is shutting down
     */
    public void destroy() {
        for (var pool : pluginPools.values()) {
            pool.close();
        }
        pluginPools.clear();
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
            ProxyWasm pluignNameAnnotation = resourceMethod.getAnnotation(ProxyWasm.class);
            if (pluignNameAnnotation == null) {
                // If no annotation on method, check the class level
                pluignNameAnnotation =
                        resourceInfo.getResourceClass().getAnnotation(ProxyWasm.class);
            }
            if (pluignNameAnnotation != null) {
                var pools =
                        Arrays.stream(pluignNameAnnotation.value())
                                .map(
                                        (name) -> {
                                            Pool pool = pluginPools.get(name);
                                            if (pool != null) {
                                                return pool;
                                            } else {
                                                throw new IllegalArgumentException(
                                                        "Wasm plugin not found: "
                                                                + name
                                                                + " for resource: "
                                                                + resourceInfo
                                                                        .getResourceClass()
                                                                        .getName()
                                                                + "."
                                                                + resourceMethod.getName());
                                            }
                                        })
                                .collect(Collectors.toList());
                context.register(new ProxyWasmFilter(pools));
            }
        }
    }
}
