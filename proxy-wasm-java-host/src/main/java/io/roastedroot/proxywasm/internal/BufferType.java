package io.roastedroot.proxywasm.internal;

/**
 * Represents the type of buffer in proxy WASM.
 * Converted from Go's BufferType type.
 */
public enum BufferType {
    HTTP_REQUEST_BODY(0),
    HTTP_RESPONSE_BODY(1),
    DOWNSTREAM_DATA(2),
    UPSTREAM_DATA(3),
    HTTP_CALL_RESPONSE_BODY(4),
    GRPC_RECEIVE_BUFFER(5),
    VM_CONFIGURATION(6),
    PLUGIN_CONFIGURATION(7),
    CALL_DATA(8);

    private final int value;

    /**
     * Constructor for BufferType enum.
     *
     * @param value The integer value of the buffer type
     */
    BufferType(int value) {
        this.value = value;
    }

    /**
     * Get the integer value of this buffer type.
     *
     * @return The integer value
     */
    public int getValue() {
        return value;
    }

    /**
     * Convert an integer value to a BufferType.
     *
     * @param value The integer value to convert
     * @return The corresponding BufferType or null if the value doesn't match any BufferType
     */
    public static BufferType fromInt(int value) {
        for (BufferType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}
