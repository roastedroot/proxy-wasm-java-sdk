package io.roastedroot.proxywasm.jaxrs.example;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.plugin.Plugin;
import io.roastedroot.proxywasm.plugin.PluginFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.nio.file.Path;

@ApplicationScoped
public class App {

    private static WasmModule module =
            Parser.parse(
                    Path.of("../proxy-wasm-java-host/src/test/go-examples/unit_tester/main.wasm"));

    // configure the the example wasm plugin
    @Produces
    public PluginFactory example() throws StartException {
        return () ->
                Plugin.builder()
                        .withName("example")
                        .withPluginConfig("{ \"type\": \"headerTests\" }")
                        .build(module);
    }
}
