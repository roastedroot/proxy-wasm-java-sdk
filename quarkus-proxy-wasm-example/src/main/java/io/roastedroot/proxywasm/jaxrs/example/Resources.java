package io.roastedroot.proxywasm.jaxrs.example;

import io.roastedroot.proxywasm.jaxrs.WasmPlugin;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * JAX-RS resource class for the proxy-wasm example.
 * Defines endpoints for testing the Wasm plugin.
 */
@Path("/")
public class Resources {

    /**
     * Default constructor.
     */
    public Resources() {
        // Default constructor
    }

    /**
     * Handles GET requests to the /test path.
     * Applies the "example" Wasm plugin.
     *
     * @return A simple "Hello World" string.
     */
    @Path("/test")
    @GET
    @WasmPlugin("example") // filter with example wasm plugin
    public String example() {
        return "Hello World";
    }
}
