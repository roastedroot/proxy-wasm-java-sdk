package io.roastedroot.proxywasm;

import io.roastedroot.proxywasm.internal.WasmResult;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple implementation of the MetricsHandler interface that keeps track of metrics in memory.
 */
public class SimpleMetricsHandler implements MetricsHandler {

    public static class Metric {

        private final int id;
        private final MetricType type;
        private final String name;
        private long value;

        public Metric(int id, MetricType type, String name) {
            this.id = id;
            this.type = type;
            this.name = name;
        }

        public int id() {
            return id;
        }

        public MetricType type() {
            return type;
        }

        public String name() {
            return name;
        }

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }
    }

    private final AtomicInteger lastMetricId = new AtomicInteger(0);
    private HashMap<Integer, Metric> metrics = new HashMap<>();
    private HashMap<String, Metric> metricsByName = new HashMap<>();

    @Override
    public synchronized int defineMetric(MetricType type, String name) throws WasmException {
        var id = lastMetricId.incrementAndGet();
        Metric value = new Metric(id, type, name);
        metrics.put(id, value);
        metricsByName.put(name, value);
        return id;
    }

    @Override
    public synchronized long getMetric(int metricId) throws WasmException {
        var metric = metrics.get(metricId);
        if (metric == null) {
            throw new WasmException(WasmResult.NOT_FOUND);
        }
        return metric.getValue();
    }

    @Override
    public synchronized WasmResult incrementMetric(int metricId, long value) {
        var metric = metrics.get(metricId);
        if (metric == null) {
            return WasmResult.NOT_FOUND;
        }
        metric.setValue(metric.getValue() + value);
        return WasmResult.OK;
    }

    @Override
    public synchronized WasmResult recordMetric(int metricId, long value) {
        var metric = metrics.get(metricId);
        if (metric == null) {
            return WasmResult.NOT_FOUND;
        }
        metric.setValue(value);
        return WasmResult.OK;
    }

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
