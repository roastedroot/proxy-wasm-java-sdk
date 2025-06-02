package io.roastedroot.proxywasm.jaxrs;

import static io.roastedroot.proxywasm.internal.Helpers.string;

import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.internal.Action;
import io.roastedroot.proxywasm.internal.HttpRequestBody;
import io.roastedroot.proxywasm.internal.HttpResponseBody;
import io.roastedroot.proxywasm.internal.Plugin;
import io.roastedroot.proxywasm.internal.PluginHttpContext;
import io.roastedroot.proxywasm.internal.Pool;
import io.roastedroot.proxywasm.internal.SendResponse;
import io.roastedroot.proxywasm.jaxrs.internal.JaxrsHttpRequestAdaptor;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements the JAX-RS {@link ContainerRequestFilter},
 * {@link ContainerResponseFilter},
 * and {@link WriterInterceptor} interfaces to intercept HTTP requests and
 * responses,
 * allowing Proxy-Wasm plugins to process them.
 *
 * <p>
 * This filter is registered by the {@link ProxyWasmFeature}. It interacts with
 * {@link Plugin} instances obtained from configured {@link Pool}s to execute
 * the
 * appropriate Proxy-Wasm ABI functions (e.g., {@code on_http_request_headers},
 * {@code on_http_response_body}) at different stages of the JAX-RS
 * request/response lifecycle.
 *
 * @see ProxyWasmFeature
 * @see ProxyWasm
 * @see Plugin
 */
