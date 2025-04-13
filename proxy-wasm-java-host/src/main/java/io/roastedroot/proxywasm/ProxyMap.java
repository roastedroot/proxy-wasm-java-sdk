package io.roastedroot.proxywasm;

import static io.roastedroot.proxywasm.Helpers.bytes;
import static io.roastedroot.proxywasm.Helpers.len;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    default Stream<Map.Entry<byte[], byte[]>> streamBytes() {
        return StreamSupport.stream(entries().spliterator(), false)
                .map(x -> Map.entry(bytes(x.getKey()), bytes(x.getValue())));
    }

    String get(String key);

    void remove(String key);

    /**
     * Encode the map into a byte array.
     */
    default byte[] encode() {
        try {
            var baos = new ByteArrayOutputStream();
            var o = new DataOutputStream(baos);
            // Write header size (number of entries)
            int mapSize = this.size();
            o.writeInt(mapSize);

            // write all the key / value sizes.
            ArrayList<Map.Entry<byte[], byte[]>> entries = new ArrayList<>(this.size());
            for (var entry : this.entries()) {
                var encoded = Map.entry(bytes(entry.getKey()), bytes(entry.getValue()));
                entries.add(encoded);
                o.writeInt(len(encoded.getKey()));
                o.writeInt(len(encoded.getValue()));
            }

            // write all the key / values
            for (var entry : entries) {
                o.write(entry.getKey());
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
