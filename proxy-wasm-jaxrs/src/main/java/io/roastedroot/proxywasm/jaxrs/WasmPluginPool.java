package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.StartException;
import jakarta.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;

public interface WasmPluginPool {

    WasmPlugin borrow() throws StartException;

    String name();

    void release(WasmPlugin plugin);

    class AppScoped implements WasmPluginPool {
        private final WasmPlugin plugin;

        public AppScoped(WasmPlugin plugin) throws StartException {
            this.plugin = plugin;
        }

        @PreDestroy
        public void close() {
            plugin.wasm.close();
        }

        public Collection<WasmPlugin> getPluginPools() {
            return List.of(plugin);
        }

        @Override
        public String name() {
            return plugin.name();
        }

        @Override
        public void release(WasmPlugin plugin) {
            if (plugin != this.plugin) {
                throw new IllegalArgumentException("Plugin not from this pool");
            }
        }

        @Override
        public WasmPlugin borrow() throws StartException {
            plugin.wasm.start();
            return plugin;
        }
    }

    class RequestScoped implements WasmPluginPool {

        final WasmPluginFactory factory;
        private final String name;

        public RequestScoped(WasmPluginFactory factory, WasmPlugin plugin) {
            this.factory = factory;
            this.name = plugin.name();
            release(plugin);
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public WasmPlugin borrow() throws StartException {
            WasmPlugin plugin = factory.create();
            plugin.wasm.start();
            return plugin;
        }

        // Return the plugin to the pool
        @Override
        public void release(WasmPlugin plugin) {
            // TODO: maybe implementing pooling in the future to reduce GC pressure
            // but for now, we just close the plugin
            plugin.close();
        }
    }
}
