package io.roastedroot.proxywasm.v1;

import com.dylibso.chicory.runtime.HostFunction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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

    public static byte[] bytes(String contentMustBeProvided) {
        return contentMustBeProvided.getBytes(StandardCharsets.UTF_8);
    }

    public static String string(byte[] body) {
        return new String(body, StandardCharsets.UTF_8);
    }
}
