package io.roastedroot.proxywasm;

/**
 * Interface for handling logging sent form the guest of Proxy-WASM environment.
 * <p>
 * This interface provides methods to log messages at different levels and to retrieve the current
 * log level.
 */
public interface LogHandler {

    /**
     * A default, no-operation {@code LogHandler}.
     * It ignores all log messages and reports {@link LogLevel#CRITICAL} as the current log level,
     * effectively disabling logging.
     *
     * <p>Useful as a placeholder or when logging is explicitly disabled.
     */
    LogHandler DEFAULT =
            new LogHandler() {
                @Override
                public LogLevel getLogLevel() throws WasmException {
                    // since we are not logging anything, we can return the highest log level
                    return LogLevel.CRITICAL;
                }
            };

    /**
     * A simple {@code LogHandler} that logs all messages to {@link System#out}.
     * Messages are prefixed with their corresponding {@link LogLevel}.
     * The effective log level for this handler is {@link LogLevel#TRACE},
     * meaning all messages will be printed.
     */
    LogHandler SYSTEM =
            new LogHandler() {
                @Override
                public void log(LogLevel level, String message) throws WasmException {
                    System.out.println(level + ": " + message);
                }
            };

    /**
     * Logs a message at the specified log level.
     *
     * @param level the log level
     * @param message the message to log
     * @throws WasmException the result to provide the plugin
     */
    default void log(LogLevel level, String message) throws WasmException {}

    /**
     * Gets the current log level.
     *
     * @return the current log level
     * @throws WasmException the result to provide the plugin
     */
    default LogLevel getLogLevel() throws WasmException {
        return LogLevel.TRACE;
    }
}
