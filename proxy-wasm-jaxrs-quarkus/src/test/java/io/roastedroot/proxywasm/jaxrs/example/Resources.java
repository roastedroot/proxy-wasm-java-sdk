package io.roastedroot.proxywasm.jaxrs.example;

import io.roastedroot.proxywasm.jaxrs.NamedWasmPlugin;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

@Path("/")
public class Resources {

    @Context ContainerRequestContext requestContext;

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

    @Path("/headerTests")
    @GET
    @NamedWasmPlugin("headerTests")
    public String uhttpHeaders(@HeaderParam("x-request-counter") String counter) {
        return String.format("counter: %s", counter);
    }

    @Path("/headerTestsNotShared")
    @GET
    @NamedWasmPlugin("headerTestsNotShared")
    public String unotSharedHttpHeaders(@HeaderParam("x-request-counter") String counter) {
        return String.format("counter: %s", counter);
    }

    @Path("/tickTests/{sub: .+ }")
    @GET
    @NamedWasmPlugin("tickTests")
    public String tickTests(@PathParam("sub") String sub) {
        return "hello world";
    }

    @Path("/ffiTests/reverse")
    @POST
    @NamedWasmPlugin("ffiTests")
    public String ffiTests(String body) {
        return body;
    }

    @Path("/httpCallTests")
    @GET
    @NamedWasmPlugin("httpCallTests")
    public String httpCallTests() {
        return "hello world";
    }
}
