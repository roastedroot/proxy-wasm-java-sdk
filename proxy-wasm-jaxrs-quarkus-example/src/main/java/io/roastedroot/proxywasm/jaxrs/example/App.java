package io.roastedroot.proxywasm.jaxrs.example;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import com.google.gson.Gson;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.jaxrs.WasmPlugin;
import io.roastedroot.proxywasm.jaxrs.WasmPluginFactory;
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

    @Produces
    public WasmPluginFactory headerTests() throws StartException {
        return () ->
                WasmPlugin.builder()
                        .withName("headerTests")
                        .withLogger(new MockLogger("headerTests"))
                        .withPluginConfig(gson.toJson(Map.of("type", "headerTests")))
                        .build(parseTestModule("/go-examples/unit_tester/main.wasm"));
    }

    @Produces
    public WasmPluginFactory headerTestsNotShared() throws StartException {
        return () ->
                WasmPlugin.builder()
                        .withName("headerTestsNotShared")
                        .withShared(false)
                        .withLogger(new MockLogger("headerTestsNotShared"))
                        .withPluginConfig(gson.toJson(Map.of("type", "headerTests")))
                        .build(parseTestModule("/go-examples/unit_tester/main.wasm"));
    }

    @Produces
    public WasmPluginFactory tickTests() throws StartException {
        return () ->
                WasmPlugin.builder()
                        .withName("tickTests")
                        .withLogger(new MockLogger("tickTests"))
                        .withPluginConfig(gson.toJson(Map.of("type", "tickTests")))
                        .build(parseTestModule("/go-examples/unit_tester/main.wasm"));
    }

    @Produces
    public WasmPluginFactory ffiTests() throws StartException {
        return () ->
                WasmPlugin.builder()
                        .withName("ffiTests")
                        .withLogger(new MockLogger("ffiTests"))
                        .withPluginConfig(gson.toJson(Map.of("type", "ffiTests")))
                        .withForeignFunctions(Map.of("reverse", App::reverse))
                        .build(parseTestModule("/go-examples/unit_tester/main.wasm"));
    }

    public static byte[] reverse(byte[] data) {
        byte[] reversed = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            reversed[i] = data[data.length - 1 - i];
        }
        return reversed;
    }

    @Produces
    public WasmPluginFactory httpCallTests() throws StartException {
        return () ->
                WasmPlugin.builder()
                        .withName("httpCallTests")
                        .withLogger(new MockLogger("httpCallTests"))
                        .withPluginConfig(
                                gson.toJson(
                                        Map.of(
                                                "type", "httpCallTests",
                                                "upstream", "web_service",
                                                "path", "/ok")))
                        .withUpstreams(Map.of("web_service", "localhost:8081"))
                        .build(parseTestModule("/go-examples/unit_tester/main.wasm"));
    }
}
