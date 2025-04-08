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
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

/**
 * This class is a JAX-RS resource that handles various HTTP methods and paths.
 * It uses the @WasmPlugin annotation to specify the use of the "waf" filter.
 * <p>
 * All paths and methods are handled by this class and it responds with a simple
 * message indicating the method, path, and body (if applicable).
 */
@Path("/anything")
@WasmPlugin("waf") // use the corsaWAF filter
public class Anything {

    @GET
    public Response gext(@Context HttpHeaders headers) {
        return process(headers, null);
    }

    @DELETE
    public Response delete(HttpHeaders headers) {
        return process(headers, null);
    }

    @OPTIONS
    public Response options(HttpHeaders headers) {
        return process(headers, null);
    }

    @HEAD
    public Response head(HttpHeaders headers) {
        return process(headers, null);
    }

    @POST
    public Response postx(HttpHeaders headers, String body) {
        return process(headers, body);
    }

    @PUT
    public Response put(HttpHeaders headers, String body) {
        return process(headers, body);
    }

    @PATCH
    public Response patch(HttpHeaders headers, String body) {
        return process(headers, body);
    }

    private Response process(HttpHeaders headers, String body) {
        Response.ResponseBuilder builder = Response.ok();
        for (var header : headers.getRequestHeaders().entrySet()) {
            for (String value : header.getValue()) {
                builder = builder.header(header.getKey(), value);
            }
        }
        if (body != null) {
            builder = builder.entity(body);
        }
        return builder.build();
    }
}
