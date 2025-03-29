package io.roastedroot.proxywasm;

public interface LogHandler {

    LogHandler DEFAULT = new LogHandler() {};

    default void log(LogLevel level, String message) throws WasmException {}

    default LogLevel getLogLevel() throws WasmException {
        return LogLevel.TRACE;
    }
}
