package org.example;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.LogHandler;
import io.roastedroot.proxywasm.Plugin;
import io.roastedroot.proxywasm.PluginFactory;
import io.roastedroot.proxywasm.SimpleMetricsHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Application configuration class for the Coraza WAF example.
 * Sets up the Wasm PluginFactory for the WAF plugin.
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
            Parser.parse(App.class.getResourceAsStream("coraza-proxy-wasm.wasm"));

    static final String CONFIG;

    static {
        try (InputStream is = App.class.getResourceAsStream("waf-config.json")) {
            CONFIG = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static final boolean DEBUG = "true".equals(System.getenv("DEBUG"));

    /**
     * Produces the PluginFactory for the Coraza WAF Wasm plugin.
     * Configures the plugin with necessary settings like name, shared status,
     * logger, plugin configuration, and metrics handler.
     *
     * @return A configured PluginFactory for the WAF plugin.
     */
    @Produces
    public PluginFactory waf() {
        return () ->
                Plugin.builder(module)
                        .withName("waf")
                        .withShared(true)
                        .withLogger(DEBUG ? LogHandler.SYSTEM : null)
                        .withPluginConfig(CONFIG)
                        .withMetricsHandler(new SimpleMetricsHandler())
                        .build();
    }
}
