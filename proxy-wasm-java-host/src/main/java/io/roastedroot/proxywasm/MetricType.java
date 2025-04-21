package io.roastedroot.proxywasm;

/**
 * Represents the types of metrics that can be defined and manipulated
 * via the Proxy-WASM ABI.
 */
public enum MetricType {
    /**
     * A metric that only increments.
     * Value: 0
     */
    COUNTER(0),
    /**
     * A metric that can be arbitrarily set.
     * Value: 1
     */
    GAUGE(1),
    /**
     * A metric that accumulates observations into predefined buckets
     * and a sum of observations.
     * Value: 2
     */
    HISTOGRAM(2);

    /** The integer representation of the metric type, as defined by the Proxy-WASM ABI. */
    private final int value;

    /**
     * Constructor for MetricType enum.
     *
     * @param value The integer value corresponding to the metric type in the ABI.
     */
    MetricType(int value) {
        this.value = value;
    }

    /**
     * Get the integer value of this metric type as defined by the Proxy-WASM ABI.
     *
     * @return The integer value representing the metric type.
     */
    public int getValue() {
        return value;
    }

    /**
     * Convert an integer value to its corresponding MetricType enum constant.
     *
     * @param value The integer value to convert.
     * @return The corresponding MetricType enum constant, or {@code null} if the
     *         provided integer value does not match any known MetricType.
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
