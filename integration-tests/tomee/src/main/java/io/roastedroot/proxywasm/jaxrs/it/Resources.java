package io.roastedroot.proxywasm.jaxrs.it;

import io.roastedroot.proxywasm.jaxrs.ProxyWasm;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;

/**
 * JAX-RS resource class providing endpoints for integration tests.
 */
@Path("/")
public class Resources {

    /**
     * Endpoint for testing header manipulation with a shared Wasm plugin instance.
     *
     * @param counter The value of the "x-request-counter" header.
     * @return A string indicating the counter value.
     */
    @Path("/headerTests")
    @GET
    @ProxyWasm("headerTests")
    public String headerTests(@HeaderParam("x-request-counter") String counter) {
        return String.format("counter: %s", counter);
    }
}
