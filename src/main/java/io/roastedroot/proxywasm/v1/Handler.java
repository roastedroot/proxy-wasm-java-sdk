package io.roastedroot.proxywasm.v1;

import java.util.Map;

public interface Handler {

    default void log(LogLevel level, String message) throws WasmException {
    }

    default Map<String, String> getHttpRequestHeader() {
        return null;
    }

    default Map<String, String> getHttpRequestTrailer() {
        return null;
    }

    default Map<String, String> getHttpResponseHeader() {
        return null;
    }

    default Map<String, String> getHttpResponseTrailer() {
        return null;
    }

    default Map<String, String> getHttpCallResponseHeaders() {
        return null;
    }

    default Map<String, String> getHttpCallResponseTrailer() {
        return null;
    }

    default Map<String, String> getGrpcReceiveInitialMetaData() {
        return null;
    }

    default Map<String, String> getGrpcReceiveTrailerMetaData() {
        return null;
    }

    default Map<String, String> getCustomHeader(int mapType) {
        return null;
    }

    default String getProperty(String key) throws WasmException {
        return null;
    }

}
