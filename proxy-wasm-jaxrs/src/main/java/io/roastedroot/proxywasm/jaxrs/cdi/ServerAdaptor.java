package io.roastedroot.proxywasm.jaxrs.cdi;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;

/**
 * ServerAdaptor is a CDI alternative for the {@link io.roastedroot.proxywasm.jaxrs.internal.ServerAdaptor}
 */
@Alternative
@Priority(100)
@ApplicationScoped
public class ServerAdaptor extends io.roastedroot.proxywasm.jaxrs.internal.ServerAdaptor {}
