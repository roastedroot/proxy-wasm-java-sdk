package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.HttpContext;
import io.roastedroot.proxywasm.StartException;
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

    private final WasmPluginFactory pluginFactory;

    @Inject
    public ProxyWasmFilter(WasmPluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
    }

    // TODO: the HttpContext and ProxyWasm object's should be closed once the request is done.
    //       is there an easy way to hook up cleanup code for this?
    static class WasmHttpFilterContext {
        final PluginHandler pluginHandler;
        final HttpHandler handler;
        final HttpContext wasm;

        public WasmHttpFilterContext(WasmPlugin plugin) {
            this.pluginHandler = plugin.pluginHandler();
            this.handler = new HttpHandler(plugin.pluginHandler());
            this.wasm = plugin.proxyWasm().createHttpContext(this.handler);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        WasmPlugin plugin = null;
        try {
            plugin = pluginFactory.create();
        } catch (StartException e) {
            requestContext.abortWith(
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
        }

        var wasmHttpFilterContext = new WasmHttpFilterContext(plugin);
        requestContext.setProperty(FILTER_CONTEXT_PROPERTY_NAME, wasmHttpFilterContext);

        // the plugin may not be interested in the request headers.
        if (wasmHttpFilterContext.wasm.hasOnRequestHeaders()) {

            wasmHttpFilterContext.handler.setRequestContext(requestContext);
            var action = wasmHttpFilterContext.wasm.callOnRequestHeaders(false);
            if (action == Action.CONTINUE) {
                // continue means plugin is done reading the headers.
                wasmHttpFilterContext.handler.setRequestContext(null);
            }

            // does the plugin want to respond early?
            HttpHandler.HttpResponse sendResponse =
                    wasmHttpFilterContext.handler.getSentHttpResponse();
            if (sendResponse != null) {
                requestContext.abortWith(sendResponse.toResponse());
            }
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
        if (wasmHttpFilterContext.wasm.hasOnRequestBody()) {
            // TODO: find out if it's more efficient to read the body in chunks and do multiple
            // callOnRequestBody calls.
            byte[] bytes = ctx.getInputStream().readAllBytes();
            wasmHttpFilterContext.handler.setHttpRequestBody(bytes);
            var action = wasmHttpFilterContext.wasm.callOnRequestBody(true);
            bytes = wasmHttpFilterContext.handler.getHttpRequestBody();
            if (action == Action.CONTINUE) {
                // continue means plugin is done reading the body.
                wasmHttpFilterContext.handler.setHttpRequestBody(null);
            }

            // TODO: find out more details about what to do here in a PAUSE condition.
            //       does it mean that we park the request here and wait for another event like
            //       tick to resume us before forwarding to the next filter?

            // does the plugin want to respond early?
            HttpHandler.HttpResponse sendResponse =
                    wasmHttpFilterContext.handler.getSentHttpResponse();
            if (sendResponse != null) {
                throw new WebApplicationException(sendResponse.toResponse());
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
        if (wasmHttpFilterContext.wasm.hasOnResponseHeaders()) {

            wasmHttpFilterContext.handler.setResponseContext(responseContext);
            var action = wasmHttpFilterContext.wasm.callOnResponseHeaders(false);
            if (action == Action.CONTINUE) {
                // continue means plugin is done reading the headers.
                wasmHttpFilterContext.handler.setResponseContext(null);
            }

            // does the plugin want to respond early?
            HttpHandler.HttpResponse sendResponse =
                    wasmHttpFilterContext.handler.getSentHttpResponse();
            if (sendResponse != null) {
                requestContext.abortWith(sendResponse.toResponse());
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

        // the plugin may not be interested in the request body.
        if (wasmHttpFilterContext.wasm.hasOnResponseBody()) {

            var original = ctx.getOutputStream();
            ctx.setOutputStream(
                    new ByteArrayOutputStream() {
                        @Override
                        public void close() throws IOException {
                            super.close();

                            // TODO: find out if it's more efficient to read the body in chunks and
                            // do
                            //  multiple callOnRequestBody calls.

                            byte[] bytes = this.toByteArray();
                            wasmHttpFilterContext.handler.setHttpResponseBody(bytes);
                            var action = wasmHttpFilterContext.wasm.callOnResponseBody(true);
                            bytes = wasmHttpFilterContext.handler.getHttpResponseBody();
                            if (action == Action.CONTINUE) {
                                // continue means plugin is done reading the body.
                                wasmHttpFilterContext.handler.setHttpResponseBody(null);
                            }

                            // does the plugin want to respond early?
                            HttpHandler.HttpResponse sendResponse =
                                    wasmHttpFilterContext.handler.getSentHttpResponse();
                            if (sendResponse != null) {
                                throw new WebApplicationException(sendResponse.toResponse());
                            }

                            // plugin may have modified the body
                            original.write(bytes);
                            original.close();

                            // clean up...
                            //                            wasmHttpFilterContext.wasm.close();
                            //
                            // wasmHttpFilterContext.wasm.getProxyWasm().close();
                        }
                    });
        } else {
            // clean up...
            //            wasmHttpFilterContext.wasm.close();
            //            wasmHttpFilterContext.wasm.getProxyWasm().close();
        }

        ctx.proceed();
    }
}
