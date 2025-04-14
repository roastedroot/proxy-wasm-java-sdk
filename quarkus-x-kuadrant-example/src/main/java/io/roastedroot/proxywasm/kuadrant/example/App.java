package io.roastedroot.proxywasm.kuadrant.example;

import com.dylibso.chicory.experimental.aot.AotMachine;
import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.LogHandler;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.plugin.Plugin;
import io.roastedroot.proxywasm.plugin.PluginFactory;
import io.roastedroot.proxywasm.plugin.SimpleMetricsHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class App {

    private static WasmModule module =
            Parser.parse(App.class.getResourceAsStream("wasm_shim.wasm"));

    static final String CONFIG;

    static {
        try (InputStream is = App.class.getResourceAsStream("config.json")) {
            CONFIG = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static final boolean DEBUG = "true".equals(System.getenv("DEBUG"));

    @ConfigProperty(name = "limitador.rls.url")
    String limitadorUrl;

    @Produces
    public PluginFactory kuadrant() throws StartException {
        return () ->
                Plugin.builder()
                        .withName("kuadrant")
                        .withMachineFactory(AotMachine::new)
                        .withLogger(DEBUG ? LogHandler.SYSTEM : null)
                        .withPluginConfig(CONFIG)
                        .withUpstreams(Map.of("limitador", limitadorUrl))
                        .withMetricsHandler(new SimpleMetricsHandler())
                        .build(module);
    }
}
