package io.roastedroot.proxywasm;

/**
 * Exception thrown when an error occurs during the initialization or startup phase of a Proxy-WASM plugin.
 *
 * <p>This typically happens during the execution of the {@link PluginFactory.Builder#build()} method,
 * encompassing issues such as WASM module instantiation failures, errors within the WASM
 * {@code _start} function, or failures during the initial {@code proxy_on_vm_start} or
 * {@code proxy_on_configure} calls within the plugin.
 */
public class StartException extends Exception {
    /**
     * Constructs a new StartException with the specified detail message.
     *
     * @param message the detail message.
     */
    public StartException(String message) {
        super(message);
    }

    /**
     * Constructs a new StartException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *                (A {@code null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public StartException(String message, Throwable cause) {
        super(message, cause);
    }
}
