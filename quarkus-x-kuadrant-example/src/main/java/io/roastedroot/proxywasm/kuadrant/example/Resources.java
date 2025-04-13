package io.roastedroot.proxywasm.kuadrant.example;

import io.roastedroot.proxywasm.jaxrs.WasmPlugin;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@WasmPlugin("kuadrant") // use the corsaWAF filter
@Path("/")
public class Resources {

    @GET
    public String root() {
        return "Hello World";
    }
}
