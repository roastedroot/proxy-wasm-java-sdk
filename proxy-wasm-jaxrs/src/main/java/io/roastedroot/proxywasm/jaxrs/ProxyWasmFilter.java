package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.HttpContext;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.jaxrs.spi.HttpServer;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@PreMatching
public class ProxyWasmFilter
        implements ContainerRequestFilter,
                ReaderInterceptor,
                WriterInterceptor,
                ContainerResponseFilter {
    private static final String FILTER_CONTEXT_PROPERTY_NAME = "WasmHttpFilterContext";

    private final WasmPluginPool pluginPool;

    Instance<HttpServer> httpServer;

    @Inject
    public ProxyWasmFilter(WasmPluginPool pluginPool, Instance<HttpServer> httpServer) {
        this.pluginPool = pluginPool;
        this.httpServer = httpServer;
    }

    // TODO: the HttpContext and ProxyWasm object's should be closed once the request is done.
    //       is there an easy way to hook up cleanup code for this?
    static class WasmHttpFilterContext {
        final WasmPlugin plugin;
        final PluginHandler pluginHandler;
        final HttpHandler httpHandler;
        final HttpContext httpContext;

        public WasmHttpFilterContext(WasmPlugin plugin, HttpServer httpServer) {
            this.plugin = plugin;
            this.pluginHandler = plugin.pluginHandler();
            this.httpHandler = new HttpHandler(plugin.pluginHandler(), httpServer);
            this.httpContext = plugin.proxyWasm().createHttpContext(this.httpHandler);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        WasmPlugin plugin = null;
        try {
            plugin = pluginPool.borrow();
            plugin.lock();

            var ctx = new WasmHttpFilterContext(plugin, this.httpServer.get());
            requestContext.setProperty(FILTER_CONTEXT_PROPERTY_NAME, ctx);

            // the plugin may not be interested in the request headers.
            if (ctx.httpContext.hasOnRequestHeaders()) {

                ctx.httpHandler.setRequestContext(requestContext);
                var action = ctx.httpContext.callOnRequestHeaders(false);
                if (action == Action.CONTINUE) {
                    // continue means plugin is done reading the headers.
                    ctx.httpHandler.setRequestContext(null);
                }

                // does the plugin want to respond early?
                HttpHandler.HttpResponse sendResponse = ctx.httpHandler.consumeSentHttpResponse();
                if (sendResponse != null) {
                    requestContext.abortWith(sendResponse.toResponse());
                }
            }

        } catch (StartException e) {
            requestContext.abortWith(
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        } finally {
            plugin.unlock(); // allow another request to use the plugin.
        }
    }

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext ctx)
            throws IOException, WebApplicationException {

        var wasmHttpFilterContext =
                (WasmHttpFilterContext) ctx.getProperty(FILTER_CONTEXT_PROPERTY_NAME);
        if (wasmHttpFilterContext == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        }

        // the plugin may not be interested in the request body.
        if (wasmHttpFilterContext.httpContext.hasOnRequestBody()) {
            // TODO: find out if it's more efficient to read the body in chunks and do multiple
            // callOnRequestBody calls.
            byte[] bytes = ctx.getInputStream().readAllBytes();

            try {
                // we are about to call into the plugin which may mutate the plugin state..
                wasmHttpFilterContext.plugin.lock();

                wasmHttpFilterContext.httpHandler.setHttpRequestBody(bytes);
                var action = wasmHttpFilterContext.httpContext.callOnRequestBody(true);
                bytes = wasmHttpFilterContext.httpHandler.getHttpRequestBody();
                if (action == Action.CONTINUE) {
                    // continue means plugin is done reading the body.
                    wasmHttpFilterContext.httpHandler.setHttpRequestBody(null);
                }

                // TODO: find out more details about what to do here in a PAUSE condition.
                //       does it mean that we park the request here and wait for another event like
                //       tick to resume us before forwarding to the next filter?

                // does the plugin want to respond early?
                HttpHandler.HttpResponse sendResponse =
                        wasmHttpFilterContext.httpHandler.getSentHttpResponse();
                if (sendResponse != null) {
                    throw new WebApplicationException(sendResponse.toResponse());
                }
            } finally {
                // allow other request to use the plugin.
                wasmHttpFilterContext.plugin.unlock();
            }

            // plugin may have modified the body
            ctx.setInputStream(new java.io.ByteArrayInputStream(bytes));
        }
        return ctx.proceed();
    }

    @Override
    public void filter(
            ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        var wasmHttpFilterContext =
                (WasmHttpFilterContext) requestContext.getProperty(FILTER_CONTEXT_PROPERTY_NAME);
        if (wasmHttpFilterContext == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
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
                }

                // does the plugin want to respond early?
                HttpHandler.HttpResponse sendResponse =
                        wasmHttpFilterContext.httpHandler.getSentHttpResponse();
                if (sendResponse != null) {
                    requestContext.abortWith(sendResponse.toResponse());
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
            throw new WebApplicationException(
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
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
                                }

                                // does the plugin want to respond early?
                                HttpHandler.HttpResponse sendResponse =
                                        wasmHttpFilterContext.httpHandler.getSentHttpResponse();
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
