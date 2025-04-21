package io.roastedroot.proxywasm.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.internal.Action;
import io.roastedroot.proxywasm.internal.ProxyWasm;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/ab4161dcf9246a828008b539a82a1556cf0f2e24/examples/http_routing/main_test.go
 */
public class HttpRoutingTest {
    private static final WasmModule module =
            Parser.parse(Path.of("./src/test/go-examples/http_routing/main.wasm"));

    @Test
    public void canary() throws StartException {
        var handler = new MockHandler();
        handler.setPluginConfig(new byte[] {2});
        ProxyWasm.Builder builder = ProxyWasm.builder().withPluginHandler(handler);
        try (var host = builder.build(module)) {
            try (var context = host.createHttpContext(handler)) {

                // Create and set request headers
                Map<String, String> headers = Map.of(":authority", "my-host.com");

                // Call OnRequestHeaders
                handler.setHttpRequestHeaders(headers);
                Action action = context.callOnRequestHeaders(false);

                // Verify action is CONTINUE
                assertEquals(Action.CONTINUE, action);

                // Get and verify modified headers
                var resultHeaders = handler.getHttpRequestHeaders();
                assertEquals(1, resultHeaders.size());
                assertEquals("my-host.com-canary", resultHeaders.get(":authority"));
            }
        }
    }

    @Test
    public void nonCanary() throws StartException {
        var handler = new MockHandler();
        handler.setPluginConfig(new byte[] {1});
        var module = Parser.parse(Path.of("./src/test/go-examples/http_routing/main.wasm"));
        ProxyWasm.Builder builder = ProxyWasm.builder().withPluginHandler(handler);
        try (var host = builder.build(module)) {
            try (var context = host.createHttpContext(handler)) {

                // Create and set request headers
                Map<String, String> headers = Map.of(":authority", "my-host.com");

                // Call OnRequestHeaders
                handler.setHttpRequestHeaders(headers);
                Action action = context.callOnRequestHeaders(false);

                // Verify action is CONTINUE
                assertEquals(Action.CONTINUE, action);

                // Get and verify modified headers
                var resultHeaders = handler.getHttpRequestHeaders();
                assertEquals(1, resultHeaders.size());
                assertEquals("my-host.com", resultHeaders.get(":authority"));
            }
        }
    }
}
