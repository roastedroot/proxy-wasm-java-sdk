package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.StartException;

public interface WasmPluginPool {

    WasmPlugin borrow() throws StartException;

    void release(WasmPlugin plugin);

    class AppScoped implements WasmPluginPool {
        private final WasmPlugin plugin;

        public AppScoped(WasmPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void release(WasmPlugin plugin) {
            if (plugin != this.plugin) {
                throw new IllegalArgumentException("Plugin not from this pool");
            }
        }

        @Override
        public WasmPlugin borrow() throws StartException {
            return plugin;
        }
    }

    class RequestScoped implements WasmPluginPool {

        final WasmPluginFactory factory;

        public RequestScoped(WasmPluginFactory factory, WasmPlugin plugin) {
            this.factory = factory;
            release(plugin);
        }

        @Override
        public WasmPlugin borrow() throws StartException {
            return factory.create();
        }

        // Return the plugin to the pool
        @Override
        public void release(WasmPlugin plugin) {
            // TODO: maybe implementing pooling in the future to reduce GC pressure
            // but for now, we just close the plugin
            plugin.proxyWasm().close();
        }
    }
}
