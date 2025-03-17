package io.roastedroot.proxywasm;

import java.util.Map;

public interface ProxyMap {

    static ProxyMap of(String... values) {
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("values must be even");
        }
        ArrayProxyMap map = new ArrayProxyMap(values.length / 2);
        for (int i = 0; i < values.length; i += 2) {
            map.add(values[i], values[i + 1]);
        }
        return map;
    }

    static ProxyMap copyOf(Map<String, String> headers) {
        if (headers == null) {
            return null;
        }
        return new ArrayProxyMap(headers);
    }

    int size();

    void add(String key, String value);

    void put(String key, String value);

    Iterable<? extends Map.Entry<String, String>> entries();

    String get(String key);

    void remove(String key);
}
