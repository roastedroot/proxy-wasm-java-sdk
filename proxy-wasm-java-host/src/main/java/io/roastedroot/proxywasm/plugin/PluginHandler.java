package io.roastedroot.proxywasm.plugin;

import static io.roastedroot.proxywasm.Helpers.bytes;
import static io.roastedroot.proxywasm.WellKnownHeaders.AUTHORITY;
import static io.roastedroot.proxywasm.WellKnownHeaders.METHOD;
import static io.roastedroot.proxywasm.WellKnownHeaders.PATH;
import static io.roastedroot.proxywasm.WellKnownHeaders.SCHEME;
import static io.roastedroot.proxywasm.WellKnownProperties.PLUGIN_NAME;
import static io.roastedroot.proxywasm.WellKnownProperties.PLUGIN_VM_ID;

import io.roastedroot.proxywasm.ArrayProxyMap;
import io.roastedroot.proxywasm.ChainedHandler;
import io.roastedroot.proxywasm.ForeignFunction;
import io.roastedroot.proxywasm.Handler;
import io.roastedroot.proxywasm.LogLevel;
import io.roastedroot.proxywasm.MetricType;
import io.roastedroot.proxywasm.ProxyMap;
import io.roastedroot.proxywasm.WasmException;
import io.roastedroot.proxywasm.WasmResult;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

class PluginHandler extends ChainedHandler {

    // //////////////////////////////////////////////////////////////////////
    // Filter Chain Methods
    // //////////////////////////////////////////////////////////////////////
    private Handler next;
    Plugin plugin;

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
        for (var cancelHttpCall : httpCalls.values()) {
            cancelHttpCall.run();
        }
        httpCalls.clear();
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

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    // //////////////////////////////////////////////////////////////////////
    // HTTP calls
    // //////////////////////////////////////////////////////////////////////

    private final AtomicInteger lastCallId = new AtomicInteger(0);
    private final HashMap<Integer, Runnable> httpCalls = new HashMap<>();

    HashMap<String, String> upstreams = new HashMap<>();
    boolean strictUpstreams;

    @Override
    public int httpCall(
            String upstreamName,
            ProxyMap headers,
            byte[] body,
            ProxyMap trailers,
            int timeoutMilliseconds)
            throws WasmException {

        var method = headers.get(METHOD);
        if (method == null) {
            throw new WasmException(WasmResult.BAD_ARGUMENT);
        }

        var scheme = headers.get(SCHEME);
        if (scheme == null) {
            scheme = "http";
        }
        var authority = headers.get(AUTHORITY);
        if (authority == null) {
            throw new WasmException(WasmResult.BAD_ARGUMENT);
        }
        headers.put("Host", authority);

        var connectHostPort = upstreams.get(upstreamName);
        if (connectHostPort == null && strictUpstreams) {
            throw new WasmException(WasmResult.BAD_ARGUMENT);
        }
        if (connectHostPort == null) {
            connectHostPort = authority;
        }

        URI connectUri = null;
        try {
            connectUri = URI.create(scheme + "://" + connectHostPort);
        } catch (IllegalArgumentException e) {
            throw new WasmException(WasmResult.BAD_ARGUMENT);
        }

        var connectHost = connectUri.getHost();
        var connectPort = connectUri.getPort();
        if (connectPort == -1) {
            connectPort = "https".equals(scheme) ? 443 : 80;
        }

        var path = headers.get(PATH);
        if (path == null) {
            throw new WasmException(WasmResult.BAD_ARGUMENT);
        }
        if (!path.isEmpty() && !path.startsWith("/")) {
            path = "/" + path;
        }

        URI uri = null;
        try {
            uri =
                    URI.create(
                            new URI(scheme, null, authority, connectPort, null, null, null) + path);
        } catch (IllegalArgumentException | URISyntaxException e) {
            throw new WasmException(WasmResult.BAD_ARGUMENT);
        }

        // Remove all the pseudo headers
        for (var r : new ArrayProxyMap(headers).entries()) {
            if (r.getKey().startsWith(":")) {
                headers.remove(r.getKey());
            }
        }

        try {
            var id = lastCallId.incrementAndGet();
            var future =
                    this.plugin.httpServer.scheduleHttpCall(
                            method,
                            connectHost,
                            connectPort,
                            uri,
                            headers,
                            body,
                            trailers,
                            timeoutMilliseconds,
                            (resp) -> {
                                this.plugin.lock();
                                try {
                                    if (httpCalls.remove(id) == null) {
                                        return; // the call could have already been cancelled
                                    }
                                    this.plugin.wasm.sendHttpCallResponse(
                                            id, resp.headers, new ArrayProxyMap(), resp.body);
                                } finally {
                                    this.plugin.unlock();
                                }
                            });
            httpCalls.put(id, future);
            return id;
        } catch (InterruptedException e) {
            throw new WasmException(WasmResult.INTERNAL_FAILURE);
        }
    }

    @Override
    public int dispatchHttpCall(
            String upstreamName,
            ProxyMap headers,
            byte[] body,
            ProxyMap trailers,
            int timeoutMilliseconds)
            throws WasmException {
        return httpCall(upstreamName, headers, body, trailers, timeoutMilliseconds);
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
    private HashMap<Integer, Metric> metrics = new HashMap<>();
    private HashMap<String, Metric> metricsByName = new HashMap<>();

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
