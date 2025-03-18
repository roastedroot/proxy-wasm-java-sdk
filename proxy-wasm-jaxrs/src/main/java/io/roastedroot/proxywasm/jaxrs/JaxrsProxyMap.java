package io.roastedroot.proxywasm.jaxrs;

import io.roastedroot.proxywasm.ProxyMap;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class JaxrsProxyMap implements ProxyMap {

    final MultivaluedMap<String, String> entries;

    public JaxrsProxyMap() {
        this.entries = new MultivaluedHashMap<>();
    }

    public JaxrsProxyMap(int mapSize) {
        this.entries = new MultivaluedHashMap<>();
    }

    public JaxrsProxyMap(ProxyMap other) {
        this(other.size());
        for (Map.Entry<String, String> entry : other.entries()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public JaxrsProxyMap(MultivaluedMap<String, String> other) {
        this.entries = other;
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public void add(String key, String value) {
        entries.add(key, value);
    }

    @Override
    public void put(String key, String value) {
        entries.put(key, List.of(value));
    }

    static <T> Iterable<T> toIterable(Stream<T> stream) {
        return stream::iterator;
    }

    @Override
    public Iterable<? extends Map.Entry<String, String>> entries() {
        return toIterable(
                entries.entrySet().stream()
                        .flatMap(x -> x.getValue().stream().map(y -> Map.entry(x.getKey(), y))));
    }

    @Override
    public String get(String key) {
        return entries.get(key).stream().findFirst().orElse(null);
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
        JaxrsProxyMap that = (JaxrsProxyMap) o;
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
