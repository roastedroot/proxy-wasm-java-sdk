package io.roastedroot.proxywasm.jaxrs.example;

import static io.roastedroot.proxywasm.jaxrs.example.Helpers.assertLogsContain;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.jaxrs.WasmPlugin;
import io.roastedroot.proxywasm.jaxrs.WasmPluginFeature;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ForeignCallOnTickTest {

    @Inject WasmPluginFeature feature;

    @Test
    public void testRequest() throws InterruptedException, StartException {
        WasmPlugin plugin = feature.pool("foreignCallOnTickTest").borrow();
        assertNotNull(plugin);

        var logger = (MockLogger) plugin.logger();
        Thread.sleep(200);
        assertLogsContain(
                logger.loggedMessages(),
                String.format(
                        "foreign function (compress) called: %d, result: %s",
                        1, "68656c6c6f20776f726c6421"));
        plugin.close(); // so that the ticks don't keep running in the background.
    }
}
