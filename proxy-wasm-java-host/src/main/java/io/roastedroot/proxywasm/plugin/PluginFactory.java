package io.roastedroot.proxywasm.plugin;

import io.roastedroot.proxywasm.StartException;

public interface PluginFactory {
    Plugin create() throws StartException;
}
