package io.roastedroot.proxywasm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.Action;
import io.roastedroot.proxywasm.v1.ProxyWasm;
import io.roastedroot.proxywasm.v1.StartException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/master/examples/shared_data/main_test.go
 */
public class SharedDataTest {

    @Test
    public void testSetEffectiveContext() throws StartException {

        var handler = new MockHandler();
        // Load the WASM module
        var module = Parser.parse(Path.of("./src/test/go-examples/shared_data/main.wasm"));

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
