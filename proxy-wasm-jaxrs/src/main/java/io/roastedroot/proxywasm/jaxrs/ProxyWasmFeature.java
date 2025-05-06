package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.PluginFactory;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.internal.ServerAdaptor;
import io.roastedroot.proxywasm.jaxrs.internal.AbstractProxyWasmFeature;
import java.util.Arrays;
import java.util.List;

/**
 * A JAX-RS {@link jakarta.ws.rs.core.Feature} that enables Proxy-Wasm plugin filtering
 * for JAX-RS applications. This feature registers the necessary {@link ProxyWasmFilter}
 * to intercept requests and responses for resources annotated with {@link ProxyWasm}.
 *
 * <p>To use this feature, register an instance of it with your JAX-RS application, providing
 * the required {@link ServerAdaptor} and a list of {@link PluginFactory} instances.
 *
 * <p>If you are using a CDI container like quarkus, you will be using the
 * {@link io.roastedroot.proxywasm.jaxrs.cdi.ProxyWasmFeature} instead.
 *
 * <pre>
 * public class MyApplication extends jakarta.ws.rs.core.Application {
 *     &#64;Override
 *     public Set&lt;Class&lt;?&gt;&gt; getClasses() {
 *         Set&lt;Class&lt;?&gt;&gt; resources = new HashSet&lt;&gt;();
 *         resources.add(MyResource.class);
 *         return resources;
 *     }
 *
 *     &#64;Override
 *     public Set&lt;Object&gt; getSingletons() {
 *         Set&lt;Object&gt; singletons = new HashSet&lt;&gt;();
 *         try {
 *             // Assuming a ServerAdaptor and PluginFactory are available
 *             ServerAdaptor serverAdaptor = ...;
 *             PluginFactory myPluginFactory = ...;
 *             singletons.add(new ProxyWasmFeature(serverAdaptor, myPluginFactory));
 *         } catch (StartException e) {
 *             throw new RuntimeException("Failed to initialize ProxyWasmFeature", e);
 *         }
 *         return singletons;
 *     }
 * }
 * </pre>
 *
 * @see ProxyWasm
 * @see ProxyWasmFilter
 * @see PluginFactory
 * @see ServerAdaptor
 */
public class ProxyWasmFeature extends AbstractProxyWasmFeature {

    /**
     * Constructs a new ProxyWasmFeature.
     *
     * @param httpServer The {@link ServerAdaptor} used to adapt JAX-RS specific request/response
     *                   objects for the Proxy-Wasm host.
     * @param factories  One or more {@link PluginFactory} instances used to create and manage
     *                   the Proxy-Wasm plugins.
     * @throws StartException If an error occurs during the initialization or startup of the
     *                        underlying Proxy-Wasm plugins.
     */
    public ProxyWasmFeature(ServerAdaptor httpServer, PluginFactory... factories)
            throws StartException {
        this(httpServer, Arrays.asList(factories));
    }

    /**
     * Constructs a new ProxyWasmFeature with a list of factories.
     *
     * @param httpServer The {@link ServerAdaptor} used to adapt JAX-RS specific request/response
     *                   objects for the Proxy-Wasm host.
     * @param factories  A list of {@link PluginFactory} instances used to create and manage
     *                   the Proxy-Wasm plugins.
     * @throws StartException If an error occurs during the initialization or startup of the
     *                        underlying Proxy-Wasm plugins.
     */
    public ProxyWasmFeature(ServerAdaptor httpServer, List<PluginFactory> factories)
            throws StartException {
        init(factories, httpServer);
    }
}
