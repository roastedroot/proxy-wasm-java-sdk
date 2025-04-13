package io.roastedroot.proxywasm;

import static io.roastedroot.proxywasm.Helpers.bytes;
import static io.roastedroot.proxywasm.Helpers.len;
import static io.roastedroot.proxywasm.Helpers.string;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArrayBytesProxyMap implements ProxyMap {

    final ArrayList<Map.Entry<String, byte[]>> entries;

    public ArrayBytesProxyMap() {
        this.entries = new ArrayList<>();
    }

    public ArrayBytesProxyMap(int mapSize) {
        this.entries = new ArrayList<>(mapSize);
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public void add(String key, String value) {
        entries.add(Map.entry(key, bytes(value)));
    }

    public void add(String key, byte[] value) {
        entries.add(Map.entry(key, value));
    }

    @Override
    public void put(String key, String value) {
        this.remove(key);
        entries.add(Map.entry(key, bytes(value)));
    }

    public void put(String key, byte[] value) {
        this.remove(key);
        entries.add(Map.entry(key, value));
    }

    @Override
    public Iterable<? extends Map.Entry<String, String>> entries() {
        return entries.stream()
                .map(x -> Map.entry(x.getKey(), string(x.getValue())))
                .collect(Collectors.toList());
    }

    @Override
    public Stream<Map.Entry<byte[], byte[]>> streamBytes() {
        return entries.stream().map(x -> Map.entry(bytes(x.getKey()), x.getValue()));
    }

    @Override
    public String get(String key) {
        return entries.stream()
                .filter(x -> x.getKey().equals(key))
                .map(Map.Entry::getValue)
                .map(Helpers::string)
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
        ArrayBytesProxyMap that = (ArrayBytesProxyMap) o;
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

    /**
     * Encode the map into a byte array.
     */
    @Override
    public byte[] encode() {
        try {
            var baos = new ByteArrayOutputStream();
            var o = new DataOutputStream(baos);
            // Write header size (number of entries)
            int mapSize = this.size();
            o.writeInt(mapSize);

            // write all the key / value sizes.
            for (var entry : entries) {
                o.writeInt(len(entry.getKey()));
                o.writeInt(len(entry.getValue()));
            }

            // write all the key / values
            for (var entry : entries) {
                o.write(bytes(entry.getKey()));
                o.write(0);
                o.write(entry.getValue());
                o.write(0);
            }
            o.close();
            return baos.toByteArray();
        } catch (IOException e) {
            // this should never happen since we are not really doing IO
            throw new RuntimeException(e);
        }
    }
}
