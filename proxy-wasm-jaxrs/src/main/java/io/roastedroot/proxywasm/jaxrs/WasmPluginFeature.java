package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.plugin.PluginFactory;
import io.roastedroot.proxywasm.plugin.ServerAdaptor;
import java.util.Arrays;

public class WasmPluginFeature extends AbstractWasmPluginFeature {

    public WasmPluginFeature(ServerAdaptor httpServer, PluginFactory... factories)
            throws StartException {
        init(Arrays.asList(factories), httpServer);
    }
}
