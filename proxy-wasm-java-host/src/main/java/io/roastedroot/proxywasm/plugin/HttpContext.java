package io.roastedroot.proxywasm.plugin;

import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.ChainedHandler;
import io.roastedroot.proxywasm.Handler;
import io.roastedroot.proxywasm.Helpers;
import io.roastedroot.proxywasm.ProxyMap;
import io.roastedroot.proxywasm.StreamType;
import io.roastedroot.proxywasm.WasmException;
import io.roastedroot.proxywasm.WasmResult;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class HttpContext {

    private final Plugin plugin;
    private final io.roastedroot.proxywasm.HttpContext context;
    private final HttpRequestAdaptor requestAdaptor;
    private final long startedAt = System.currentTimeMillis();

    final HashMap<List<String>, byte[]> properties = new HashMap<>();
    private byte[] httpRequestBody = new byte[0];
    private byte[] grpcReceiveBuffer = new byte[0];
    private byte[] upstreamData = new byte[0];
    private byte[] downStreamData = new byte[0];
    private byte[] httpResponseBody = new byte[0];
    private SendResponse sendResponse;
    private Action action;
    private CountDownLatch resumeLatch;

    HttpContext(Plugin plugin, HttpRequestAdaptor requestAdaptor) {
        this.plugin = plugin;
        this.requestAdaptor = requestAdaptor;
        this.context = plugin.wasm.createHttpContext(new HandlerImpl());
    }

    public Plugin plugin() {
        return plugin;
    }

    public io.roastedroot.proxywasm.HttpContext context() {
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

    public byte[] getHttpRequestBody() {
        return httpRequestBody;
    }

    public byte[] getHttpResponseBody() {
        return httpResponseBody;
    }

    public void setHttpRequestBody(byte[] httpRequestBody) {
        this.httpRequestBody = httpRequestBody;
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

    public void setHttpResponseBody(byte[] httpResponseBody) {
        this.httpResponseBody = httpResponseBody;
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
            return httpRequestBody;
        }

        @Override
        public WasmResult setHttpRequestBody(byte[] body) {
            httpRequestBody = body;
            return WasmResult.OK;
        }

        public void appendHttpRequestBody(byte[] body) {
            httpRequestBody = Helpers.append(httpRequestBody, body);
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
            byte[] result = requestAdaptor.getProperty(HttpContext.this, path);
            if (result == null) {
                result = properties.get(path);
            }
            return result;
        }

        @Override
        public WasmResult setProperty(List<String> path, byte[] value) {
            var result = requestAdaptor.setProperty(HttpContext.this, path, value);
            if (result == WasmResult.NOT_FOUND) {
                properties.put(path, value);
            }
            return WasmResult.OK;
        }
    }
}
