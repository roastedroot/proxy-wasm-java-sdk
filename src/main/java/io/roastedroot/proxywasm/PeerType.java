package io.roastedroot.proxywasm;

/**
 * Represents the type of peer in proxy WASM.
 */
public enum PeerType {
    UNKNOWN(0),
    LOCAL(1),
    REMOTE(2);

    private final int value;

    /**
     * Constructor for PeerType enum.
     *
     * @param value The integer value of the peer type
     */
    PeerType(int value) {
        this.value = value;
    }

    /**
     * Get the integer value of this peer type.
     *
     * @return The integer value
     */
    public int getValue() {
        return value;
    }

    /**
     * Convert an integer value to a PeerType.
     *
     * @param value The integer value to convert
     * @return The corresponding PeerType or null if the value doesn't match any PeerType
     */
    public static PeerType fromInt(int value) {
        for (PeerType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}
