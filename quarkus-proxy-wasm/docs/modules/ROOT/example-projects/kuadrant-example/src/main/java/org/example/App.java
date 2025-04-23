package org.example;

import io.roastedroot.proxywasm.LogHandler;
import io.roastedroot.proxywasm.Plugin;
import io.roastedroot.proxywasm.PluginFactory;
import io.roastedroot.proxywasm.SimpleMetricsHandler;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.kuadrant.example.internal.WasmShimModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Application configuration class for the Kuadrant example.
 * Sets up the Wasm PluginFactory for the Kuadrant plugin.
 */
@ApplicationScoped
public class App {

    /**
     * Default constructor.
     */
    public App() {
        // Default constructor
    }

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

    /**
     * Produces the PluginFactory for the Kuadrant Wasm plugin.
     * Configures the plugin with necessary settings like name, machine factory,
     * logger, plugin configuration, upstream URIs, and metrics handler.
     *
     * @return A configured PluginFactory for the Kuadrant plugin.
     * @throws StartException if there is an error during plugin initialization.
     */
    @Produces
    public PluginFactory kuadrant() throws StartException {
        return () ->
                Plugin.builder(WasmShimModule.load())
                        .withName("kuadrant")
                        .withMachineFactory(WasmShimModule::create)
                        .withLogger(DEBUG ? LogHandler.SYSTEM : null)
                        .withPluginConfig(CONFIG)
                        .withUpstreams(Map.of("limitador", new URI(limitadorUrl)))
                        .withMetricsHandler(new SimpleMetricsHandler())
                        .build();
    }
}
