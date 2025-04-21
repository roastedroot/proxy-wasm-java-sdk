package io.roastedroot.proxywasm.plugin;

import static io.roastedroot.proxywasm.Helpers.bytes;
import static io.roastedroot.proxywasm.WellKnownHeaders.AUTHORITY;
import static io.roastedroot.proxywasm.WellKnownHeaders.METHOD;
import static io.roastedroot.proxywasm.WellKnownHeaders.PATH;
import static io.roastedroot.proxywasm.WellKnownHeaders.SCHEME;
import static io.roastedroot.proxywasm.WellKnownProperties.PLUGIN_NAME;
import static io.roastedroot.proxywasm.WellKnownProperties.PLUGIN_VM_ID;

import com.dylibso.chicory.runtime.ImportMemory;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.Machine;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.ArrayBytesProxyMap;
import io.roastedroot.proxywasm.ArrayProxyMap;
import io.roastedroot.proxywasm.ChainedHandler;
import io.roastedroot.proxywasm.ForeignFunction;
import io.roastedroot.proxywasm.Handler;
import io.roastedroot.proxywasm.LogHandler;
import io.roastedroot.proxywasm.LogLevel;
import io.roastedroot.proxywasm.MetricType;
import io.roastedroot.proxywasm.MetricsHandler;
import io.roastedroot.proxywasm.ProxyMap;
import io.roastedroot.proxywasm.ProxyWasm;
import io.roastedroot.proxywasm.QueueName;
import io.roastedroot.proxywasm.SharedData;
import io.roastedroot.proxywasm.SharedDataHandler;
import io.roastedroot.proxywasm.SharedQueueHandler;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.WasmException;
import io.roastedroot.proxywasm.WasmResult;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Plugin is an instance of a Proxy-Wasm plugin.
 */
public final class Plugin {

    private final ReentrantLock lock = new ReentrantLock();
    final ProxyWasm wasm;
    ServerAdaptor serverAdaptor;
    private final boolean shared;
    private final String name;

    private final MetricsHandler metricsHandler;
    private final SharedQueueHandler sharedQueueHandler;
    private final SharedDataHandler sharedDataHandler;

    private Plugin(Builder builder, ProxyWasm proxyWasm) throws StartException {
        Objects.requireNonNull(proxyWasm);
        this.name = Objects.requireNonNullElse(builder.name, "default");
        this.shared = builder.shared;
        this.foreignFunctions =
                Objects.requireNonNullElseGet(builder.foreignFunctions, HashMap::new);
        this.upstreams = Objects.requireNonNullElseGet(builder.upstreams, HashMap::new);
        this.strictUpstreams = builder.strictUpstreams;
        this.minTickPeriodMilliseconds = builder.minTickPeriodMilliseconds;
        this.vmConfig = builder.vmConfig;
        this.pluginConfig = builder.pluginConfig;
        this.logger = Objects.requireNonNullElse(builder.logger, LogHandler.DEFAULT);
        ;
        this.metricsHandler =
                Objects.requireNonNullElse(builder.metricsHandler, MetricsHandler.DEFAULT);
        this.sharedQueueHandler =
                Objects.requireNonNullElse(builder.sharedQueueHandler, SharedQueueHandler.DEFAULT);
        this.sharedDataHandler =
                Objects.requireNonNullElse(builder.sharedDataHandler, SharedDataHandler.DEFAULT);

        this.wasm = proxyWasm;
        this.wasm.setPluginHandler(new HandlerImpl());
        this.wasm.start();
    }

