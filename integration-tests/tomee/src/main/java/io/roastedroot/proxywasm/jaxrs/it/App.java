package io.roastedroot.proxywasm.jaxrs.it;

import com.google.gson.Gson;
import io.roastedroot.proxywasm.PluginFactory;
import io.roastedroot.proxywasm.jaxrs.it.internal.MainWasm;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.util.Map;

/**
 * CDI producers for PluginFactory instances used in integration tests.
 */
@ApplicationScoped
public class App {

    /**
     * Default constructor.
     */
    public App() {
        // Default constructor for CDI
    }

    private static final Gson gson = new Gson();

    /**
     * Configures the headerTests PluginFactory.
     *
     * @return a configured PluginFactory for header testing
     */
    @Produces
    public PluginFactory headerTests() {

        return PluginFactory.builder(MainWasm.load())
                .withMachineFactory(MainWasm::create)
                .withName("headerTests")
                .withShared(true)
                .withPluginConfig(gson.toJson(Map.of("type", "headerTests")))
                .build();
    }
}
