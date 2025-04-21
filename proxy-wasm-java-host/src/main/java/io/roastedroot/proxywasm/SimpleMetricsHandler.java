package io.roastedroot.proxywasm;

import io.roastedroot.proxywasm.internal.WasmResult;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A basic, in-memory implementation of the {@link MetricsHandler} interface.
 *
 * <p>This handler manages metrics entirely within the host's memory using standard Java collections.
 * It supports Counter, Gauge, and Histogram types in a simplified manner (e.g., Histograms are
 * treated like Gauges for storage). It is suitable for single-process environments or testing
 * scenarios where integration with a real metrics backend (like Prometheus, StatsD) is not required.
 *
 * <p>All operations on this handler are synchronized to ensure thread safety within a single JVM.
 */
public class SimpleMetricsHandler implements MetricsHandler {

    /**
     * Represents an individual metric managed by {@link SimpleMetricsHandler}.
     * Stores the metric's ID, type, name, and its current value.
     * Note: Histograms are stored as a single value (like a Gauge) in this simple implementation.
     */
    public static class Metric {

        private final int id;
        private final MetricType type;
        private final String name;
        private long value; // Represents counter/gauge value, or potentially sum for histograms

        /**
         * Constructs a new Metric instance.
         *
         * @param id   The unique integer ID assigned by the handler.
         * @param type The {@link MetricType} (Counter, Gauge, Histogram).
         * @param name The name of the metric.
         */
        public Metric(int id, MetricType type, String name) {
            this.id = id;
            this.type = type;
            this.name = name;
        }

        /**
         * @return The unique integer ID of the metric.
         */
        public int id() {
            return id;
        }

        /**
         * @return The {@link MetricType} of the metric.
         */
        public MetricType type() {
            return type;
        }

        /**
         * @return The name of the metric.
         */
        public String name() {
            return name;
        }

        /**
         * @return The current value of the metric.
         */
        public long getValue() {
            return value;
        }

        /**
         * Sets the current value of the metric.
         * Used internally by {@link SimpleMetricsHandler#recordMetric(int, long)} and
         * {@link SimpleMetricsHandler#incrementMetric(int, long)}.
         *
         * @param value The new value for the metric.
         */
        public void setValue(long value) {
            this.value = value;
        }
    }

    private final AtomicInteger lastMetricId = new AtomicInteger(0);
    private final HashMap<Integer, Metric> metrics = new HashMap<>();
    private final HashMap<String, Metric> metricsByName = new HashMap<>();

    /**
     * Defines a new metric with the specified type and name.
     * Assigns a new unique ID to the metric and stores it in memory.
     * If a metric with the same name already exists, it is overwritten (behavior may vary in other handlers).
     *
     * @param type The {@link MetricType} of the new metric.
     * @param name The name for the new metric.
     * @return The unique integer ID assigned to the newly defined metric.
     * @throws WasmException (Not currently thrown by this implementation, but part of the interface contract).
     */
    @Override
    public synchronized int defineMetric(MetricType type, String name) throws WasmException {
        var id = lastMetricId.incrementAndGet();
        Metric value = new Metric(id, type, name);
        metrics.put(id, value);
        metricsByName.put(name, value);
        return id;
    }

    /**
     * Retrieves the current value of the specified metric.
     *
     * @param metricId The unique ID of the metric to query.
     * @return The current value of the metric.
     * @throws WasmException with {@link WasmResult#NOT_FOUND} if no metric exists with the given ID.
     */
    @Override
    public synchronized long getMetric(int metricId) throws WasmException {
        var metric = metrics.get(metricId);
        if (metric == null) {
            throw new WasmException(WasmResult.NOT_FOUND);
        }
        return metric.getValue();
    }

    /**
     * Increments the value of the specified metric by the given amount.
     * Applicable primarily to Counters, but this implementation applies it additively to any metric type.
     *
     * @param metricId The unique ID of the metric to increment.
     * @param value    The amount to add to the metric's current value.
     * @return {@link WasmResult#OK} if successful, or {@link WasmResult#NOT_FOUND} if the metric ID is invalid.
     */
    @Override
    public synchronized WasmResult incrementMetric(int metricId, long value) {
        var metric = metrics.get(metricId);
        if (metric == null) {
            return WasmResult.NOT_FOUND;
        }
        metric.setValue(metric.getValue() + value);
        return WasmResult.OK;
    }

    /**
     * Sets the value of the specified metric to the given value.
     * Applicable primarily to Gauges, but this implementation applies it directly to any metric type.
     *
     * @param metricId The unique ID of the metric to record.
     * @param value    The value to set for the metric.
     * @return {@link WasmResult#OK} if successful, or {@link WasmResult#NOT_FOUND} if the metric ID is invalid.
     */
    @Override
    public synchronized WasmResult recordMetric(int metricId, long value) {
        var metric = metrics.get(metricId);
        if (metric == null) {
            return WasmResult.NOT_FOUND;
        }
        metric.setValue(value);
        return WasmResult.OK;
    }

    /**
     * Removes the metric definition and its associated value.
     *
     * @param metricId The unique ID of the metric to remove.
     * @return {@link WasmResult#OK} if the metric was successfully removed, or {@link WasmResult#NOT_FOUND} if no metric exists with the given ID.
     */
    @Override
    public synchronized WasmResult removeMetric(int metricId) {
        Metric metric = metrics.remove(metricId);
        if (metric == null) {
            return WasmResult.NOT_FOUND;
        }
        metricsByName.remove(metric.name());
        return WasmResult.OK;
    }
}
