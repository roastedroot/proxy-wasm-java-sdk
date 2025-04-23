package org.example;

import io.roastedroot.proxywasm.jaxrs.WasmPlugin;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * JAX-RS resource class for the Kuadrant example.
 * This class defines the root endpoint and applies the Kuadrant WasmPlugin.
 */
@WasmPlugin("kuadrant") // use the corsaWAF filter
@Path("/")
public class Resources {

    /**
     * Default constructor.
     */
    public Resources() {
        // Default constructor
    }

    /**
     * Handles GET requests to the root path ("/").
     *
     * @return A simple "Hello World" string.
     */
    @GET
    public String root() {
        return "Hello World";
    }
}
