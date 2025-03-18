package io.roastedroot.proxywasm.jaxrs;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/http_headers")
public class HttpHeadersResource {

    @GET
    @NamedWasmPlugin("http_headers")
    public String get() {
        return "hello world";
    }
}
