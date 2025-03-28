package io.roastedroot.proxywasm.jaxrs;

import static io.roastedroot.proxywasm.Helpers.string;

import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.plugin.HttpContext;
import io.roastedroot.proxywasm.plugin.Plugin;
import io.roastedroot.proxywasm.plugin.Pool;
import io.roastedroot.proxywasm.plugin.SendResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WasmPluginFilter
        implements ContainerRequestFilter, WriterInterceptor, ContainerResponseFilter {
    private static final String FILTER_CONTEXT_PROPERTY_NAME = HttpContext.class.getName();

    private final Pool pluginPool;

    public WasmPluginFilter(Pool pluginPool) {
        this.pluginPool = pluginPool;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        Plugin plugin;
        try {
            plugin = pluginPool.borrow();
        } catch (StartException e) {
            requestContext.abortWith(interalServerError());
            return;
        }

        plugin.lock();
        try {
            var requestAdaptor =
                    (JaxrsHttpRequestAdaptor)
                            plugin.getServerAdaptor().httpRequestAdaptor(requestContext);
            var httpContext = plugin.createHttpContext(requestAdaptor);
            requestContext.setProperty(FILTER_CONTEXT_PROPERTY_NAME, httpContext);

            // the plugin may not be interested in the request headers.
            if (httpContext.context().hasOnRequestHeaders()) {

                requestAdaptor.setRequestContext(requestContext);
                var action = httpContext.context().callOnRequestHeaders(false);
                if (action == Action.PAUSE) {
                    httpContext.maybePause();
                }

                // does the plugin want to respond early?
                var sendResponse = httpContext.consumeSentHttpResponse();
                if (sendResponse != null) {
                    requestContext.abortWith(toResponse(sendResponse));
                }
            }

            // the plugin may not be interested in the request body.
            if (httpContext.context().hasOnRequestBody()) {

                // TODO: find out if it's more efficient to read the body in chunks and do multiple
                // callOnRequestBody calls.
                byte[] bytes = requestContext.getEntityStream().readAllBytes();

                httpContext.setHttpRequestBody(bytes);
                var action = httpContext.context().callOnRequestBody(true);
                bytes = httpContext.getHttpRequestBody();
                if (action == Action.CONTINUE) {
                    // continue means plugin is done reading the body.
                    httpContext.setHttpRequestBody(null);
                } else {
                    httpContext.maybePause();
                }

                // does the plugin want to respond early?
                var sendResponse = httpContext.consumeSentHttpResponse();
                if (sendResponse != null) {
                    throw new WebApplicationException(toResponse(sendResponse));
                }

                // plugin may have modified the body
                requestContext.setEntityStream(new java.io.ByteArrayInputStream(bytes));
            }

        } finally {
            plugin.unlock(); // allow another request to use the plugin.
        }
    }

    private static Response interalServerError() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @Override
    public void filter(
            ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        var httpContext = (HttpContext) requestContext.getProperty(FILTER_CONTEXT_PROPERTY_NAME);
        if (httpContext == null) {
            throw new WebApplicationException(interalServerError());
        }

        // the plugin may not be interested in the request headers.
        if (httpContext.context().hasOnResponseHeaders()) {
            try {
                httpContext.plugin().lock();

                var requestAdaptor = (JaxrsHttpRequestAdaptor) httpContext.requestAdaptor();
                requestAdaptor.setResponseContext(responseContext);
                var action = httpContext.context().callOnResponseHeaders(false);
                if (action == Action.PAUSE) {
                    httpContext.maybePause();
                }

                // does the plugin want to respond early?
                var sendResponse = httpContext.consumeSentHttpResponse();
                if (sendResponse != null) {
                    Response response = toResponse(sendResponse);
                    responseContext.setStatus(response.getStatus());
                    responseContext.getHeaders().putAll(response.getHeaders());
                    responseContext.setEntity(response.getEntity());
                }
            } finally {
                // allow other request to use the plugin.
                httpContext.plugin().unlock();
            }
        }
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext ctx)
            throws IOException, WebApplicationException {
        var httpContext = (HttpContext) ctx.getProperty(FILTER_CONTEXT_PROPERTY_NAME);
        if (httpContext == null) {
            throw new WebApplicationException(interalServerError());
        }

        try {
            httpContext.plugin().lock();

            // the plugin may not be interested in the request body.
            if (!httpContext.context().hasOnResponseBody()) {
                ctx.proceed();
            }

            var original = ctx.getOutputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ctx.setOutputStream(baos);
            ctx.proceed();

            byte[] bytes = baos.toByteArray();
            httpContext.setHttpResponseBody(bytes);
            var action = httpContext.context().callOnResponseBody(false);
            bytes = httpContext.getHttpResponseBody();
            if (action == Action.CONTINUE) {
                // continue means plugin is done reading the body.
                httpContext.setHttpResponseBody(null);
            } else {
                httpContext.maybePause();
            }

            // does the plugin want to respond early?
            var sendResponse = httpContext.consumeSentHttpResponse();
            if (sendResponse != null) {
                throw new WebApplicationException(toResponse(sendResponse));
            }

            // plugin may have modified the body
            original.write(bytes);

        } finally {
            // allow other request to use the plugin.
            httpContext.context().close();
            httpContext.plugin().unlock();

            // TODO: will aroundWriteTo always get called so that we can avoid leaking the plugin?
            this.pluginPool.release(httpContext.plugin());
        }
    }

    public Response toResponse(SendResponse other) {
        Response.ResponseBuilder builder =
                Response.status(other.statusCode(), string(other.statusCodeDetails()));
        if (other.headers() != null) {
            for (var entry : other.headers().entries()) {
                builder = builder.header(entry.getKey(), entry.getValue());
            }
        }
        builder.entity(other.body());
        return builder.build();
    }
}
