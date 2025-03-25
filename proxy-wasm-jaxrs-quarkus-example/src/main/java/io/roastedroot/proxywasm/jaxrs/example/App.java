package io.roastedroot.proxywasm.jaxrs.example;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
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

    public static WasmModule parseTestModule(String file) {
        return Parser.parse(Path.of(EXAMPLES_DIR + file));
    }

    @Produces
    public WasmPluginFactory foreignCallOnTickTest() throws StartException {
        return () ->
                WasmPlugin.builder()
                        .withName("foreignCallOnTickTest")
                        .withLogger(new MockLogger())
                        .withMinTickPeriodMilliseconds(
                                100) // plugin wants a tick every 1 ms, that's too often
                        .withForeignFunctions(Map.of("compress", data -> data))
                        .build(parseTestModule("/go-examples/foreign_call_on_tick/main.wasm"));
    }

    @Produces
    public WasmPluginFactory notSharedHttpHeaders() throws StartException {
        return () ->
                WasmPlugin.builder()
                        .withName("notSharedHttpHeaders")
                        .withShared(false)
                        .withPluginConfig("{\"header\": \"x-wasm-header\", \"value\": \"foo\"}")
                        .build(parseTestModule("/go-examples/http_headers/main.wasm"));
    }

    @Produces
    public WasmPluginFactory httpHeaders() throws StartException {
        return () ->
                WasmPlugin.builder()
                        .withName("httpHeaders")
                        .withPluginConfig("{\"header\": \"x-wasm-header\", \"value\": \"foo\"}")
                        .build(parseTestModule("/go-examples/http_headers/main.wasm"));
    }

    @Produces
    public WasmPluginFactory dispatchCallOnTickTest() throws StartException {
        return () ->
                WasmPlugin.builder()
                        .withName("dispatchCallOnTickTest")
                        .withLogger(new MockLogger())
                        .withUpstreams(Map.of("web_service", "localhost:8081"))
                        .build(parseTestModule("/go-examples/dispatch_call_on_tick/main.wasm"));
    }
}
