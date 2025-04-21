package io.roastedroot.proxywasm;

import io.roastedroot.proxywasm.internal.WasmResult;

/**
 * Defines the contract for handling metrics operations initiated by a Proxy-WASM module.
 * Implementations of this interface are responsible for interacting with the underlying
 * metrics system of the host environment (Prometheus etc.).
 *
 * <p>The host environment provides implementations of these methods to allow WASM modules
 * to define, manipulate, and retrieve metric values.
 */
public interface MetricsHandler {

    /**
     * A default, non-functional instance of {@code MetricsHandler}.
     * This instance throws {@link WasmException} with {@link WasmResult#UNIMPLEMENTED}
     * for methods that define or retrieve metrics, and returns {@link WasmResult#UNIMPLEMENTED}
     * for methods that modify or remove metrics.
     * Useful as a placeholder or base when only a subset of metrics functionality is needed.
     */
    MetricsHandler DEFAULT = new MetricsHandler() {};

    /**
     * Defines a new metric.
     *
     * @param metricType The type of the metric (e.g., Counter, Gauge, Histogram).
     * @param name       The name of the metric.
     * @return A unique identifier (metric ID) for the newly defined metric.
     * @throws WasmException If the metric cannot be defined (e.g., name conflict, unsupported type,
     *                       or if the operation is unimplemented by the host).
     */
    default int defineMetric(MetricType metricType, String name) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    /**
     * Removes or deletes a previously defined metric.
     *
     * @param metricId The unique identifier of the metric to remove.
     * @return A {@link WasmResult} indicating the outcome of the operation (e.g., OK, NOT_FOUND, UNIMPLEMENTED).
     */
    default WasmResult removeMetric(int metricId) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Records a value for a metric (typically used for Gauges or Histograms).
     *
     * @param metricId The unique identifier of the metric.
     * @param value    The value to record.
     * @return A {@link WasmResult} indicating the outcome of the operation (e.g., OK, BAD_ARGUMENT, UNIMPLEMENTED).
     */
    default WasmResult recordMetric(int metricId, long value) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Increments a metric's value (typically used for Counters).
     *
     * @param metricId The unique identifier of the metric.
     * @param value    The amount to increment by (can be negative to decrement, although convention is usually positive).
     * @return A {@link WasmResult} indicating the outcome of the operation (e.g., OK, BAD_ARGUMENT, UNIMPLEMENTED).
     */
    default WasmResult incrementMetric(int metricId, long value) {
        return WasmResult.UNIMPLEMENTED;
    }

    /**
     * Retrieves the current value of a metric.
     *
     * @param metricId The unique identifier of the metric.
     * @return The current value of the metric.
     * @throws WasmException If the metric cannot be retrieved (e.g., metric not found, type mismatch,
     *                       or if the operation is unimplemented by the host).
     */
    default long getMetric(int metricId) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }
}
