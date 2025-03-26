package io.roastedroot.proxywasm.jaxrs.example;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class IsTrueMatcher<T> extends TypeSafeMatcher<T> {

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
