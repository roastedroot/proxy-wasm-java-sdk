package io.roastedroot.proxywasm.jaxrs.example;

import com.dylibso.chicory.experimental.aot.AotMachine;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.Plugin;
import io.roastedroot.proxywasm.PluginFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.nio.file.Path;

/**
 * Application configuration class for the proxy-wasm JAX-RS example.
 * Sets up the Wasm PluginFactory for the example plugin.
 */
@ApplicationScoped
public class App {

    /**
     * Default constructor.
     */
    public App() {
        // Default constructor
    }

    private static WasmModule module =
            Parser.parse(
                    Path.of("../proxy-wasm-java-host/src/test/go-examples/unit_tester/main.wasm"));

    /**
     * Produces the PluginFactory for the example Wasm plugin.
     * Configures the plugin with necessary settings like name, shared status,
     * plugin configuration, and machine factory.
     *
     * @return A configured PluginFactory for the example plugin.
     */
    @Produces
    public PluginFactory example() {
        return () ->
                Plugin.builder(module)
                        .withName("example")
                        .withShared(true)
                        .withPluginConfig("{ \"type\": \"headerTests\" }")
                        .withMachineFactory(AotMachine::new)
                        .build();
    }
}
