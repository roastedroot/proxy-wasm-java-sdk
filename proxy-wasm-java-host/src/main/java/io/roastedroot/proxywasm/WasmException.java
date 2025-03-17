package io.roastedroot.proxywasm;

/**
 * Represents WebAssembly result codes.
 * Converted from Go's WasmResult type.
 */
public class WasmException extends Exception {

    private final WasmResult result;

    public WasmException(WasmResult result) {
        super(result.description());
        this.result = result;
    }

    public WasmResult result() {
        return result;
    }
}
