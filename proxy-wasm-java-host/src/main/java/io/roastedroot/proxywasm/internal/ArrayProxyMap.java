package io.roastedroot.proxywasm.internal;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class ArrayProxyMap implements ProxyMap {

    final ArrayList<Map.Entry<String, String>> entries;

    public ArrayProxyMap() {
        this.entries = new ArrayList<>();
    }

    public ArrayProxyMap(int mapSize) {
        this.entries = new ArrayList<>(mapSize);
    }

    public ArrayProxyMap(ProxyMap other) {
        this(other.size());
        for (Map.Entry<String, String> entry : other.entries()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public ArrayProxyMap(Map<String, String> other) {
        this(other.size());
        for (Map.Entry<String, String> entry : other.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public void add(String key, String value) {
        entries.add(Map.entry(key, value));
    }

    @Override
    public void put(String key, String value) {
        this.remove(key);
        entries.add(Map.entry(key, value));
    }

    @Override
    public Iterable<? extends Map.Entry<String, String>> entries() {
        return entries;
    }

    @Override
    public String get(String key) {
        return entries.stream()
                .filter(x -> x.getKey().equals(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void remove(String key) {
        entries.removeIf(x -> x.getKey().equals(key));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ArrayProxyMap that = (ArrayProxyMap) o;
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
