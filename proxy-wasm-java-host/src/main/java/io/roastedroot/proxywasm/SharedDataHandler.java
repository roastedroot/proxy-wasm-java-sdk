package io.roastedroot.proxywasm;

import io.roastedroot.proxywasm.internal.WasmResult;

/**
 * Defines the contract for handling shared key-value data accessible by Proxy-WASM modules.
 * Implementations of this interface manage the storage, retrieval, and conditional update
 * (using CAS - Compare-And-Swap) of data that can be shared across different WASM module
 * instances or even different VMs, depending on the host environment's implementation.
 *
 * <p>Shared data provides a mechanism for state sharing, caching, or coordination between plugins.
 */
public interface SharedDataHandler {

    /**
     * A default, non-functional instance of {@code SharedDataHandler}.
     * This instance throws {@link WasmException} with {@link WasmResult#UNIMPLEMENTED}
     * for {@link #getSharedData(String)} and returns {@link WasmResult#UNIMPLEMENTED}
     * for {@link #setSharedData(String, byte[], int)}.
     * Useful as a placeholder or base when shared data functionality is not supported or needed.
     */
    SharedDataHandler DEFAULT = new SharedDataHandler() {};

    /**
     * Retrieves the shared data associated with the given key.
     * The result includes the data itself and a CAS (Compare-And-Swap) value, which represents
     * the version of the data. The CAS value is used for optimistic concurrency control
     * during updates via {@link #setSharedData(String, byte[], int)}.
     *
     * @param key The key identifying the shared data item.
     * @return A {@link SharedData} object containing the value and its CAS.
     * @throws WasmException If the key is not found ({@link WasmResult#NOT_FOUND}),
     *                       or if the operation is unimplemented by the host.
     */
    default SharedData getSharedData(String key) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    /**
     * Sets or updates the shared data associated with the given key.
     * This operation can be conditional based on the provided CAS value.
     *
     * <p>The {@code cas} parameter enables Compare-And-Swap:
     * <ul>
     *     <li>If {@code cas} is 0, the operation is unconditional (a blind write/overwrite).</li>
     *     <li>If {@code cas} is non-zero, the operation only succeeds if the current CAS value
     *         stored in the host for the given {@code key} matches the provided {@code cas}.
     *         If they don't match, it means the data was modified by another actor since it was
     *         last read, and the operation fails with {@link WasmResult#CAS_MISMATCH}.</li>
     * </ul>
     *
     * @param key   The key identifying the shared data item.
     * @param value The new data value to store (can be null or empty, depending on implementation). A null value might signify deletion.
     * @param cas   The Compare-And-Swap value expected for a conditional update, or 0 for an unconditional update.
     * @return A {@link WasmResult} indicating the outcome (e.g., {@link WasmResult#OK},
     *         {@link WasmResult#CAS_MISMATCH}, {@link WasmResult#UNIMPLEMENTED}).
     */
    default WasmResult setSharedData(String key, byte[] value, int cas) {
        return WasmResult.UNIMPLEMENTED;
    }
}
