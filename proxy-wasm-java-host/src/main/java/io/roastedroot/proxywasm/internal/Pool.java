package io.roastedroot.proxywasm.internal;

import io.roastedroot.proxywasm.PluginFactory;
import io.roastedroot.proxywasm.StartException;

public interface Pool {

    Plugin borrow() throws StartException;

    String name();

    void release(Plugin plugin);

    default void close() {}

    class SharedPlugin implements Pool {

        private final ServerAdaptor serverAdaptor;
        private final PluginFactory factory;
        private Plugin plugin;

        public SharedPlugin(ServerAdaptor serverAdaptor, PluginFactory factory) {
            this.serverAdaptor = serverAdaptor;
            this.factory = factory;
        }

        @Override
        public String name() {
            return this.factory.name();
        }

        @Override
        public Plugin borrow() throws StartException {
            if (plugin != null) {
                return plugin;
            }
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
            if (plugin != this.plugin) {
                throw new IllegalArgumentException("Plugin not from this pool");
            }
        }
    }

    class PluginPerRequest implements Pool {

        private final ServerAdaptor serverAdaptor;
        final PluginFactory factory;

        public PluginPerRequest(ServerAdaptor serverAdaptor, PluginFactory factory) {
            this.serverAdaptor = serverAdaptor;
            this.factory = factory;
        }

        @Override
        public String name() {
            return this.factory.name();
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
