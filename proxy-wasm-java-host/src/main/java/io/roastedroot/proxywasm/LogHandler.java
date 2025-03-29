package io.roastedroot.proxywasm;

public interface LogHandler {

    default void log(LogLevel level, String message) throws WasmException {}

    default LogLevel getLogLevel() throws WasmException {
        return LogLevel.TRACE;
    }
}
