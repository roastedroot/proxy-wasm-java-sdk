package io.roastedroot.proxywasm;

public interface MetricsHandler {

    MetricsHandler DEFAULT = new MetricsHandler() {};

    default int defineMetric(MetricType metricType, String name) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }

    default WasmResult removeMetric(int metricId) {
        return WasmResult.UNIMPLEMENTED;
    }

    default WasmResult recordMetric(int metricId, long value) {
        return WasmResult.UNIMPLEMENTED;
    }

    default WasmResult incrementMetric(int metricId, long value) {
        return WasmResult.UNIMPLEMENTED;
    }

    default long getMetric(int metricId) throws WasmException {
        throw new WasmException(WasmResult.UNIMPLEMENTED);
    }
}
