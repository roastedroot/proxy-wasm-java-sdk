package io.roastedroot.proxywasm.jaxrs;

import static io.roastedroot.proxywasm.jaxrs.TestHelpers.parseTestModule;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import io.roastedroot.proxywasm.StartException;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ForeignCallOnTickTest {

    @Produces
    public WasmPluginFactory create() throws StartException {
        return () ->
                WasmPlugin.builder()
                        .withName("foreignCallOnTickTest")
                        .withLogger(new MockLogger())
                        .withMinTickPeriodMilliseconds(
                                100) // plugin wants a tick every 1 ms, that's too often
                        .withForeignFunctions(Map.of("compress", data -> data))
                        .build(parseTestModule("/go-examples/foreign_call_on_tick/main.wasm"));
    }

    @Inject WasmPluginFeature feature;

    @Test
    public void testRequest() throws InterruptedException, StartException {
        WasmPlugin plugin = feature.pool("foreignCallOnTickTest").borrow();
        assertNotNull(plugin);
        assertEquals(1, plugin.handler.getTickPeriodMilliseconds());

        var logger = (MockLogger) plugin.handler.logger;
        Thread.sleep(200);
        logger.assertLogsContain(
                String.format(
                        "foreign function (compress) called: %d, result: %s",
                        1, "68656c6c6f20776f726c6421"));
        plugin.handler.logger = null;
        plugin.close(); // so that the ticks don't keep running in the background.
    }
}
