package io.roastedroot.proxywasm;

/**
 * Represents WebAssembly result codes.
 * Converted from Go's WasmResult type.
 */
public enum WasmResult {
    OK(0, "Operation completed successfully"),
    NOT_FOUND(1, "The result could not be found, e.g. a provided key did not appear in a table"),
    BAD_ARGUMENT(2, "An argument was bad, e.g. did not conform to the required range"),
    SERIALIZATION_FAILURE(3, "A protobuf could not be serialized"),
    PARSE_FAILURE(4, "A protobuf could not be parsed"),
    INVALID_MEMORY_ACCESS(6, "A provided memory range was not legal"),
    EMPTY(7, "Data was requested from an empty container"),
    CAS_MISMATCH(8, "The provided CAS did not match that of the stored data"),
    INTERNAL_FAILURE(10, "Internal failure: trying check logs of the surrounding system"),
    UNIMPLEMENTED(12, "Feature not implemented");

    private final int value;
    private final String description;

    /**
     * Constructor for WasmResult enum.
     *
     * @param value The integer value of the result code
     * @param description Description of the result code
     */
    WasmResult(int value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Get the integer value of this result code.
     *
     * @return The integer value
     */
    public int getValue() {
        return value;
    }

    /**
     * Get the description of this result code.
     *
     * @return The description
     */
    public String description() {
        return description;
    }

    /**
     * Convert an integer value to a WasmResult.
     *
     * @param value The integer value to convert
     * @return The corresponding WasmResult
     * @throws IllegalArgumentException if the value doesn't match any WasmResult
     */
    public static WasmResult fromInt(int value) {
        for (WasmResult result : values()) {
            if (result.value == value) {
                return result;
            }
        }
        throw new IllegalArgumentException("Unknown WasmResult value: " + value);
    }

    public void expect(WasmResult... expected) throws WasmException {
        for (WasmResult result : expected) {
            if (this == result) {
                return;
            }
        }
        throw new WasmException(this);
    }
}
