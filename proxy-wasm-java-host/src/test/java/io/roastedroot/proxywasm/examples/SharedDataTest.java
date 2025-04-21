package io.roastedroot.proxywasm.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.internal.Action;
import io.roastedroot.proxywasm.internal.ProxyWasm;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/master/examples/shared_data/main_test.go
 */
public class SharedDataTest {
    private static final WasmModule module =
            Parser.parse(Path.of("./src/test/go-examples/shared_data/main.wasm"));

    @Test
    public void testSetEffectiveContext() throws StartException {
        var sharedData = new MockSharedHandler();
        var handler = new MockHandler(sharedData);

        // Create and configure the ProxyWasm instance
        try (var host = ProxyWasm.builder().withPluginHandler(handler).build(module)) {

            // Initialize context
            try (var context = host.createHttpContext(handler)) {

                // Call OnHttpRequestHeaders.
                Action action = context.callOnRequestHeaders(false);
                assertEquals(Action.CONTINUE, action);

                // Check Envoy logs.
                handler.assertLogsContain("shared value: 1");

                // Call OnHttpRequestHeaders again.
                action = context.callOnRequestHeaders(false);
                assertEquals(Action.CONTINUE, action);
                action = context.callOnRequestHeaders(false);
                assertEquals(Action.CONTINUE, action);

                // Check Envoy logs.
                handler.assertLogsContain("shared value: 3");
            }
        }
    }
}
