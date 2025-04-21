package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.PluginFactory;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.internal.ServerAdaptor;
import io.roastedroot.proxywasm.jaxrs.internal.AbstractWasmPluginFeature;
import java.util.Arrays;

/**
 * WasmPluginFeature is a JAX-RS feature that allows the use of Proxy-Wasm plugins to filter JAX-RS
 * requests.
 */
public class WasmPluginFeature extends AbstractWasmPluginFeature {
    public WasmPluginFeature(ServerAdaptor httpServer, PluginFactory... factories)
            throws StartException {
        init(Arrays.asList(factories), httpServer);
    }
}
