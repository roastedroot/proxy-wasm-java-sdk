package io.roastedroot.proxywasm;

/**
 * Action represents the action which Wasm contexts expects hosts to take.
 */
public enum Action {
    CONTINUE(0),
    PAUSE(1);

    private final int value;

    /**
     * Constructor for BufferType enum.
     *
     * @param value The integer value of the buffer type
     */
    Action(int value) {
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
    public static Action fromInt(int value) {
        for (Action type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown Action value: " + value);
    }
}
