package io.roastedroot.proxywasm;

/**
 * A functional interface representing a factory for creating {@link Plugin} instances.
 *
 * <p>This is typically used in scenarios where plugin instantiation needs to be deferred
 * or customized, potentially based on configuration or context available at runtime.
 * Implementations might handle loading WASM modules, configuring builders, and returning
 * the ready-to-use plugin.
 */
@FunctionalInterface
public interface PluginFactory {
    /**
     * Creates and returns a new {@link Plugin} instance.
     * Implementations are responsible for all necessary setup, including potentially
     * loading the WASM module and configuring it using {@link Plugin#builder(com.dylibso.chicory.wasm.WasmModule)}.
     *
     * @return A newly created {@link Plugin} instance.
     * @throws Exception If any error occurs during plugin creation (e.g., file loading, WASM instantiation,
     *                   initialization errors within the plugin's start lifecycle).
     */
    Plugin create() throws Exception;
}
