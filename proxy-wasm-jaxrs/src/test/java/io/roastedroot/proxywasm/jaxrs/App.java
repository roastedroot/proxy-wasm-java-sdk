package io.roastedroot.proxywasm.jaxrs;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.StartException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.nio.file.Path;

@ApplicationScoped
public class App {

    private static final WasmModule httpHeadersModule =
            Parser.parse(
                    Path.of("../proxy-wasm-java-host/src/test/go-examples/http_headers/main.wasm"));

    @Produces
    public WasmPluginFactory createFoo() throws StartException {
        return () ->
                WasmPlugin.builder()
                        .withName("foo")
                        .withPluginConfig("{\"header\": \"x-wasm-header\", \"value\": \"foo\"}")
                        .build(httpHeadersModule);
    }

    @Produces
    public WasmPluginFactory createBar() throws StartException {
        return () ->
                WasmPlugin.builder()
                        .withName("bar")
                        .withPluginConfig("{\"header\": \"x-wasm-header\", \"value\": \"bar\"}")
                        .build(httpHeadersModule);
    }
}
