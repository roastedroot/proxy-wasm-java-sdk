package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.LogLevel;

public interface Logger {

    void log(LogLevel level, String message);

    LogLevel getLogLevel();
}
