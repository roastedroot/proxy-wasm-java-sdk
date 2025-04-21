package io.roastedroot.proxywasm.jaxrs.internal;

import io.roastedroot.proxywasm.internal.ProxyMap;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class MultivaluedMapAdaptor<T> implements ProxyMap {

    final MultivaluedMap<String, T> entries;

    public MultivaluedMapAdaptor() {
        this.entries = new MultivaluedHashMap<>();
    }

    public MultivaluedMapAdaptor(int mapSize) {
        this.entries = new MultivaluedHashMap<>();
    }

    public MultivaluedMapAdaptor(ProxyMap other) {
        this(other.size());
        for (Map.Entry<String, String> entry : other.entries()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public MultivaluedMapAdaptor(MultivaluedMap<String, T> other) {
        this.entries = other;
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public void add(String key, String value) {
        entries.add(key, (T) value);
    }

    @Override
    public void put(String key, String value) {
        entries.put(key, List.of((T) value));
    }

    static <T> Iterable<T> toIterable(Stream<T> stream) {
        return stream::iterator;
    }

    @Override
    public Iterable<? extends Map.Entry<String, String>> entries() {
        return toIterable(
                entries.entrySet().stream()
                        .flatMap(
                                entry ->
                                        entry.getValue().stream()
                                                .map(
                                                        value ->
                                                                Map.entry(
                                                                        entry.getKey(),
                                                                        asString(value)))));
    }

    @Override
    public String get(String key) {
        return asString(entries.getFirst(key));
    }

    private static String asString(Object x) {
        if (x == null) {
            return null;
        }
        if (x.getClass() == String.class) {
            return (String) x;
        }
        return x.toString();
    }

    @Override
    public void remove(String key) {
        entries.remove(key);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MultivaluedMapAdaptor that = (MultivaluedMapAdaptor) o;
        return Objects.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(entries);
    }

    @Override
    public String toString() {
        return entries.toString();
    }
}
