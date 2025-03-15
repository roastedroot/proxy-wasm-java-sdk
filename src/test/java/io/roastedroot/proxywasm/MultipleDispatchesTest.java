package io.roastedroot.proxywasm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.Action;
import io.roastedroot.proxywasm.v1.ProxyWasm;
import io.roastedroot.proxywasm.v1.StartException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/master/examples/multiple_dispatches/main_test.go
 */
public class MultipleDispatchesTest {
    private final MockHandler handler = new MockHandler();

    @Test
    public void testHttpContextOnHttpRequestHeaders() throws StartException {
        // Load the WASM module
        var module = Parser.parse(Path.of("./src/test/go-examples/multiple_dispatches/main.wasm"));

        // Create and configure the ProxyWasm instance
        try (var host = ProxyWasm.builder().withPluginHandler(handler).build(module)) {

            // Create an HTTP context
            try (var context = host.createHttpContext(handler)) {
                // Set response headers and call OnResponseHeaders
                Map<String, String> headers = Map.of("key", "value");
                handler.setHttpResponseHeaders(headers);
                Action action = context.callOnResponseHeaders(false);

                // Verify the action is PAUSE
                assertEquals(Action.PAUSE, action, "Expected PAUSE action for response headers");

                // Verify DispatchHttpCall is called 10 times
                var httpCalls = handler.getHttpCalls();
                assertEquals(10, httpCalls.size(), "Expected 10 HTTP calls to be dispatched");

                assertEquals(Action.PAUSE, handler.getAction());

                // Emulate Envoy receiving all responses to the dispatched callouts
                for (var entry : httpCalls.entrySet()) {
                    var callout = entry.getValue();
                    host.sendHttpCallResponse(
                            callout.id,
                            new HashMap<>(), // headers
                            new HashMap<>(), // trailers
                            new byte[0] // body
                            );
                }

                assertEquals(Action.CONTINUE, handler.getAction());

                // Check logs for expected messages
                handler.assertLogsContain(
                        "pending dispatched requests: 9",
                        "pending dispatched requests: 1",
                        "response resumed after processed 10 dispatched request");
            }
        }
    }
}
