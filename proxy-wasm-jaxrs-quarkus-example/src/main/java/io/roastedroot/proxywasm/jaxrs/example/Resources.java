package io.roastedroot.proxywasm.jaxrs.example;

import io.roastedroot.proxywasm.jaxrs.NamedWasmPlugin;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/")
public class Resources {

    @Path("/ffiTests/reverse")
    @POST
    @NamedWasmPlugin("ffiTests") // filter with ffiTests wasm plugin
    public String ffiTests(String body) {
        return body;
    }
}