    public String name() {
        return name;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public boolean isShared() {
        return shared;
    }

    public ServerAdaptor getServerAdaptor() {
        return serverAdaptor;
    }

    public void setServerAdaptor(ServerAdaptor serverAdaptor) {
        this.serverAdaptor = serverAdaptor;
    }

    public LogHandler logger() {
        return logger;
    }

    public HttpContext createHttpContext(HttpRequestAdaptor requestAdaptor) {
        return new HttpContext(this, requestAdaptor);
    }

    public void close() {
        lock();
        try {
            wasm.close();
            if (cancelTick != null) {
                cancelTick.run();
                cancelTick = null;
            }
            for (var cancel : httpCalls.values()) {
                cancel.run();
            }
            httpCalls.clear();
            for (var cancel : grpcCalls.values()) {
                cancel.run();
            }
            grpcCalls.clear();

        } finally {
            unlock();
        }
    }

    /**
     * Creates a new Plugin builder.
     *
     * @return a new Plugin builder
     */
    public static Plugin.Builder builder(WasmModule module) {
        return new Plugin.Builder(module);
    }

    /**
     * Builder for creating a Plugin instance.
     */
    public static final class Builder {

        private final WasmModule module;
        private final ProxyWasm.Builder proxyWasmBuilder = ProxyWasm.builder().withStart(false);
        private boolean shared = true;
        private String name;
        private HashMap<String, ForeignFunction> foreignFunctions;
        private HashMap<String, URI> upstreams;
        private boolean strictUpstreams;
        private int minTickPeriodMilliseconds;
        private LogHandler logger;
        private byte[] vmConfig;
        private byte[] pluginConfig;
        private MetricsHandler metricsHandler;
        private SharedQueueHandler sharedQueueHandler;
        private SharedDataHandler sharedDataHandler;

        /**
         * Set the WASM module of the plugin.  The module contains the plugin instructions.
         *
         * @param module the WASM module of the plugin
         * @return this builder
         */
        private Builder(WasmModule module) {
            this.module = module;
        }

        /**
         * Set the name of the plugin.
         *
         * @param name the name of the plugin
         * @return this builder
         */
        public Plugin.Builder withName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set the foreign functions of that can be called from the plugin.
         *
         * @param functions the foreign functions of the plugin
         * @return this builder
         */
        public Builder withForeignFunctions(Map<String, ForeignFunction> functions) {
            this.foreignFunctions = new HashMap<>(functions);
            return this;
        }

        /**
         * Set the upstream server URL
         *
         * @param upstreams the upstream URI mappings.  When a http or grpc call is made
         *                  from the plugin, the upstream name is used to lookup the URL.
         * @return this builder
         */
        public Builder withUpstreams(Map<String, URI> upstreams) {
            this.upstreams = new HashMap<>(upstreams);
            return this;
        }

        /**
         * Set the strict upstreams mode of the plugin.  If strict upstreams is enabled,
         * then the plugin will throw an error if an upstream is not found.  If disabled,
         * then the upstream name is used as the URL.
         *
         * @param strictUpstreams the strict upstreams of the plugin
         * @return this builder
         */
        public Builder withStrictUpstreams(boolean strictUpstreams) {
            this.strictUpstreams = strictUpstreams;
            return this;
        }

        /**
         * Set the minimum tick period of the plugin.  A pluign that requests
         * a very small tick period will be ticked very frequently.  Use this
         * to protect the host from being overwhelmed by the plugin.
         *
         * @param minTickPeriodMilliseconds the minimum tick period of the plugin
         * @return this builder
         */
        public Builder withMinTickPeriodMilliseconds(int minTickPeriodMilliseconds) {
            this.minTickPeriodMilliseconds = minTickPeriodMilliseconds;
            return this;
        }

        /**
         * Set the logger of the plugin.
         *
         * @param logger the logger of the plugin
         * @return this builder
         */
        public Builder withLogger(LogHandler logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Set the metrics handler of the plugin.  If the metrics handler is not set,
         * then calls by the guest to define/use metrics will result in UNIMPLEMENTED errors
         * reported to the guest.
         *
         * @param metricsHandler the metrics handler of the plugin
         * @return this builder
         */
        public Builder withMetricsHandler(MetricsHandler metricsHandler) {
            this.metricsHandler = metricsHandler;
            return this;
        }

        /**
         * Set the shared queue handler of the plugin.  If the sahred queue handler is not set,
         * then calls by the guest to define/use shared queues will result in UNIMPLEMENTED errors
         * reported to the guest.
         *
         * @param sharedQueueHandler the shared queue handler of the plugin
         * @return this builder
         */
        public Builder withSharedQueueHandler(SharedQueueHandler sharedQueueHandler) {
            this.sharedQueueHandler = sharedQueueHandler;
            return this;
        }

        /**
         * Set the shared data handler of the plugin.  If the shared data handler is not set,
         * then calls by the guest to define/use shared data will result in UNIMPLEMENTED errors
         * reported to the guest.
         *
         * @param sharedDataHandler the shared data handler of the plugin
         * @return this builder
         */
        public Builder withSharedDataHandler(SharedDataHandler sharedDataHandler) {
            this.sharedDataHandler = sharedDataHandler;
            return this;
        }

        /**
         * Set whether the plugin is shared between host requests.  If the plugin is shared,
         * then the plugin will be created once and reused for each host request.  If the plugin
         * is not shared, then a new plugin MAY be use for each concurrent host request.
         *
         * @param shared whether the plugin is shared
         * @return this builder
         */
        public Builder withShared(boolean shared) {
            this.shared = shared;
            return this;
        }

        /**
         * Set the VM config of the plugin.
         *
         * @param vmConfig the VM config of the plugin
         * @return this builder
         */
        public Builder withVmConfig(byte[] vmConfig) {
            this.vmConfig = vmConfig;
            return this;
        }

        /**
         * Set the VM config of the plugin.
         *
         * @param vmConfig the VM config of the plugin
         * @return this builder
         */
        public Builder withVmConfig(String vmConfig) {
            this.vmConfig = bytes(vmConfig);
            return this;
        }

        /**
         * Set the plugin config of the plugin.
         *
         * @param pluginConfig the plugin config of the plugin
         * @return this builder
         */
        public Builder withPluginConfig(byte[] pluginConfig) {
            this.pluginConfig = pluginConfig;
            return this;
        }

        /**
         * Set the plugin config of the plugin.
         *
         * @param pluginConfig the plugin config of the plugin
         * @return this builder
         */
        public Builder withPluginConfig(String pluginConfig) {
            this.pluginConfig = bytes(pluginConfig);
            return this;
        }

        /**
         * Set the import memory of the plugin.
         *
         * @param memory the import memory of the plugin
         * @return this builder
         */
        public Builder withImportMemory(ImportMemory memory) {
            proxyWasmBuilder.withImportMemory(memory);
            return this;
        }

        /**
         * Set the machine factory of the plugin.  The machine factory is used to control
         * how instructions are executed.  By default instructions are executed in a
         * by an interpreter.  To increase performance, you can use compile the
         * was instructions to bytecode at runtime or at build time.  For more information
         * see https://chicory.dev/docs/experimental/aot
         *
         * @param machineFactory the machine factory of the plugin
         * @return this builder
         */
        public Builder withMachineFactory(Function<Instance, Machine> machineFactory) {
            proxyWasmBuilder.withMachineFactory(machineFactory);
            return this;
        }

        /**
         * Set the WASI options of the plugin.  A default WASI enviroment will be provided
         * to the pluign.  You can use this method to customize the WASI environment,
         * for example to provide it access to some file system resources.
         *
         * @param options the WASI options of the plugin
         * @return this builder
         */
        public Builder withWasiOptions(WasiOptions options) {
            proxyWasmBuilder.withWasiOptions(options);
            return this;
        }

        /**
         * Build the plugin.
         *
         * @return the plugin
         * @throws StartException if the plugin fails to start
         */
        public Plugin build() throws StartException {
            return new Plugin(this, proxyWasmBuilder.build(module));
        }
    }

    public LogHandler logger;
    static final boolean DEBUG = "true".equals(System.getenv("DEBUG"));
    byte[] vmConfig;
    byte[] pluginConfig;
    private final AtomicInteger lastCallId = new AtomicInteger(0);
    private final HashMap<Integer, Runnable> httpCalls = new HashMap<>();
    private final HashMap<Integer, Runnable> grpcCalls = new HashMap<>();
    private final HashMap<String, URI> upstreams;
    boolean strictUpstreams;
    int minTickPeriodMilliseconds;
    private int tickPeriodMilliseconds;
    private Runnable cancelTick;
    private final HashMap<String, ForeignFunction> foreignFunctions;
    private byte[] funcCallData = new byte[0];
    private final HashMap<List<String>, byte[]> properties = new HashMap<>();

    class HandlerImpl extends ChainedHandler {

        @Override
        protected Handler next() {
            return Handler.DEFAULT;
        }

        // //////////////////////////////////////////////////////////////////////
        // Plugin config
        // //////////////////////////////////////////////////////////////////////
        @Override
        public byte[] getVmConfig() {
            return vmConfig;
        }

        @Override
        public byte[] getPluginConfig() {
            return pluginConfig;
        }

        // //////////////////////////////////////////////////////////////////////
        // Properties
        // //////////////////////////////////////////////////////////////////////

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

        @Override
        public void log(LogLevel level, String message) throws WasmException {
            logger.log(level, message);
        }

        @Override
        public LogLevel getLogLevel() throws WasmException {
            return logger.getLogLevel();
        }

        // //////////////////////////////////////////////////////////////////////
        // Timers
        // //////////////////////////////////////////////////////////////////////
        @Override
        public WasmResult setTickPeriodMilliseconds(int tickMs) {

            // check for no change
            if (tickMs == tickPeriodMilliseconds) {
                return WasmResult.OK;
            }

            // cancel the current tick, if any
            if (cancelTick != null) {
                cancelTick.run();
                cancelTick = null;
            }

            // set the new tick period, if any
            tickPeriodMilliseconds = tickMs;
            if (tickPeriodMilliseconds == 0) {
                return WasmResult.OK;
            }

            // schedule the new tick
            cancelTick =
                    serverAdaptor.scheduleTick(
                            Math.max(minTickPeriodMilliseconds, tickPeriodMilliseconds),
                            () -> {
                                lock();
                                try {
                                    wasm.tick();
                                } finally {
                                    unlock();
                                }
                            });
            return WasmResult.OK;
        }

        // //////////////////////////////////////////////////////////////////////
        // Foreign function interface (FFI)
        // //////////////////////////////////////////////////////////////////////

        @Override
        public byte[] getFuncCallData() {
            return funcCallData;
        }

        @Override
        public WasmResult setFuncCallData(byte[] data) {
            funcCallData = data;
            return WasmResult.OK;
        }

        // //////////////////////////////////////////////////////////////////////
        // HTTP calls
        // //////////////////////////////////////////////////////////////////////

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

            var connectUri = upstreams.get(upstreamName);
            if (connectUri == null && strictUpstreams) {
                throw new WasmException(WasmResult.BAD_ARGUMENT);
            }
            if (connectUri == null) {
                try {
                    connectUri = new URI(upstreamName);
                } catch (URISyntaxException e) {
                    throw new WasmException(WasmResult.BAD_ARGUMENT);
                }
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
                uri = URI.create(scheme + "://" + authority + path);
            } catch (IllegalArgumentException e) {
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
                        serverAdaptor.scheduleHttpCall(
                                method,
                                connectHost,
                                connectPort,
                                uri,
                                headers,
                                body,
                                trailers,
                                timeoutMilliseconds,
                                (resp) -> {
                                    lock();
                                    try {
                                        if (httpCalls.remove(id) == null) {
                                            return; // the call could have already been cancelled
                                        }
                                        wasm.sendHttpCallResponse(
                                                id, resp.headers, new ArrayProxyMap(), resp.body);
                                    } finally {
                                        unlock();
                                    }
                                });
                httpCalls.put(id, future);
                return id;
            } catch (InterruptedException e) {
                throw new WasmException(WasmResult.INTERNAL_FAILURE);
            } catch (UnsupportedOperationException e) {
                throw new WasmException(WasmResult.UNIMPLEMENTED);
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
        // GRPC calls
        // //////////////////////////////////////////////////////////////////////

        @Override
        public int grpcCall(
                String upstreamName,
                String serviceName,
                String methodName,
                ProxyMap headers,
                byte[] body,
                int timeoutMilliseconds)
                throws WasmException {

            var connectUri = upstreams.get(upstreamName);
            if (connectUri == null && strictUpstreams) {
                throw new WasmException(WasmResult.BAD_ARGUMENT);
            }

            if (connectUri == null) {
                try {
                    connectUri = new URI(upstreamName);
                } catch (URISyntaxException e) {
                    throw new WasmException(WasmResult.BAD_ARGUMENT);
                }
            }

            if (!("http".equals(connectUri.getScheme())
                    || "https".equals(connectUri.getScheme()))) {
                logger.log(
                        LogLevel.ERROR,
                        "grpc call upstream not mapped to URL with a http/https scheme: "
                                + upstreamName);
                throw new WasmException(WasmResult.BAD_ARGUMENT);
            }

            var connectHost = connectUri.getHost();
            var connectPort = connectUri.getPort();
            if (connectPort == -1) {
                connectPort = "https".equals(connectUri.getScheme()) ? 443 : 80;
            }

            try {

                var id = lastCallId.incrementAndGet();
                var callHandler =
                        new GrpcCallResponseHandler() {

                            @Override
                            public void onHeaders(ArrayBytesProxyMap headers) {
                                lock();
                                try {
                                    if (grpcCalls.get(id) == null) {
                                        return; // the call could have already been cancelled
                                    }
                                    wasm.sendGrpcReceiveInitialMetadata(id, headers);
                                } finally {
                                    unlock();
                                }
                            }

                            @Override
                            public void onMessage(byte[] data) {
                                lock();
                                try {
                                    if (grpcCalls.get(id) == null) {
                                        return; // the call could have already been cancelled
                                    }
                                    wasm.sendGrpcReceive(id, data);
                                } finally {
                                    unlock();
                                }
                            }

                            @Override
                            public void onTrailers(ArrayBytesProxyMap trailers) {
                                lock();
                                try {
                                    if (grpcCalls.get(id) == null) {
                                        return; // the call could have already been cancelled
                                    }
                                    wasm.sendGrpcReceiveTrailingMetadata(id, trailers);
                                } finally {
                                    unlock();
                                }
                            }

                            @Override
                            public void onClose(int status) {
                                lock();
                                try {
                                    if (grpcCalls.get(id) == null) {
                                        return; // the call could have already been cancelled
                                    }
                                    wasm.sendGrpcClose(id, status);
                                } finally {
                                    unlock();
                                }
                            }
                        };

                var future =
                        serverAdaptor.scheduleGrpcCall(
                                connectHost,
                                connectPort,
                                "http".equals(connectUri.getScheme()),
                                serviceName,
                                methodName,
                                headers,
                                body,
                                timeoutMilliseconds,
                                callHandler);
                grpcCalls.put(id, future);
                return id;
            } catch (InterruptedException e) {
                throw new WasmException(WasmResult.INTERNAL_FAILURE);
            } catch (UnsupportedOperationException e) {
                throw new WasmException(WasmResult.UNIMPLEMENTED);
            }
        }

        // //////////////////////////////////////////////////////////////////////
        // Metrics
        // //////////////////////////////////////////////////////////////////////

        @Override
        public int defineMetric(MetricType type, String name) throws WasmException {
            return metricsHandler.defineMetric(type, name);
        }

        @Override
        public long getMetric(int metricId) throws WasmException {
            return metricsHandler.getMetric(metricId);
        }

        @Override
        public WasmResult incrementMetric(int metricId, long value) {
            return metricsHandler.incrementMetric(metricId, value);
        }

        @Override
        public WasmResult recordMetric(int metricId, long value) {
            return metricsHandler.recordMetric(metricId, value);
        }

        @Override
        public WasmResult removeMetric(int metricId) {
            return metricsHandler.removeMetric(metricId);
        }

        // //////////////////////////////////////////////////////////////////////
        // FFI
        // //////////////////////////////////////////////////////////////////////

        @Override
        public ForeignFunction getForeignFunction(String name) {
            return foreignFunctions.get(name);
        }

        // //////////////////////////////////////////////////////////////////////
        // Shared Data
        // //////////////////////////////////////////////////////////////////////

        @Override
        public SharedData getSharedData(String key) throws WasmException {
            return sharedDataHandler.getSharedData(key);
        }

        @Override
        public WasmResult setSharedData(String key, byte[] value, int cas) {
            return sharedDataHandler.setSharedData(key, value, cas);
        }

        // //////////////////////////////////////////////////////////////////////
        // Shared Queue
        // //////////////////////////////////////////////////////////////////////

        @Override
        public int registerSharedQueue(QueueName queueName) throws WasmException {
            return sharedQueueHandler.registerSharedQueue(queueName);
        }

        @Override
        public int resolveSharedQueue(QueueName queueName) throws WasmException {
            return sharedQueueHandler.resolveSharedQueue(queueName);
        }

        @Override
        public byte[] dequeueSharedQueue(int queueId) throws WasmException {
            return sharedQueueHandler.dequeueSharedQueue(queueId);
        }

        @Override
        public WasmResult enqueueSharedQueue(int queueId, byte[] value) {
            return sharedQueueHandler.enqueueSharedQueue(queueId, value);
        }
    }
}
