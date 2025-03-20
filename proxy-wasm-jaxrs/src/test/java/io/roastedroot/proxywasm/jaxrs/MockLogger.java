package io.roastedroot.proxywasm.jaxrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.roastedroot.proxywasm.LogLevel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public synchronized void assertLogsEqual(String... messages) {
        assertEquals(List.of(messages), loggedMessages());
    }

    public synchronized void assertSortedLogsEqual(String... messages) {
        assertEquals(
                Stream.of(messages).sorted().collect(Collectors.toList()),
                loggedMessages().stream().sorted().collect(Collectors.toList()));
    }

    public synchronized void assertLogsContain(String... message) {
        for (String m : message) {
            assertTrue(loggedMessages().contains(m), "logged messages does not contain: " + m);
        }
    }

    public synchronized void assertLogsDoNotContain(String... message) {
        for (String log : loggedMessages()) {
            for (String m : message) {
                assertFalse(log.contains(m), "logged messages contains: " + m);
            }
        }
    }
}
