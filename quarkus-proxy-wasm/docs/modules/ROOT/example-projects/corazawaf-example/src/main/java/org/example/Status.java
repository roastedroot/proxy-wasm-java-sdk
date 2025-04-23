package org.example;

import io.roastedroot.proxywasm.jaxrs.WasmPlugin;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

/**
 * JAX-RS resource class for handling requests to /status/{status}.
 * Returns a response with the specified HTTP status code.
 * Applies the "waf" Wasm plugin.
 */
@Path("/status/{status}")
@WasmPlugin("waf") // use the corsaWAF filter
public class Status {

    /**
     * Default constructor.
     */
    public Status() {
        // Default constructor
    }

    /**
     * Handles GET requests.
     * @param status The desired HTTP status code.
     * @return A Response with the specified status.
     */
    @GET
    public Response gext(@PathParam("status") int status) {
        return Response.status(status).build();
    }

    /**
     * Handles DELETE requests.
     * @param status The desired HTTP status code.
     * @return A Response with the specified status.
     */
    @DELETE
    public Response delete(@PathParam("status") int status) {
        return Response.status(status).build();
    }

    /**
     * Handles OPTIONS requests.
     * @param status The desired HTTP status code.
     * @return A Response with the specified status.
     */
    @OPTIONS
    public Response options(@PathParam("status") int status) {
        return Response.status(status).build();
    }

    /**
     * Handles HEAD requests.
     * @param status The desired HTTP status code.
     * @return A Response with the specified status.
     */
    @HEAD
    public Response head(@PathParam("status") int status) {
        return Response.status(status).build();
    }

    /**
     * Handles POST requests.
     * @param status The desired HTTP status code.
     * @param body The request body (ignored).
     * @return A Response with the specified status.
     */
    @POST
    public Response postx(@PathParam("status") int status, String body) {
        return Response.status(status).build();
    }

    /**
     * Handles PUT requests.
     * @param status The desired HTTP status code.
     * @param body The request body (ignored).
     * @return A Response with the specified status.
     */
    @PUT
    public Response put(@PathParam("status") int status, String body) {
        return Response.status(status).build();
    }

    /**
     * Handles PATCH requests.
     * @param status The desired HTTP status code.
     * @param body The request body (ignored).
     * @return A Response with the specified status.
     */
    @PATCH
    public Response patch(@PathParam("status") int status, String body) {
        return Response.status(status).build();
    }
}
