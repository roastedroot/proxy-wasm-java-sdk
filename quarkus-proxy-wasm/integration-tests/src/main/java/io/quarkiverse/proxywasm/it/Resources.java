package io.quarkiverse.proxywasm.it;

import io.roastedroot.proxywasm.jaxrs.WasmPlugin;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

/**
 * JAX-RS resource class providing endpoints for integration tests.
 */
@Path("/")
public class Resources {

    /**
     * Default constructor.
     */
    public Resources() {
        // Default constructor
    }

    @Context ContainerRequestContext requestContext;

    /**
     * Endpoint that simulates a failure response.
     *
     * @return A {@link Response} with status 400 (Bad Request) echoing request headers.
     */
    @Path("/fail")
    @GET
    public Response fail() {
        Response.ResponseBuilder builder = Response.status(Response.Status.BAD_REQUEST);
        for (String header : requestContext.getHeaders().keySet()) {
            builder.header("echo-" + header, requestContext.getHeaderString(header));
        }
        return builder.build();
    }

    /**
     * Endpoint that simulates a successful response.
     *
     * @return A {@link Response} with status 200 (OK) echoing request headers and body "ok".
     */
    @Path("/ok")
    @GET
    public Response ok() {
        Response.ResponseBuilder builder = Response.status(Response.Status.OK);
        for (String header : requestContext.getHeaders().keySet()) {
            builder.header("echo-" + header, requestContext.getHeaderString(header));
        }
        return builder.entity("ok").build();
    }

    /**
     * Endpoint for testing header manipulation with a shared Wasm plugin instance.
     *
     * @param counter The value of the "x-request-counter" header.
     * @return A string indicating the counter value.
     */
    @Path("/headerTests")
    @GET
    @WasmPlugin("headerTests")
    public String uhttpHeaders(@HeaderParam("x-request-counter") String counter) {
        return String.format("counter: %s", counter);
    }

    /**
     * Endpoint for testing header manipulation with non-shared Wasm plugin instances.
     *
     * @param counter The value of the "x-request-counter" header.
     * @return A string indicating the counter value.
     */
    @Path("/headerTestsNotShared")
    @GET
    @WasmPlugin("headerTestsNotShared")
    public String unotSharedHttpHeaders(@HeaderParam("x-request-counter") String counter) {
        return String.format("counter: %s", counter);
    }

    /**
     * Endpoint for testing tick-based Wasm plugin functionality.
     *
     * @param sub The path parameter.
     * @return A simple "hello world" string.
     */
    @Path("/tickTests/{sub: .+ }")
    @GET
    @WasmPlugin("tickTests")
    public String tickTests(@PathParam("sub") String sub) {
        return "hello world";
    }

    /**
     * Endpoint for testing Foreign Function Interface (FFI) calls (reverse function).
     *
     * @param body The request body.
     * @return The request body (potentially modified by the Wasm plugin).
     */
    @Path("/ffiTests/reverse")
    @POST
    @WasmPlugin("ffiTests")
    public String ffiTests(String body) {
        return body;
    }

    /**
     * Endpoint for testing HTTP calls made from the Wasm plugin.
     *
     * @return A simple "hello world" string.
     */
    @Path("/httpCallTests")
    @GET
    @WasmPlugin("httpCallTests")
    public String httpCallTests() {
        return "hello world";
    }

    /**
     * Endpoint for testing combined FFI and HTTP call functionality.
     *
     * @return A simple "hello world" string.
     */
    @Path("/httpCallTestsAndFFI")
    @GET
    @WasmPlugin({"ffiTests", "httpCallTests"})
    public String httpCallTestsAndFFI() {
        return "hello world";
    }
}
