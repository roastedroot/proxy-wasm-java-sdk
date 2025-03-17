package io.roastedroot.proxywasm;

/**
 * Represents the type of metric in proxy WASM.
 */
public enum MetricType {
    COUNTER(0),
    GAUGE(1),
    HISTOGRAM(2);

    private final int value;

    /**
     * Constructor for MetricType enum.
     *
     * @param value The integer value of the metric type
     */
    MetricType(int value) {
        this.value = value;
    }

    /**
     * Get the integer value of this metric type.
     *
     * @return The integer value
     */
    public int getValue() {
        return value;
    }

    /**
     * Convert an integer value to a MetricType.
     *
     * @param value The integer value to convert
     * @return The corresponding MetricType or null if the value doesn't match any MetricType
     */
    public static MetricType fromInt(int value) {
        for (MetricType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return null;
    }
}
