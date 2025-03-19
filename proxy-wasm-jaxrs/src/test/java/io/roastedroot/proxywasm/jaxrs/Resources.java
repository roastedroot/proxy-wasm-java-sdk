package io.roastedroot.proxywasm.jaxrs;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/test")
public class Resources {

    @Path("/httpHeaders")
    @GET
    @NamedWasmPlugin("httpHeaders")
    public String httpHeaders() {
        return "hello world";
    }

    @Path("/notSharedHttpHeaders")
    @GET
    @NamedWasmPlugin("notSharedHttpHeaders")
    public String notSharedHttpHeaders() {
        return "hello world";
    }
}
