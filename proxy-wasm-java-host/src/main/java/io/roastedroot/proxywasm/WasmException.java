package io.roastedroot.proxywasm;

import io.roastedroot.proxywasm.internal.WasmResult;

/**
 * An exception used to signal specific outcomes or errors from host functions
 * back to the calling Proxy-WASM module.
 *
 * <p>This exception wraps a {@link WasmResult} enum value, which corresponds to the
 * standard result codes defined in the Proxy-WASM ABI specification. When a host function
 * (like those defined in {@link MetricsHandler}, {@link SharedQueueHandler}, etc.) needs
 * to return a status other than simple success (which is often indicated by a void return
 * or a non-exceptional return value), it throws a {@code WasmException} containing the
 * appropriate {@code WasmResult}.
 *
 * <p>The runtime catches this exception and translates its contained {@code WasmResult}
 * into the integer value expected by the WASM module according to the ABI.
 */
public class WasmException extends Exception {

    /** The specific Proxy-WASM result code associated with this exception. */
    private final WasmResult result;

    /**
     * Constructs a new WasmException with the specified result code.
     * The exception message is automatically set to the description of the {@link WasmResult}.
     *
     * @param result The {@link WasmResult} representing the outcome or error condition.
     */
    public WasmException(WasmResult result) {
        super(result.description());
        this.result = result;
    }

    /**
     * Gets the underlying {@link WasmResult} code associated with this exception.
     *
     * @return The non-null {@link WasmResult} enum value.
     */
    public WasmResult result() {
        return result;
    }
}
