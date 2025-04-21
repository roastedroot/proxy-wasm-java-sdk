package io.roastedroot.proxywasm.jaxrs.cdi;

import io.roastedroot.proxywasm.PluginFactory;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.internal.ServerAdaptor;
import io.roastedroot.proxywasm.jaxrs.internal.AbstractWasmPluginFeature;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.ext.Provider;

/**
 * This class is a CDI provider for the WasmPluginFeature.
 * It initializes the plugin factories and server adaptor.
 * It also handles the lifecycle of the feature.
 */
@Provider
@ApplicationScoped
public class WasmPluginFeature extends AbstractWasmPluginFeature {

    @Inject Instance<PluginFactory> factories;

    @Inject @Any ServerAdaptor serverAdaptor;

    @Inject
    @PostConstruct
    public void init() throws StartException {
        init(factories, serverAdaptor);
    }

    @PreDestroy
    public void destroy() {
        super.destroy();
    }
}
