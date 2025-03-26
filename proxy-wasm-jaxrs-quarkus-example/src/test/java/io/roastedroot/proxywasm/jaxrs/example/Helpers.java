package io.roastedroot.proxywasm.jaxrs.example;

import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;

public class Helpers {
    private Helpers() {}

    public static void assertLogsContain(ArrayList<String> loggedMessages, String... message) {
        for (String m : message) {
            Assertions.assertTrue(
                    loggedMessages.contains(m), "logged messages does not contain: " + m);
        }
    }

    public static <T> IsTrueMatcher<T> isTrue(IsTrueMatcher.Predicate<T> predicate) {
        return new IsTrueMatcher<T>(predicate);
    }
}
