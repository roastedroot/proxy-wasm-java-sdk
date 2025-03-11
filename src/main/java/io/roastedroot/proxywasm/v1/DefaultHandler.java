package io.roastedroot.proxywasm.v1;

import java.nio.ByteBuffer;
import java.util.Map;

public class DefaultHandler implements Handler {

    @Override
    public void log(LogLevel level, String message) throws WasmException {
    }

    @Override
    public Map<String, String> getHttpRequestHeader() {
        return null;
    }

    @Override
    public Map<String, String> getHttpRequestTrailer() {
        return null;
    }

    @Override
    public Map<String, String> getHttpResponseHeader() {
        return null;
    }

    @Override
    public Map<String, String> getHttpResponseTrailer() {
        return null;
    }

    @Override
    public Map<String, String> getHttpCallResponseHeaders() {
        return null;
    }

    @Override
    public Map<String, String> getHttpCallResponseTrailer() {
        return null;
    }

    @Override
    public Map<String, String> getGrpcReceiveInitialMetaData() {
        return null;
    }

    @Override
    public Map<String, String> getGrpcReceiveTrailerMetaData() {
        return null;
    }

    @Override
    public Map<String, String> getCustomHeader(int mapType) {
        return null;
    }

    @Override
    public String getProperty(String key) throws WasmException {
        return null;
    }

    @Override
    public ByteBuffer getHttpRequestBody() {
        return null;
    }

    @Override
    public ByteBuffer getHttpResponseBody() {
        return null;
    }

    @Override
    public ByteBuffer getDownStreamData() {
        return null;
    }

    @Override
    public ByteBuffer getUpstreamData() {
        return null;
    }

    @Override
    public ByteBuffer getHttpCallResponseBody() {
        return null;
    }

    @Override
    public ByteBuffer getGrpcReceiveBuffer() {
        return null;
    }

    @Override
    public ByteBuffer getPluginConfig() {
        return null;
    }

    @Override
    public ByteBuffer getVmConfig() {
        return null;
    }

    @Override
    public ByteBuffer getFuncCallData() {
        return null;
    }

    @Override
    public ByteBuffer getCustomBuffer(int bufferType) {
        return null;
    }

    @Override
    public WasmResult setEffectiveContextID(int contextID) {
        return WasmResult.UNIMPLEMENTED;
    }

    @Override
    public WasmResult done() {
        return WasmResult.NOT_FOUND;
    }

    @Override
    public WasmResult setTickPeriodMilliseconds(int tick_period) {
        return WasmResult.UNIMPLEMENTED;
    }

    // Warning This function has been deprecated in favor of wasi_snapshot_preview1.clock_time_get.
    @Override
    public int getCurrentTimeNanoseconds() throws WasmException {
        return (int) System.currentTimeMillis() * 1000000;
    }
}
