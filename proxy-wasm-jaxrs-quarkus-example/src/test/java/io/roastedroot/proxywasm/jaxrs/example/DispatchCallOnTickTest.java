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
public class DispatchCallOnTickTest {

    @Inject WasmPluginFeature feature;

    @Test
    public void test() throws InterruptedException, StartException {
        WasmPlugin plugin = feature.pool("dispatchCallOnTickTest").borrow();
        assertNotNull(plugin);

        var logger = (MockLogger) plugin.logger();
        Thread.sleep(300);

        //        for (var l : logger.loggedMessages()) {
        //            System.out.println(l);
        //        }
        assertLogsContain(
                logger.loggedMessages(),
                "set tick period milliseconds: 100",
                "called 1 for contextID=1",
                "called 2 for contextID=1",
                "response header for the dispatched call: Content-Type: text/plain;charset=UTF-8",
                "response header for the dispatched call: echo-accept: */*",
                "response header for the dispatched call: echo-content-length: 0",
                "response header for the dispatched call: echo-Host: some_authority");
        plugin.close(); // so that the ticks don't keep running in the background.
    }
}
