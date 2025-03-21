package io.roastedroot.proxywasm.jaxrs;

import static io.roastedroot.proxywasm.Helpers.bytes;
import static io.roastedroot.proxywasm.WellKnownProperties.PLUGIN_NAME;
import static io.roastedroot.proxywasm.WellKnownProperties.PLUGIN_VM_ID;

import io.roastedroot.proxywasm.ChainedHandler;
import io.roastedroot.proxywasm.ForeignFunction;
import io.roastedroot.proxywasm.Handler;
import io.roastedroot.proxywasm.LogLevel;
import io.roastedroot.proxywasm.MetricType;
import io.roastedroot.proxywasm.ProxyMap;
import io.roastedroot.proxywasm.WasmException;
import io.roastedroot.proxywasm.WasmResult;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

class PluginHandler extends ChainedHandler {

    // //////////////////////////////////////////////////////////////////////
    // Filter Chain Methods
    // //////////////////////////////////////////////////////////////////////
    private Handler next;
    WasmPlugin plugin;

    PluginHandler() {
        this(new Handler() {});
    }

    PluginHandler(Handler next) {
        this.next = next;
    }

    @Override
    protected Handler next() {
        return next;
    }

    // //////////////////////////////////////////////////////////////////////
    // Cleanup
    // //////////////////////////////////////////////////////////////////////
    public void close() {
        if (cancelTick != null) {
            cancelTick.run();
            cancelTick = null;
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Plugin config
    // //////////////////////////////////////////////////////////////////////

    byte[] vmConfig;

    @Override
    public byte[] getVmConfig() {
        return vmConfig;
    }

    byte[] pluginConfig;

    @Override
    public byte[] getPluginConfig() {
        return pluginConfig;
    }

    // //////////////////////////////////////////////////////////////////////
    // Properties
    // //////////////////////////////////////////////////////////////////////

    String name = "default";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    private final HashMap<List<String>, byte[]> properties = new HashMap<>();

    @Override
    public byte[] getProperty(List<String> path) throws WasmException {
        // TODO: do we need field for vm_id and root_id?
        if (PLUGIN_VM_ID.equals(path)) {
            return bytes(name);
        }
        if (PLUGIN_NAME.equals(path)) {
            return bytes(name);
        }
        return properties.get(path);
    }

    @Override
    public WasmResult setProperty(List<String> path, byte[] value) {
        properties.put(path, value);
        return WasmResult.OK;
    }

    // //////////////////////////////////////////////////////////////////////
    // Logging
    // //////////////////////////////////////////////////////////////////////

    public Logger logger;

    static final boolean DEBUG = "true".equals(System.getenv("DEBUG"));

    @Override
    public void log(LogLevel level, String message) throws WasmException {
        Logger l = logger;
        if (l == null) {
            super.log(level, message);
            return;
        }
        l.log(level, message);
    }

    @Override
    public LogLevel getLogLevel() throws WasmException {
        Logger l = logger;
        if (l == null) {
            return super.getLogLevel();
        }
        return l.getLogLevel();
    }

    // //////////////////////////////////////////////////////////////////////
    // Timers
    // //////////////////////////////////////////////////////////////////////

    int minTickPeriodMilliseconds;
    private int tickPeriodMilliseconds;
    private Runnable cancelTick;

    public int getTickPeriodMilliseconds() {
        return tickPeriodMilliseconds;
    }

    @Override
    public WasmResult setTickPeriodMilliseconds(int tickPeriodMilliseconds) {

        // check for no change
        if (tickPeriodMilliseconds == this.tickPeriodMilliseconds) {
            return WasmResult.OK;
        }

        // cancel the current tick, if any
        if (cancelTick != null) {
            cancelTick.run();
            cancelTick = null;
        }

        // set the new tick period, if any
        this.tickPeriodMilliseconds = tickPeriodMilliseconds;
        if (this.tickPeriodMilliseconds == 0) {
            return WasmResult.OK;
        }

        // schedule the new tick
        this.cancelTick =
                this.plugin.httpServer.scheduleTick(
                        Math.max(minTickPeriodMilliseconds, this.tickPeriodMilliseconds),
                        () -> {
                            this.plugin.lock();
                            try {
                                this.plugin.wasm.tick();
                            } finally {
                                this.plugin.unlock();
                            }
                        });
        return WasmResult.OK;
    }

    // //////////////////////////////////////////////////////////////////////
    // Foreign function interface (FFI)
    // //////////////////////////////////////////////////////////////////////

    private byte[] funcCallData = new byte[0];

    @Override
    public byte[] getFuncCallData() {
        return this.funcCallData;
    }

    @Override
    public WasmResult setFuncCallData(byte[] data) {
        this.funcCallData = data;
        return WasmResult.OK;
    }

    public void setPlugin(WasmPlugin plugin) {
        this.plugin = plugin;
    }

    // //////////////////////////////////////////////////////////////////////
    // HTTP calls
    // //////////////////////////////////////////////////////////////////////

    public static class HttpCall {
        public enum Type {
            REGULAR,
            DISPATCH
        }

        public final int id;
        public final Type callType;
        public final String uri;
        public final Object headers;
        public final byte[] body;
        public final ProxyMap trailers;
        public final int timeoutMilliseconds;

        public HttpCall(
                int id,
                Type callType,
                String uri,
                ProxyMap headers,
                byte[] body,
                ProxyMap trailers,
                int timeoutMilliseconds) {
            this.id = id;
            this.callType = callType;
            this.uri = uri;
            this.headers = headers;
            this.body = body;
            this.trailers = trailers;
            this.timeoutMilliseconds = timeoutMilliseconds;
        }
    }

    private final AtomicInteger lastCallId = new AtomicInteger(0);
    private final HashMap<Integer, HttpCall> httpCalls = new HashMap();

    public HashMap<Integer, HttpCall> getHttpCalls() {
        return httpCalls;
    }

    @Override
    public int httpCall(
            String uri, ProxyMap headers, byte[] body, ProxyMap trailers, int timeoutMilliseconds)
            throws WasmException {
        var id = lastCallId.incrementAndGet();
        HttpCall value =
                new HttpCall(
                        id,
                        HttpCall.Type.REGULAR,
                        uri,
                        headers,
                        body,
                        trailers,
                        timeoutMilliseconds);
        httpCalls.put(id, value);
        return id;
    }

    @Override
    public int dispatchHttpCall(
            String upstreamName,
            ProxyMap headers,
            byte[] body,
            ProxyMap trailers,
            int timeoutMilliseconds)
            throws WasmException {
        var id = lastCallId.incrementAndGet();
        HttpCall value =
                new HttpCall(
                        id,
                        HttpCall.Type.DISPATCH,
                        upstreamName,
                        headers,
                        body,
                        trailers,
                        timeoutMilliseconds);
        httpCalls.put(id, value);
        return id;
    }

    // //////////////////////////////////////////////////////////////////////
    // Metrics
    // //////////////////////////////////////////////////////////////////////

    public static class Metric {

        public final int id;
        public final MetricType type;
        public final String name;
        public long value;

        public Metric(int id, MetricType type, String name) {
            this.id = id;
            this.type = type;
            this.name = name;
        }
    }

    private final AtomicInteger lastMetricId = new AtomicInteger(0);
    private HashMap<Integer, Metric> metrics = new HashMap();
    private HashMap<String, Metric> metricsByName = new HashMap();

    @Override
    public int defineMetric(MetricType type, String name) throws WasmException {
        var id = lastMetricId.incrementAndGet();
        Metric value = new Metric(id, type, name);
        metrics.put(id, value);
        metricsByName.put(name, value);
        return id;
    }

    @Override
    public long getMetric(int metricId) throws WasmException {
        var metric = metrics.get(metricId);
        if (metric == null) {
            throw new WasmException(WasmResult.NOT_FOUND);
        }
        return metric.value;
    }

    public Metric getMetric(String name) {
        return metricsByName.get(name);
    }

    @Override
    public WasmResult incrementMetric(int metricId, long value) {
        var metric = metrics.get(metricId);
        if (metric == null) {
            return WasmResult.NOT_FOUND;
        }
        metric.value += value;
        return WasmResult.OK;
    }

    @Override
    public WasmResult recordMetric(int metricId, long value) {
        var metric = metrics.get(metricId);
        if (metric == null) {
            return WasmResult.NOT_FOUND;
        }
        metric.value = value;
        return WasmResult.OK;
    }

    @Override
    public WasmResult removeMetric(int metricId) {
        Metric metric = metrics.remove(metricId);
        if (metric == null) {
            return WasmResult.NOT_FOUND;
        }
        metricsByName.remove(metric.name);
        return WasmResult.OK;
    }

    // //////////////////////////////////////////////////////////////////////
    // FFI
    // //////////////////////////////////////////////////////////////////////
    HashMap<String, ForeignFunction> foreignFunctions;

    @Override
    public ForeignFunction getForeignFunction(String name) {
        if (foreignFunctions == null) {
            return null;
        }
        return foreignFunctions.get(name);
    }
}
