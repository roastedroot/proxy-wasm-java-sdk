package io.roastedroot.proxywasm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/master/examples/postpone_requests/main_test.go
 */
public class PostponeRequestsTest {

    @Test
    public void testSetEffectiveContext() throws StartException {

        var handler = new MockHandler();
        // Load the WASM module
        var module = Parser.parse(Path.of("./src/test/go-examples/postpone_requests/main.wasm"));

        // Create and configure the ProxyWasm instance
        try (var host = ProxyWasm.builder().withPluginHandler(handler).build(module)) {

            // Initialize context
            try (var context = host.createHttpContext(handler)) {

                // Call OnHttpRequestHeaders
                Action action = context.callOnRequestHeaders(false);
                assertEquals(
                        Action.PAUSE, action, "Expected PAUSE action for OnHttpRequestHeaders");

                // Call OnTick
                host.tick();

                action = handler.getAction();
                assertEquals(Action.CONTINUE, action, "Expected CONTINUE action after OnTick");
            }
        }
    }
}
