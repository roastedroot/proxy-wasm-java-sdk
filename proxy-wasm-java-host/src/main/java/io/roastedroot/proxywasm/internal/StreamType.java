package io.roastedroot.proxywasm.internal;

/**
 * Represents the type of map in proxy WASM.
 * Converted from Go's MapType type.
 */
public enum StreamType {
    REQUEST(0),
    RESPONSE(1),
    DOWNSTREAM(2),
    UPSTREAM(3);

    private final int value;

    /**
     * Constructor for MapType enum.
     *
     * @param value The integer value of the map type
     */
    StreamType(int value) {
        this.value = value;
    }

    /**
     * Get the integer value of this map type.
     *
     * @return The integer value
     */
    public int getValue() {
        return value;
    }

    /**
     * Convert an integer value to a MapType.
     *
     * @param value The integer value to convert
     * @return The corresponding MapType or null if the value doesn't match any MapType
     */
    public static StreamType fromInt(int value) {
        for (StreamType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}
