package io.roastedroot.proxywasm.corazawaf.example;

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

@Path("/status/{status}")
@WasmPlugin("waf") // use the corsaWAF filter
public class Status {

    @GET
    public Response gext(@PathParam("status") int status) {
        return Response.status(status).build();
    }

    @DELETE
    public Response delete(@PathParam("status") int status) {
        return Response.status(status).build();
    }

    @OPTIONS
    public Response options(@PathParam("status") int status) {
        return Response.status(status).build();
    }

    @HEAD
    public Response head(@PathParam("status") int status) {
        return Response.status(status).build();
    }

    @POST
    public Response postx(@PathParam("status") int status, String body) {
        return Response.status(status).build();
    }

    @PUT
    public Response put(@PathParam("status") int status, String body) {
        return Response.status(status).build();
    }

    @PATCH
    public Response patch(@PathParam("status") int status, String body) {
        return Response.status(status).build();
    }
}
