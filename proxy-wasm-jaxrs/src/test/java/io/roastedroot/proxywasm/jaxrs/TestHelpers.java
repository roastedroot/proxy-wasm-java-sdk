package io.roastedroot.proxywasm.jaxrs;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import java.nio.file.Path;

public final class TestHelpers {
    private TestHelpers() {}

    public static final String EXAMPLES_DIR = "../proxy-wasm-java-host/src/test";

    public static WasmModule parseTestModule(String file) {
        return Parser.parse(Path.of(EXAMPLES_DIR + file));
    }
}
