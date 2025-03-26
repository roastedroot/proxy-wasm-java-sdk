package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.HttpContext;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.jaxrs.spi.HttpServerRequest;
import jakarta.enterprise.inject.Instance;
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

public class ProxyWasmFilter
        implements ContainerRequestFilter, WriterInterceptor, ContainerResponseFilter {
    private static final String FILTER_CONTEXT_PROPERTY_NAME = "WasmHttpFilterContext";

    private final WasmPluginPool pluginPool;

    Instance<HttpServerRequest> httpServer;

    public ProxyWasmFilter(WasmPluginPool pluginPool, Instance<HttpServerRequest> httpServer) {
        this.pluginPool = pluginPool;
        this.httpServer = httpServer;
    }

    String name() {
        return pluginPool.name();
    }

    // TODO: the HttpContext and ProxyWasm object's should be closed once the request is done.
    //       is there an easy way to hook up cleanup code for this?
    static class WasmHttpFilterContext {
        final WasmPlugin plugin;
        final PluginHandler pluginHandler;
        final HttpHandler httpHandler;
        final HttpContext httpContext;

        public WasmHttpFilterContext(WasmPlugin plugin, HttpServerRequest httpServer) {
            this.plugin = plugin;
            this.pluginHandler = plugin.handler;
            this.httpHandler = new HttpHandler(plugin.handler, httpServer);
            this.httpContext = plugin.wasm.createHttpContext(this.httpHandler);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        WasmPlugin plugin;
        try {
            plugin = pluginPool.borrow();
        } catch (StartException e) {
            requestContext.abortWith(interalServerError());
            return;
        }

        plugin.lock();
        try {
            var wasmHttpFilterContext = new WasmHttpFilterContext(plugin, this.httpServer.get());
            requestContext.setProperty(FILTER_CONTEXT_PROPERTY_NAME, wasmHttpFilterContext);

            // the plugin may not be interested in the request headers.
            if (wasmHttpFilterContext.httpContext.hasOnRequestHeaders()) {

                wasmHttpFilterContext.httpHandler.setRequestContext(requestContext);
                var action = wasmHttpFilterContext.httpContext.callOnRequestHeaders(false);
                if (action == Action.CONTINUE) {
                    // continue means plugin is done reading the headers.
                    wasmHttpFilterContext.httpHandler.setRequestContext(null);
                } else {
                    wasmHttpFilterContext.httpHandler.maybePause(wasmHttpFilterContext.plugin);
                }

                // does the plugin want to respond early?
                HttpHandler.HttpResponse sendResponse =
                        wasmHttpFilterContext.httpHandler.consumeSentHttpResponse();
                if (sendResponse != null) {
                    requestContext.abortWith(sendResponse.toResponse());
                }
            }

            // the plugin may not be interested in the request body.
            if (wasmHttpFilterContext.httpContext.hasOnRequestBody()) {

                // TODO: find out if it's more efficient to read the body in chunks and do multiple
                // callOnRequestBody calls.
                byte[] bytes = requestContext.getEntityStream().readAllBytes();

                wasmHttpFilterContext.httpHandler.setHttpRequestBody(bytes);
                var action = wasmHttpFilterContext.httpContext.callOnRequestBody(true);
                bytes = wasmHttpFilterContext.httpHandler.getHttpRequestBody();
                if (action == Action.CONTINUE) {
                    // continue means plugin is done reading the body.
                    wasmHttpFilterContext.httpHandler.setHttpRequestBody(null);
                } else {
                    wasmHttpFilterContext.httpHandler.maybePause(wasmHttpFilterContext.plugin);
                }

                // TODO: find out more details about what to do here in a PAUSE condition.
                //       does it mean that we park the request here and wait for another event like
                //       tick to resume us before forwarding to the next filter?

                // does the plugin want to respond early?
                HttpHandler.HttpResponse sendResponse =
                        wasmHttpFilterContext.httpHandler.consumeSentHttpResponse();
                if (sendResponse != null) {
                    throw new WebApplicationException(sendResponse.toResponse());
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
        var wasmHttpFilterContext =
                (WasmHttpFilterContext) requestContext.getProperty(FILTER_CONTEXT_PROPERTY_NAME);
        if (wasmHttpFilterContext == null) {
            throw new WebApplicationException(interalServerError());
        }

        // the plugin may not be interested in the request headers.
        if (wasmHttpFilterContext.httpContext.hasOnResponseHeaders()) {
            try {
                wasmHttpFilterContext.plugin.lock();

                wasmHttpFilterContext.httpHandler.setResponseContext(responseContext);
                var action = wasmHttpFilterContext.httpContext.callOnResponseHeaders(false);
                if (action == Action.CONTINUE) {
                    // continue means plugin is done reading the headers.
                    wasmHttpFilterContext.httpHandler.setResponseContext(null);
                } else {
                    wasmHttpFilterContext.httpHandler.maybePause(wasmHttpFilterContext.plugin);
                }

                // does the plugin want to respond early?
                HttpHandler.HttpResponse sendResponse =
                        wasmHttpFilterContext.httpHandler.consumeSentHttpResponse();
                if (sendResponse != null) {
                    Response response = sendResponse.toResponse();
                    responseContext.setStatus(response.getStatus());
                    responseContext.getHeaders().putAll(response.getHeaders());
                    responseContext.setEntity(response.getEntity());
                }
            } finally {
                // allow other request to use the plugin.
                wasmHttpFilterContext.plugin.unlock();
            }
        }
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext ctx)
            throws IOException, WebApplicationException {
        var wasmHttpFilterContext =
                (WasmHttpFilterContext) ctx.getProperty(FILTER_CONTEXT_PROPERTY_NAME);
        if (wasmHttpFilterContext == null) {
            throw new WebApplicationException(interalServerError());
        }

        try {

            // the plugin may not be interested in the request body.
            if (wasmHttpFilterContext.httpContext.hasOnResponseBody()) {
                var original = ctx.getOutputStream();
                ctx.setOutputStream(
                        new ByteArrayOutputStream() {
                            boolean closed = false;

                            @Override
                            public void close() throws IOException {
                                if (closed) {
                                    return;
                                }
                                closed = true;
                                super.close();

                                // TODO: find out if it's more efficient to read the body in chunks
                                // and
                                // do
                                //  multiple callOnRequestBody calls.

                                byte[] bytes = this.toByteArray();

                                wasmHttpFilterContext.plugin.lock();

                                wasmHttpFilterContext.httpHandler.setHttpResponseBody(bytes);
                                var action =
                                        wasmHttpFilterContext.httpContext.callOnResponseBody(false);
                                bytes = wasmHttpFilterContext.httpHandler.getHttpResponseBody();
                                if (action == Action.CONTINUE) {
                                    // continue means plugin is done reading the body.
                                    wasmHttpFilterContext.httpHandler.setHttpResponseBody(null);
                                } else {
                                    wasmHttpFilterContext.httpHandler.maybePause(
                                            wasmHttpFilterContext.plugin);
                                }

                                // does the plugin want to respond early?
                                HttpHandler.HttpResponse sendResponse =
                                        wasmHttpFilterContext.httpHandler.consumeSentHttpResponse();
                                if (sendResponse != null) {
                                    throw new WebApplicationException(sendResponse.toResponse());
                                }

                                // plugin may have modified the body
                                original.write(bytes);
                                original.close();
                            }
                        });
            }

            ctx.proceed();
        } finally {
            // allow other request to use the plugin.
            wasmHttpFilterContext.httpContext.close();
            wasmHttpFilterContext.plugin.unlock();

            // TODO: will aroundWriteTo always get called so that we can avoid leaking the plugin?
            this.pluginPool.release(wasmHttpFilterContext.plugin);
        }
    }
}