public class ProxyWasmFilter
        implements ContainerRequestFilter, WriterInterceptor, ContainerResponseFilter {

    private static final String FILTER_CONTEXT = PluginHttpContext.class.getName() + ".context";

    private static final Logger LOGGER = Logger.getLogger(ProxyWasmFilter.class.getName());

    private final List<Pool> pluginPools;

    /**
     * Constructs a ProxyWasmFilter.
     *
     * @param pluginPools A list of {@link Pool} instances, each managing a pool of
     *                    {@link Plugin}
     *                    instances for a specific Wasm module.
     */
    public ProxyWasmFilter(List<Pool> pluginPools) {
        this.pluginPools = List.copyOf(pluginPools);
    }

    private static class FilterContext {
        final Pool pool;
        final Plugin plugin;
        final PluginHttpContext httpContext;

        FilterContext(Pool pool, Plugin plugin, PluginHttpContext httpContext) {
            this.pool = pool;
            this.plugin = plugin;
            this.httpContext = httpContext;
        }

        public void release() {
            pool.release(plugin);
        }
    }

    /**
     * Intercepts incoming JAX-RS requests before they reach the resource method.
     *
     * <p>
     * This method iterates through the configured plugin pools, borrows a
     * {@link Plugin}
     * instance from each, creates a {@link PluginHttpContext}, and calls the
     * plugin's
     * {@code on_http_request_headers} and potentially {@code on_http_request_body}
     * functions.
     * It handles potential early responses or modifications dictated by the
     * plugins.
     *
     * @param requestContext The JAX-RS request context.
     * @throws IOException If an I/O error occurs, typically during body processing.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        ArrayList<FilterContext> filterContexts = new ArrayList<>();
        requestContext.setProperty(FILTER_CONTEXT, filterContexts);

        for (var pluginPool : pluginPools) {
            try {
                Plugin plugin = pluginPool.borrow();
                plugin.lock();
                try {
                    var serverAdaptor = plugin.getServerAdaptor();
                    var requestAdaptor =
                            (JaxrsHttpRequestAdaptor)
                                    serverAdaptor.httpRequestAdaptor(requestContext);
                    requestAdaptor.setRequestContext(requestContext);
                    var httpContext = plugin.createHttpContext(requestAdaptor);

                    filterContexts.add(new FilterContext(pluginPool, plugin, httpContext));
                } finally {
                    plugin.unlock();
                }
            } catch (StartException e) {
                LOGGER.log(Level.SEVERE, "Failed to start plugin: " + pluginPool.name(), e);

                // release any plugins that were borrowed before the exception
                for (var filterContext : filterContexts) {
                    filterContext.release();
                }
                filterContexts.clear();
                requestContext.abortWith(internalServerError());
                return;
            }
        }

        // Create a shared lazy body supplier for all plugins
        HttpRequestBody bodySupplier = new HttpRequestBody(() -> requestContext.getEntityStream());

        // Set up lazy providers for all plugins that need the body
        for (var filterContext : filterContexts) {
            if (filterContext.httpContext.context().hasOnRequestBody()) {
                filterContext.httpContext.setHttpRequestBodyState(bodySupplier);
            }
        }

        for (var filterContext : filterContexts) {
            filter(requestContext, filterContext);
        }
    }

    private void filter(ContainerRequestContext requestContext, FilterContext filterContext)
            throws IOException {

        var httpContext = filterContext.httpContext;
        httpContext.plugin().lock();
        try {

            // the plugin may not be interested in the request headers.
            if (httpContext.context().hasOnRequestHeaders()) {

                var action = httpContext.context().callOnRequestHeaders(false);
                if (action == Action.PAUSE) {
                    httpContext.maybePause();
                }

                // does the plugin want to respond early?
                var sendResponse = httpContext.consumeSentHttpResponse();
                if (sendResponse != null) {
                    requestContext.abortWith(toResponse(sendResponse));
                    return;
                }
            }

            // the plugin may not be interested in the request body.
            if (!httpContext.context().hasOnRequestBody()) {
                return;
            }

            HttpRequestBody httpRequestBodyState = httpContext.getHttpRequestBodyState();

            while (true) {
                // if we streamed body updates, then endOfStream would be initially false
                var action = httpContext.context().callOnRequestBody(true);

                // does the plugin want to respond early?
                var sendResponse = httpContext.consumeSentHttpResponse();
                if (sendResponse != null) {
                    requestContext.abortWith(toResponse(sendResponse));
                    return;
                }

                if (action == Action.CONTINUE) {
                    break;
                }
                httpContext.maybePause();
            }

            // Body was accessed and potentially modified, update the request stream
            if (httpRequestBodyState.isLoaded()) {
                byte[] bytes = httpRequestBodyState.getBodyIfLoaded();
                requestContext.setEntityStream(new java.io.ByteArrayInputStream(bytes));
            }

        } finally {
            httpContext.plugin().unlock(); // allow another request to use the plugin.
        }
    }

    private static Response internalServerError() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Intercepts outgoing JAX-RS responses before the entity is written.
     *
     * <p>
     * This method iterates through the configured plugin pools, retrieves the
     * {@link PluginHttpContext} created during the request phase, and calls the
     * plugin's
     * {@code on_http_response_headers} function. It handles potential modifications
     * to the
     * response headers dictated by the plugins. If the response has no entity but
     * the plugin
     * implements {@code on_http_response_body}, it invokes that callback as well.
     *
     * @param requestContext  The JAX-RS request context.
     * @param responseContext The JAX-RS response context.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void filter(
            ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        var filterContexts = (ArrayList<FilterContext>) requestContext.getProperty(FILTER_CONTEXT);
        if (filterContexts == null) {
            return;
        }

        for (var filterContext : filterContexts) {
            filter(requestContext, responseContext, filterContext);
        }
    }

    private void filter(
            ContainerRequestContext requestContext,
            ContainerResponseContext responseContext,
            FilterContext filterContext)
            throws IOException {

        var httpContext = filterContext.httpContext;

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
                    return;
                }

                // aroundWriteTo won't be called if there is no entity to send.
                if (responseContext.getEntity() != null
                        || !httpContext.context().hasOnResponseBody()) {
                    return;
                }

                // Set up empty response body for plugins that need it
                HttpResponseBody responseBodyState = new HttpResponseBody(new byte[0]);
                httpContext.setHttpResponseBodyState(responseBodyState);

                while (true) {
                    action = httpContext.context().callOnResponseBody(true);

                    // does the plugin want to respond early?
                    sendResponse = httpContext.consumeSentHttpResponse();
                    if (sendResponse != null) {
                        Response response = toResponse(sendResponse);
                        responseContext.setStatus(response.getStatus());
                        responseContext.getHeaders().putAll(response.getHeaders());
                        responseContext.setEntity(response.getEntity());
                        return;
                    }

                    if (action == Action.CONTINUE) {
                        break;
                    }
                    httpContext.maybePause();
                }

            } finally {
                // allow other request to use the plugin.
                httpContext.plugin().unlock();
            }
        }
    }

    /**
     * Intercepts the response body writing process.
     *
     * <p>
     * This method is called when the JAX-RS framework is about to serialize and
     * write
     * the response entity. It captures the original response body, allows plugins
     * (via {@code on_http_response_body}) to inspect or modify it, and then writes
     * the
     * potentially modified body to the original output stream. It handles potential
     * early responses dictated by the plugins during body processing.
     *
     * @param ctx The JAX-RS writer interceptor context.
     * @throws IOException             If an I/O error occurs during stream
     *                                 processing.
     * @throws WebApplicationException If a plugin decides to abort processing and
     *                                 send an
     *                                 alternative response during body filtering.
     */
    @Override
    public void aroundWriteTo(WriterInterceptorContext ctx)
            throws IOException, WebApplicationException {

        var filterContexts = (ArrayList<FilterContext>) ctx.getProperty(FILTER_CONTEXT);
        if (filterContexts == null) {
            return;
        }

        try {

            var original = ctx.getOutputStream();

            HttpResponseBody sharedResponseBody =
                    new HttpResponseBody(
                            () -> {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                ctx.setOutputStream(baos);
                                try {
                                    ctx.proceed();
                                } catch (Exception e) {
                                    throw new RuntimeException("Failed to read response body", e);
                                }
                                return baos.toByteArray();
                            });

            for (var filterContext : List.copyOf(filterContexts)) {
                var httpContext = filterContext.httpContext;

                httpContext.plugin().lock();

                // the plugin may not be interested in the response body.
                if (!httpContext.context().hasOnResponseBody()) {
                    continue;
                }

                // Set up lazy response body - will only be accessed if plugin needs it
                httpContext.setHttpResponseBodyState(sharedResponseBody);

                while (true) {
                    var action = httpContext.context().callOnResponseBody(true);

                    // does the plugin want to respond early?
                    var sendResponse = httpContext.consumeSentHttpResponse();
                    if (sendResponse != null) {
                        throw new WebApplicationException(toResponse(sendResponse));
                    }

                    if (action == Action.CONTINUE) {
                        break;
                    }
                    httpContext.maybePause();
                }
            }

            // Write the response body - if it was accessed and modified, use that,
            // otherwise continue with the original stream.
            if (sharedResponseBody.isLoaded()) {
                original.write(sharedResponseBody.get());
            } else {
                // Body was never accessed by any plugin, use original
                ctx.proceed();
            }

        } finally {
            for (var filterContext : List.copyOf(filterContexts)) {

                var httpContext = filterContext.httpContext;

                // allow other request to use the plugin.
                httpContext.context().close();
                httpContext.plugin().unlock();

                // TODO: will aroundWriteTo always get called so that we can avoid leaking the
                // plugin?
                filterContext.release();
            }
        }
    }

    Response toResponse(SendResponse other) {
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
