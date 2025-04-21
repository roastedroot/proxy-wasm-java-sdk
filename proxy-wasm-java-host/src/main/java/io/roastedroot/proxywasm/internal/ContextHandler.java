package io.roastedroot.proxywasm.internal;

public interface ContextHandler {

    /**
     * Set the effective context ID.
     *
     * @param contextID The context ID
     * @return The result of the operation
     */
    default WasmResult setEffectiveContextID(int contextID) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Indicates to the host that the plugin is done processing active context.
     *
     * @return The result of the operation
     */
    default WasmResult done() {
        return WasmResult.NOT_FOUND;
    }
}
