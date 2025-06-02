package io.roastedroot.proxywasm.internal;

import io.roastedroot.proxywasm.WasmException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class PluginHttpContext {

    private final Plugin plugin;
    private final io.roastedroot.proxywasm.internal.HttpContext context;
    private final HttpRequestAdaptor requestAdaptor;
    private final long startedAt = System.currentTimeMillis();

    final HashMap<List<String>, byte[]> properties = new HashMap<>();
    private HttpRequestBody httpRequestBodyState;

    // Other body buffers and state fields (not lazy)
    private byte[] grpcReceiveBuffer = new byte[0];
    private byte[] upstreamData = new byte[0];
    private byte[] downStreamData = new byte[0];
    private byte[] httpResponseBody = new byte[0];
    private SendResponse sendResponse;
    private Action action;
    private CountDownLatch resumeLatch;

    public PluginHttpContext(Plugin plugin, HttpRequestAdaptor requestAdaptor) {
        this.plugin = plugin;
        this.requestAdaptor = requestAdaptor;
        this.context = plugin.wasm.createHttpContext(new HandlerImpl());
    }

    /**
     * Sets the lazy request body supplier.
     */
    public void setHttpRequestBodyState(HttpRequestBody supplier) {
        this.httpRequestBodyState = supplier;
    }

    /**
     * Gets the lazy request body supplier.
     */
    public HttpRequestBody getHttpRequestBodyState() {
        return httpRequestBodyState;
    }

    /**
     * Gets the HTTP request body, triggering lazy loading if needed.
     */
    public byte[] getHttpRequestBody() {
        if (httpRequestBodyState != null) {
            return httpRequestBodyState.get();
        }
        return new byte[0];
    }

    /**
     * Sets the HTTP request body, updating the supplier if present.
     */
    public void setHttpRequestBody(byte[] body) {
        if (httpRequestBodyState != null && body != null) {
            httpRequestBodyState.setBody(body);
        }
    }

    public Plugin plugin() {
        return plugin;
    }

    public io.roastedroot.proxywasm.internal.HttpContext context() {
        return context;
    }

    public HttpRequestAdaptor requestAdaptor() {
        return requestAdaptor;
    }

    public Action getAction() {
        return action;
    }

    public void maybePause() {
        // don't pause if plugin wants us to continue
        if (action == Action.CONTINUE) {
            return;
        }
        // don't pause if the plugin wants to respond to the request
        if (sendResponse != null) {
            return;
        }

        // ok, lets pause the request processing. A tick or http call response event will
        // need to resume the processing
        resumeLatch = new CountDownLatch(1);
        plugin.unlock();
        try {
            resumeLatch.await();
        } catch (InterruptedException ignore) {
            return;
        } finally {
            plugin.lock();
            resumeLatch = null;
        }
    }

    public byte[] getHttpResponseBody() {
        return httpResponseBody;
    }

    public void setHttpResponseBody(byte[] httpResponseBody) {
        this.httpResponseBody = httpResponseBody;
    }

    public byte[] getGrpcReceiveBuffer() {
        return grpcReceiveBuffer;
    }

    public void setGrpcReceiveBuffer(byte[] grpcReceiveBuffer) {
        this.grpcReceiveBuffer = grpcReceiveBuffer;
    }

    public byte[] getUpstreamData() {
        return upstreamData;
    }

    public void setUpstreamData(byte[] upstreamData) {
        this.upstreamData = upstreamData;
    }

    public byte[] getDownStreamData() {
        return downStreamData;
    }

    public void setDownStreamData(byte[] downStreamData) {
        this.downStreamData = downStreamData;
    }

    public SendResponse getSendResponse() {
        return sendResponse;
    }

    public SendResponse consumeSentHttpResponse() {
        var result = sendResponse;
        sendResponse = null;
        return result;
    }

    class HandlerImpl extends ChainedHandler {
        private final Handler next = plugin.wasm.getPluginHandler();

        @Override
        protected Handler next() {
            return next;
        }

        public ProxyMap getHttpRequestHeaders() {
            return requestAdaptor.getHttpRequestHeaders();
        }

        public ProxyMap getHttpRequestTrailers() {
            return requestAdaptor.getHttpRequestTrailers();
        }

        public ProxyMap getHttpResponseHeaders() {
            return requestAdaptor.getHttpResponseHeaders();
        }

        public ProxyMap getHttpResponseTrailers() {
            return requestAdaptor.getHttpResponseTrailers();
        }

        public ProxyMap getGrpcReceiveInitialMetaData() {
            return requestAdaptor.getGrpcReceiveInitialMetaData();
        }

        public ProxyMap getGrpcReceiveTrailerMetaData() {
            return requestAdaptor.getGrpcReceiveTrailerMetaData();
        }

        // //////////////////////////////////////////////////////////////////////
        // Buffers
        // //////////////////////////////////////////////////////////////////////

        @Override
        public byte[] getHttpRequestBody() {
            return PluginHttpContext.this
                    .getHttpRequestBody(); // Delegate to outer class for lazy loading
        }

        @Override
        public WasmResult setHttpRequestBody(byte[] body) {
            PluginHttpContext.this.setHttpRequestBody(body); // Delegate to outer class
            return WasmResult.OK;
        }

        public void appendHttpRequestBody(byte[] body) {
            byte[] currentBody =
                    PluginHttpContext.this
                            .getHttpRequestBody(); // This will trigger lazy loading if needed
            PluginHttpContext.this.setHttpRequestBody(Helpers.append(currentBody, body));
        }

        @Override
        public byte[] getGrpcReceiveBuffer() {
            return grpcReceiveBuffer;
        }

        @Override
        public WasmResult setGrpcReceiveBuffer(byte[] buffer) {
            grpcReceiveBuffer = buffer;
            return WasmResult.OK;
        }

        @Override
        public byte[] getUpstreamData() {
            return upstreamData;
        }

        @Override
        public WasmResult setUpstreamData(byte[] data) {
            upstreamData = data;
            return WasmResult.OK;
        }

        @Override
        public byte[] getDownStreamData() {
            return downStreamData;
        }

        @Override
        public WasmResult setDownStreamData(byte[] data) {
            downStreamData = data;
            return WasmResult.OK;
        }

        @Override
        public byte[] getHttpResponseBody() {
            return httpResponseBody;
        }

        @Override
        public WasmResult setHttpResponseBody(byte[] body) {
            httpResponseBody = body;
            return WasmResult.OK;
        }

        public void appendHttpResponseBody(byte[] body) {
            httpResponseBody = Helpers.append(httpResponseBody, body);
        }

        // //////////////////////////////////////////////////////////////////////
        // HTTP streams
        // //////////////////////////////////////////////////////////////////////

        @Override
        public WasmResult sendHttpResponse(
                int responseCode,
                byte[] responseCodeDetails,
                byte[] responseBody,
                ProxyMap additionalHeaders,
                int grpcStatus) {
            sendResponse =
                    new SendResponse(
                            responseCode,
                            responseCodeDetails,
                            responseBody,
                            additionalHeaders,
                            grpcStatus);

            if (resumeLatch != null) {
                resumeLatch.countDown();
            }
            return WasmResult.OK;
        }

        @Override
        public WasmResult setAction(StreamType streamType, Action actionValue) {
            action = actionValue;
            if (action == Action.CONTINUE && resumeLatch != null) {
                resumeLatch.countDown();
            }
            return WasmResult.OK;
        }

        // //////////////////////////////////////////////////////////////////////
        // Properties
        // //////////////////////////////////////////////////////////////////////
        @Override
        public byte[] getProperty(List<String> path) throws WasmException {
            byte[] result = requestAdaptor.getProperty(PluginHttpContext.this, path);
            if (result == null) {
                result = properties.get(path);
            }
            return result;
        }

        @Override
        public WasmResult setProperty(List<String> path, byte[] value) {
            var result = requestAdaptor.setProperty(PluginHttpContext.this, path, value);
            if (result == WasmResult.NOT_FOUND) {
                properties.put(path, value);
            }
            return WasmResult.OK;
        }
    }
}
