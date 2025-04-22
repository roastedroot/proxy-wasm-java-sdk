package io.roastedroot.proxywasm;

import io.roastedroot.proxywasm.internal.WasmResult;
import java.util.HashMap;

/**
 * A basic, in-memory implementation of the {@link SharedDataHandler} interface.
 *
 * <p>This handler manages shared key-value data entirely within the host's memory using a
 * {@link HashMap}. It supports Compare-And-Swap (CAS) operations for optimistic concurrency control.
 * It is suitable for single-process environments or testing scenarios where data persistence or
 * cross-process sharing is not required.
 *
 * <p>All operations on this handler are synchronized to ensure thread safety within a single JVM.
 */
public class SimpleSharedDataHandler implements SharedDataHandler {

    /**
     * Default constructor.
     */
    public SimpleSharedDataHandler() {
        // Default constructor for SimpleSharedDataHandler
    }

    private final HashMap<String, SharedData> sharedData = new HashMap<>();

    /**
     * Retrieves the shared data associated with the given key from the in-memory store.
     *
     * @param key The key identifying the shared data item.
     * @return A {@link SharedData} object containing the value and its current CAS value,
     *         or {@code null} if the key is not found in the map.
     * @throws WasmException (Not currently thrown by this implementation, but part of the interface contract).
     */
    @Override
    public synchronized SharedData getSharedData(String key) throws WasmException {
        // Note: The interface contract might expect a NOT_FOUND exception, but returning null
        // is also common for map lookups. The caller (ProxyWasm runtime) should handle null.
        return sharedData.get(key);
    }

    /**
     * Sets or updates the shared data associated with the given key in the in-memory store,
     * potentially performing a Compare-And-Swap (CAS) check.
     *
     * <p>CAS behavior:
     * <ul>
     *     <li>If the key does not exist: The operation succeeds only if {@code cas} is 0. A new entry
     *         is created with the given value and a CAS value of 0 (or 1, depending on interpretation,
     *         this implementation uses 0 initially, then increments).</li>
     *     <li>If the key exists: The operation succeeds only if {@code cas} is 0 (unconditional update)
     *         or if {@code cas} matches the current CAS value stored for the key. On successful update,
     *         the CAS value is incremented.</li>
     * </ul>
     *
     * Setting {@code value} to {@code null} effectively removes the key if the CAS check passes
     * (or if cas is 0), as {@code HashMap} allows null values.
     *
     * @param key   The key identifying the shared data item.
     * @param value The new data value to store (can be null).
     * @param cas   The Compare-And-Swap value for conditional update, or 0 for unconditional update.
     * @return {@link WasmResult#OK} if the update was successful, or
     *         {@link WasmResult#CAS_MISMATCH} if the CAS check failed.
     */
    @Override
    public synchronized WasmResult setSharedData(String key, byte[] value, int cas) {
        SharedData current = sharedData.get(key);
        int nextCas = (current == null) ? 1 : current.cas() + 1; // Simple incrementing CAS

        if (current == null) {
            // Key does not exist
            if (cas == 0) {
                // Unconditional set/create
                sharedData.put(key, new SharedData(value, nextCas));
                return WasmResult.OK;
            } else {
                // CAS specified for a non-existent key
                return WasmResult.CAS_MISMATCH; // Or NOT_FOUND, depending on desired semantics
            }
        } else {
            // Key exists
            if (cas == 0 || current.cas() == cas) {
                // Unconditional update OR CAS matches
                sharedData.put(key, new SharedData(value, nextCas));
                return WasmResult.OK;
            } else {
                // CAS mismatch
                return WasmResult.CAS_MISMATCH;
            }
        }
    }
}
