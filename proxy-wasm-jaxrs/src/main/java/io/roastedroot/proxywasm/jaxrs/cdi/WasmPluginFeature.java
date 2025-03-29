package io.roastedroot.proxywasm.jaxrs.cdi;

import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.plugin.PluginFactory;
import io.roastedroot.proxywasm.plugin.ServerAdaptor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class WasmPluginFeature extends io.roastedroot.proxywasm.jaxrs.AbstractWasmPluginFeature {

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
