package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.StartException;

public interface WasmPluginFactory {
    WasmPlugin create() throws StartException;
}
