package io.roastedroot.proxywasm.v1;

import com.dylibso.chicory.runtime.HostFunction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

    public static String string(byte[] value) {
        return new String(value, StandardCharsets.UTF_8);
    }

    public static int len(byte[] value) {
        if (value == null) {
            return 0;
        }
        return value.length;
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
}
