package io.roastedroot.proxywasm;

public interface LogHandler {

    LogHandler DEFAULT = new LogHandler() {};

    LogHandler SYSTEM =
            new LogHandler() {
                @Override
                public void log(LogLevel level, String message) throws WasmException {
                    System.out.println(level + ": " + message);
                }
            };

    default void log(LogLevel level, String message) throws WasmException {}

    default LogLevel getLogLevel() throws WasmException {
        return LogLevel.TRACE;
    }
}
