package io.roastedroot.proxywasm;

/**
 * Represents log levels used within the Proxy-WASM ABI specification.
 * These levels correspond to the values expected by the host environment
 * when logging messages from a WASM module.
 * <p>
 * Converted from Go's LogLevel type in the proxy-wasm-go-sdk.
 */
public enum LogLevel {

    /** Trace log level. Value: 0 */
    TRACE(0),
    /** Debug log level. Value: 1 */
    DEBUG(1),
    /** Info log level. Value: 2 */
    INFO(2),
    /** Warn log level. Value: 3 */
    WARN(3),
    /** Error log level. Value: 4 */
    ERROR(4),
    /** Critical log level. Value: 5 */
    CRITICAL(5);

    /** The integer representation of the log level, as defined by the Proxy-WASM ABI. */
    private final int value;

    /**
     * Constructor for LogLevel enum.
     *
     * @param value The integer value corresponding to the log level in the ABI.
     */
    LogLevel(int value) {
        this.value = value;
    }

    /**
     * Get the integer value of this log level as defined by the Proxy-WASM ABI.
     *
     * @return The integer value representing the log level.
     */
    public int value() {
        return value;
    }

    /**
     * Convert an integer value to its corresponding LogLevel enum constant.
     * This is useful for interpreting log level values received from the host
     * or specified in configurations.
     *
     * @param value The integer value to convert.
     * @return The corresponding LogLevel enum constant.
     * @throws IllegalArgumentException if the provided integer value does not
     *                                  match any known LogLevel.
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
