package io.roastedroot.proxywasm;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A ProxyMap implementation that chains to another ProxyMap instance.
 */
public class SplitProxyMap implements ProxyMap {

    private final ProxyMap primary;
    private final ProxyMap secondary;

    public SplitProxyMap(ProxyMap primary, ProxyMap secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    @Override
    public int size() {
        return primary.size() + secondary.size();
    }

    @Override
    public void add(String key, String value) {
        secondary.add(key, value);
    }

    @Override
    public void put(String key, String value) {
        if (primary.get(key) != null) {
            primary.put(key, value);
        }
        secondary.put(key, value);
    }

    @Override
    public Iterable<? extends Map.Entry<String, String>> entries() {
        return Stream.concat(
                        StreamSupport.stream(primary.entries().spliterator(), false),
                        StreamSupport.stream(secondary.entries().spliterator(), false))
                .collect(Collectors.toList());
    }

    @Override
    public String get(String key) {
        String value = primary.get(key);
        if (value != null) {
            return value;
        }
        return secondary.get(key);
    }

    @Override
    public void remove(String key) {
        secondary.remove(key);
    }
}
