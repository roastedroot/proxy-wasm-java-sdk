package io.roastedroot.proxywasm.internal;

import io.roastedroot.proxywasm.PluginFactory;
import io.roastedroot.proxywasm.StartException;
import java.util.Collection;
import java.util.List;

public interface Pool {

    Plugin borrow() throws StartException;

    String name();

    void release(Plugin plugin);

    default void close() {}

    class SharedPlugin implements Pool {
        private final Plugin plugin;

        public SharedPlugin(ServerAdaptor serverAdaptor, Plugin plugin) throws StartException {
            this.plugin = plugin;
            this.plugin.setServerAdaptor(serverAdaptor);
        }

        public void close() {
            plugin.wasm.close();
        }

        public Collection<Plugin> getPluginPools() {
            return List.of(plugin);
        }

        @Override
        public String name() {
            return plugin.name();
        }

        @Override
        public void release(Plugin plugin) {
            if (plugin != this.plugin) {
                throw new IllegalArgumentException("Plugin not from this pool");
            }
        }

        @Override
        public Plugin borrow() throws StartException {
            plugin.wasm.start();
            return plugin;
        }
    }

    class PluginPerRequest implements Pool {

        private final ServerAdaptor serverAdaptor;
        final PluginFactory factory;
        private final String name;

        public PluginPerRequest(ServerAdaptor serverAdaptor, PluginFactory factory, Plugin plugin) {
            this.serverAdaptor = serverAdaptor;
            this.factory = factory;
            this.name = plugin.name();
            release(plugin);
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public Plugin borrow() throws StartException {
            Plugin plugin = null;
            try {
                plugin = (Plugin) factory.create();
            } catch (Throwable e) {
                throw new StartException("Plugin create failed.", e);
            }
            plugin.setServerAdaptor(serverAdaptor);
            plugin.wasm.start();
            return plugin;
        }

        // Return the plugin to the pool
        @Override
        public void release(Plugin plugin) {
            // TODO: maybe implementing pooling in the future to reduce GC pressure
            // but for now, we just close the plugin
            plugin.close();
        }
    }
}
