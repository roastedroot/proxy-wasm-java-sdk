package io.quarkiverse.proxywasm.it;

import io.roastedroot.proxywasm.LogHandler;
import io.roastedroot.proxywasm.LogLevel;
import java.util.ArrayList;

/**
 * A mock implementation of {@link LogHandler} for testing purposes.
 * Stores logged messages in memory and optionally prints them to the console if DEBUG is enabled.
 */
public class MockLogger implements LogHandler {

    static final boolean DEBUG = "true".equals(System.getenv("DEBUG"));

    final ArrayList<String> loggedMessages = new ArrayList<>();
    private final String name;

    /**
     * Constructs a new MockLogger with the given name.
     *
     * @param name The name to associate with logged messages.
     */
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

    /**
     * Retrieves a copy of all messages logged by this instance.
     *
     * @return A new {@link ArrayList} containing the logged messages.
     */
    public synchronized ArrayList<String> loggedMessages() {
        return new ArrayList<>(loggedMessages);
    }
}
