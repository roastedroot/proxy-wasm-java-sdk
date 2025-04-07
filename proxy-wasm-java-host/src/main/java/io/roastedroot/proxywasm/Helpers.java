package io.roastedroot.proxywasm;

import com.dylibso.chicory.runtime.HostFunction;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class Helpers {

    private Helpers() {}

    public static HostFunction[] withModuleName(HostFunction[] hostFunctions, String moduleName) {
        return Arrays.stream(hostFunctions)
                .map(
                        hf ->
                                new HostFunction(
                                        moduleName,
                                        hf.name(),
                                        hf.paramTypes(),
                                        hf.returnTypes(),
                                        hf.handle()))
                .toArray(HostFunction[]::new);
    }

    public static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] bytes(Date value) {
        // encode using
        // https://protobuf.dev/reference/protobuf/google.protobuf/#timestamp
        var instant = value.toInstant();
        var rfc3339String = instant.toString();
        return bytes(rfc3339String);
    }

    public static byte[] bytes(Duration value) {
        // encode using
        // https://protobuf.dev/reference/protobuf/google.protobuf/#duration
        return bytes(String.format("%d.%09d", value.getSeconds(), value.getNano()));
    }

    public static byte[] bytes(int value) {
        return ByteBuffer.allocate(Integer.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array();
    }

    public static byte[] bytes(long value) {
        return ByteBuffer.allocate(Long.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(value)
                .array();
    }

    public static int int32(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            throw new IllegalArgumentException("Byte array must be exactly 4 bytes long");
        }
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static long int64(byte[] bytes) {
        if (bytes == null || bytes.length != 8) {
            throw new IllegalArgumentException("Byte array must be exactly 8 bytes long");
        }
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    public static String string(byte[] value) {
        if (value == null) {
            return null;
        }
        return new String(value, StandardCharsets.UTF_8);
    }

    public static List<String> split(String str, char separator) {
        ArrayList<String> parts = new ArrayList<>();
        int start = 0;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == separator) {
                parts.add(str.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(str.substring(start)); // Add the last part
        return List.copyOf(parts);
    }

    public static int len(byte[] value) {
        if (value == null) {
            return 0;
        }
        return value.length;
    }

    public static int len(ProxyMap value) {
        if (value == null) {
            return 0;
        }
        return value.size();
    }

    public static <T> int len(String value) {
        if (value == null) {
            return 0;
        }
        return value.length();
    }

    public static <T> int len(T[] value) {
        if (value == null) {
            return 0;
        }
        return value.length;
    }

    public static <K, V> int len(Map<K, V> value) {
        if (value == null) {
            return 0;
        }
        return value.size();
    }

    public static byte[] append(byte[] value1, byte[] value2) {
        if (len(value1) == 0) {
            return value2;
        }
        if (len(value2) == 0) {
            return value1;
        }
        byte[] result = new byte[value1.length + value2.length];
        System.arraycopy(value1, 0, result, 0, value1.length);
        System.arraycopy(value2, 0, result, value1.length, value2.length);
        return result;
    }

    public static String[] append(String[] value1, String... value2) {
        if (len(value1) == 0) {
            return value2;
        }
        if (len(value2) == 0) {
            return value1;
        }
        String[] result = new String[value1.length + value2.length];
        System.arraycopy(value1, 0, result, 0, value1.length);
        System.arraycopy(value2, 0, result, value1.length, value2.length);
        return result;
    }

    public static byte[] replaceBytes(
            byte[] existing, byte[] change, int replaceStart, int replaceLength) {

        if (replaceStart > existing.length) {
            replaceStart = existing.length;
        }
        if (replaceLength > existing.length) {
            replaceLength = existing.length;
        }

        // when we are replacing the whole buffer
        if (replaceStart == 0 && replaceLength == existing.length) {
            return change;
        }

        int newLength = change.length + (existing.length - replaceLength);
        byte[] result = new byte[newLength];

        // Copy the unchanged part before the start position
        System.arraycopy(existing, 0, result, 0, Math.min(replaceStart, existing.length));

        // Copy the new change bytes
        System.arraycopy(change, 0, result, replaceStart, change.length);

        // Copy the remaining unchanged part after replacement
        if (replaceStart + replaceLength < existing.length) {
            System.arraycopy(
                    existing,
                    replaceStart + replaceLength,
                    result,
                    replaceStart + change.length,
                    existing.length - (replaceStart + replaceLength));
        }

        return result;
    }

    static final int U32_LEN = 4;
}
