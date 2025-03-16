package io.roastedroot.proxywasm;

/**
 * Represents log levels for proxy WASM.
 * Converted from Go's LogLevel type.
 */
public enum LogLevel {
    // The values follow the same order as in the Go code using iota (0-based incrementing)
    TRACE(0),
    DEBUG(1),
    INFO(2),
    WARN(3),
    ERROR(4),
    CRITICAL(5);

    private final int value;

    /**
     * Constructor for LogLevel enum.
     *
     * @param value The integer value of the log level
     */
    LogLevel(int value) {
        this.value = value;
    }

    /**
     * Get the integer value of this log level.
     *
     * @return The integer value
     */
    public int value() {
        return value;
    }

    /**
     * Convert an integer value to a LogLevel.
     *
     * @param value The integer value to convert
     * @return The corresponding LogLevel, or null if not found
     */
    public static LogLevel fromInt(int value) {
        for (LogLevel level : values()) {
            if (level.value == value) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown LogLevel value: " + value);
    }
}
