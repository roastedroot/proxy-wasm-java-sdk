package io.roastedroot.proxywasm.jaxrs.example;

import io.roastedroot.proxywasm.LogLevel;
import io.roastedroot.proxywasm.jaxrs.Logger;
import java.util.ArrayList;

public class MockLogger implements Logger {

    static final boolean DEBUG = "true".equals(System.getenv("DEBUG"));

    final ArrayList<String> loggedMessages = new ArrayList<>();

    @Override
    public synchronized void log(LogLevel level, String message) {
        if (DEBUG) {
            System.out.println(level + ": " + message);
        }
        loggedMessages.add(message);
    }

    @Override
    public synchronized LogLevel getLogLevel() {
        return LogLevel.TRACE;
    }

    public synchronized ArrayList<String> loggedMessages() {
        return new ArrayList<>(loggedMessages);
    }
}
