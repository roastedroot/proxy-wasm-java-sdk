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
 * A CDI-managed JAX-RS {@link Provider} and {@link jakarta.ws.rs.core.Feature} that automatically
 * configures and registers Proxy-Wasm plugin filtering within a CDI environment.
 *
 * <p>This bean acts as the bridge between the CDI container and the Proxy-Wasm JAX-RS integration.
 * It automatically discovers and injects {@link PluginFactory} beans your application provides.
 *
 * It will use the best {@link ServerAdaptor} bean alternative that it can find for your CDI environment.
 *
 * <p>Simply ensure this bean is available in your CDI application (e.g., through scanning or
 * explicit declaration), and any required {@link PluginFactory} beans are also defined. This feature
 * will then automatically register the necessary filters.
 *
 * <p>Note: This class is intended for use in CDI environments. If you are not using CDI, you can
 * use the {@link io.roastedroot.proxywasm.jaxrs.WasmPluginFeature} class directly to register the
 * feature with your JAX-RS application.
 *
 * @see io.roastedroot.proxywasm.jaxrs.WasmPluginFeature
 * @see io.roastedroot.proxywasm.jaxrs.WasmPluginFilter
 * @see PluginFactory
 * @see ServerAdaptor
 * @see Provider
 * @see ApplicationScoped
 * @see PostConstruct
 * @see PreDestroy
 */
@Provider
@ApplicationScoped
public class WasmPluginFeature extends AbstractWasmPluginFeature {

    @Inject Instance<PluginFactory> factories;

    @Inject @Any ServerAdaptor serverAdaptor;

    /**
     * Creates a new instance of the WasmPluginFeature.
     */
    public WasmPluginFeature() {}

    /**
     * Initializes the WasmPluginFeature using injected CDI dependencies.
     * This method is automatically called by the CDI container after dependency injection
     * is complete.
     *
     * @throws StartException If an error occurs during the initialization or startup of the
     *                        underlying Proxy-Wasm plugins obtained from the injected factories.
     */
    @Inject
    @PostConstruct
    public void init() throws StartException {
        init(factories, serverAdaptor);
    }

    /**
     * Cleans up resources used by the feature, such as stopping the underlying plugins.
     * This method is automatically called by the CDI container before the bean is destroyed.
     */
    @PreDestroy
    public void destroy() {
        super.destroy();
    }
}
