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

    public static int length(byte[] value) {
        if (value == null) {
            return 0;
        }
        return value.length;
    }

    public static <K, V> int length(Map<K, V> value) {
        if (value == null) {
            return 0;
        }
        return value.size();
    }
}
