package io.roastedroot.proxywasm.jaxrs;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/example")
public class ExampleResource {

    @GET
    @NamedWasmPlugin("bar")
    @Path("/bar")
    public String bar() {
        return "bar";
    }

    @GET
    @NamedWasmPlugin("foo")
    @Path("/foo")
    public String foo() {
        return "foo";
    }
}
