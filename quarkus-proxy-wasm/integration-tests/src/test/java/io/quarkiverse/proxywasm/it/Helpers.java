package io.quarkiverse.proxywasm.it;

import java.util.ArrayList;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Assertions;

public class Helpers {
    private Helpers() {}

    public static void assertLogsContain(ArrayList<String> loggedMessages, String... message) {
        for (String m : message) {
            Assertions.assertTrue(
                    loggedMessages.contains(m), "logged messages does not contain: " + m);
        }
    }

    public static <T> TypeSafeMatcher<T> isTrue(IsTrueMatcher.Predicate<T> predicate) {
        return new IsTrueMatcher<T>(predicate);
    }

    public static class IsTrueMatcher<T> extends TypeSafeMatcher<T> {

        public interface Predicate<T> {
            boolean matchesSafely(T value);
        }

        Predicate<T> predicate;

        public IsTrueMatcher(Predicate<T> predicate) {
            this.predicate = predicate;
        }

        @Override
        protected boolean matchesSafely(T item) {
            return predicate.matchesSafely(item);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("is not true");
        }
    }
}
