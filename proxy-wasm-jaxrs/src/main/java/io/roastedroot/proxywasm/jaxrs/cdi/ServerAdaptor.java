package io.roastedroot.proxywasm.jaxrs.cdi;

import io.roastedroot.proxywasm.jaxrs.internal.BlockingServerAdaptor;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

/**
 * A CDI {@link Alternative} bean providing an implementation of
 * {@link io.roastedroot.proxywasm.internal.ServerAdaptor} tailored for JAX-RS environments
 * managed by CDI (Contexts and Dependency Injection).
 *
 * <p>This bean allows the Proxy-Wasm host integration to be seamlessly managed within
 * a CDI container (like Quarkus or Helidon). By being an {@code @Alternative} with a
 * specific {@code @Priority}, it can be selected or overridden as needed within the
 * CDI application configuration.
 *
 * <p>It extends {@link BlockingServerAdaptor}, inheriting the
 * base JAX-RS adaptation logic.
 *
 * @see io.roastedroot.proxywasm.internal.ServerAdaptor
 * @see Alternative
 * @see ApplicationScoped
 */
@Alternative
@Priority(100)
@ApplicationScoped
public class ServerAdaptor extends BlockingServerAdaptor {

    /**
     * Default constructor required by CDI for proxying and bean management.
     */
    public ServerAdaptor() {
        super();
    }
}
