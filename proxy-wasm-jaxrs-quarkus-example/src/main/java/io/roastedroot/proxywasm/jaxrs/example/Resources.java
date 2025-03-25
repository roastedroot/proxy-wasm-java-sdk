package io.roastedroot.proxywasm.jaxrs.example;

import io.roastedroot.proxywasm.jaxrs.NamedWasmPlugin;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

@Path("/")
public class Resources {

    @Context ContainerRequestContext requestContext;

    @Path("/test/httpHeaders")
    @GET
    @NamedWasmPlugin("httpHeaders")
    public String httpHeaders() {
        return "hello world";
    }

    @Path("/test/notSharedHttpHeaders")
    @GET
    @NamedWasmPlugin("notSharedHttpHeaders")
    public String notSharedHttpHeaders() {
        return "hello world";
    }

    @Path("/fail")
    @GET
    public Response fail() {
        Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
        for (String header : requestContext.getHeaders().keySet()) {
            builder.header("echo-" + header, requestContext.getHeaderString(header));
        }
        return builder.build();
    }

    @Path("/ok")
    @GET
    public Response ok() {
        Response.ResponseBuilder builder = Response.status(Response.Status.OK);
        for (String header : requestContext.getHeaders().keySet()) {
            builder.header("echo-" + header, requestContext.getHeaderString(header));
        }
        return builder.entity("ok").build();
    }
}
