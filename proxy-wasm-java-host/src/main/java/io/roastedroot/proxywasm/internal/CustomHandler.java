package io.roastedroot.proxywasm.internal;

public interface CustomHandler {

    /**
     * Get a custom buffer.
     *
     * @param bufferType The buffer type
     * @return The custom buffer as a byte[], or null if not available
     */
    default byte[] getCustomBuffer(int bufferType) {
        return null;
    }

    /**
     * Set a custom buffer.
     *
     * @param bufferType The buffer type
     * @param buffer     The custom buffer as a byte[]
     * @return WasmResult indicating success or failure
     */
    default WasmResult setCustomBuffer(int bufferType, byte[] buffer) {
        return WasmResult.UNIMPLEMENTED;
    }

    default ProxyMap getCustomHeaders(int mapType) {
        return null;
    }

    /**
     * Set a custom header map.
     *
     * @param mapType The type of map to set
     * @param map     The header map to set
     * @return WasmResult indicating success or failure
     */
    default WasmResult setCustomHeaders(int mapType, ProxyMap map) {
        return WasmResult.UNIMPLEMENTED;
    }
}
