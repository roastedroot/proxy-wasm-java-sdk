package io.roastedroot.proxywasm.internal;

import io.roastedroot.proxywasm.ForeignFunction;

public interface FFIHandler {
    /**
     * Get the function call data.
     *
     * @return The function call data as a byte[], or null if not available
     */
    default byte[] getFuncCallData() {
        return null;
    }

    /**
     * Set the function call data.
     *
     * @param data The function call data as a byte[]
     * @return WasmResult indicating success or failure
     */
    default WasmResult setFuncCallData(byte[] data) {
        return WasmResult.UNIMPLEMENTED;
    }

    default ForeignFunction getForeignFunction(String name) {
        return null;
    }
}
