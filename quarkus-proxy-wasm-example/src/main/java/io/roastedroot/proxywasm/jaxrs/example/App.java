package io.roastedroot.proxywasm.jaxrs.example;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import com.google.gson.Gson;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.plugin.Plugin;
import io.roastedroot.proxywasm.plugin.PluginFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.nio.file.Path;
import java.util.Map;

@ApplicationScoped
public class App {

    public static final String EXAMPLES_DIR = "../proxy-wasm-java-host/src/test";
    private static final Gson gson = new Gson();

    public static WasmModule parseTestModule(String file) {
        return Parser.parse(Path.of(EXAMPLES_DIR + file));
    }

    // configure the ffiTests wasm plugin
    @Produces
    public PluginFactory ffiTests() throws StartException {
        return () ->
                Plugin.builder()
                        .withName("ffiTests")
                        .withPluginConfig("{ \"type\": \"ffiTests\" }")
                        .withForeignFunctions(Map.of("reverse", App::reverse))
                        .build(parseTestModule("/go-examples/unit_tester/main.wasm"));
    }

    // This function can be called from the Wasm module
    public static byte[] reverse(byte[] data) {
        byte[] reversed = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            reversed[i] = data[data.length - 1 - i];
        }
        return reversed;
    }
}
