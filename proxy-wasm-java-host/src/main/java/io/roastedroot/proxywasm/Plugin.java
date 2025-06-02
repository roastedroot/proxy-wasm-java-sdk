package io.roastedroot.proxywasm;

/**
 * Represents a Proxy-WASM plugin, providing the bridge between the host
 * environment and the WASM module.
 *
 * <p>Concrete Plugin instances are created using a {@link PluginFactory}.
 * The actual WASM instance and interaction logic are managed internally.
 */
public interface Plugin {

    /**
     * Returns the configured name of this plugin instance.
     *
     * @return the plugin name, which might be null if not explicitly set via the builder.
     */
    String name();
}
