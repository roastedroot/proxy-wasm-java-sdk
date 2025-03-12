package io.roastedroot.proxywasm.v1;

/**
 * Represents the type of map in proxy WASM.
 * Converted from Go's MapType type.
 */
public enum MapType {
    HTTP_REQUEST_HEADERS(0),
    HTTP_REQUEST_TRAILERS(1),
    HTTP_RESPONSE_HEADERS(2),
    HTTP_RESPONSE_TRAILERS(3),
    GRPC_RECEIVE_INITIAL_METADATA(4),
    GRPC_RECEIVE_TRAILING_METADATA(5),
    HTTP_CALL_RESPONSE_HEADERS(6),
    HTTP_CALL_RESPONSE_TRAILERS(7);

    private final int value;

    /**
     * Constructor for MapType enum.
     *
     * @param value The integer value of the map type
     */
    MapType(int value) {
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
    public static MapType fromInt(int value) {
        for (MapType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}
