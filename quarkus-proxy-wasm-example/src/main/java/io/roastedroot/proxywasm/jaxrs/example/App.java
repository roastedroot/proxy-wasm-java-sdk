package io.roastedroot.proxywasm.jaxrs.example;

import com.dylibso.chicory.experimental.aot.AotMachine;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.Plugin;
import io.roastedroot.proxywasm.PluginFactory;
import io.roastedroot.proxywasm.StartException;
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
                Plugin.builder(module)
                        .withName("example")
                        .withShared(true)
                        .withPluginConfig("{ \"type\": \"headerTests\" }")
                        .withMachineFactory(AotMachine::new)
                        .build();
    }
}
