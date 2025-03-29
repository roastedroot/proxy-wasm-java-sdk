package io.quarkiverse.proxywasm.it;

import io.roastedroot.proxywasm.LogHandler;
import io.roastedroot.proxywasm.LogLevel;
import java.util.ArrayList;

public class MockLogger implements LogHandler {

    static final boolean DEBUG = "true".equals(System.getenv("DEBUG"));

    final ArrayList<String> loggedMessages = new ArrayList<>();
    private final String name;

    public MockLogger(String name) {
        this.name = name;
    }

    @Override
    public synchronized void log(LogLevel level, String message) {
        if (DEBUG) {
            System.out.printf("%s: [%s] %s\n", level, name, message);
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
