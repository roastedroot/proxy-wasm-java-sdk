package io.roastedroot.proxywasm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.Action;
import io.roastedroot.proxywasm.v1.ProxyWasm;
import io.roastedroot.proxywasm.v1.StartException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class HttpRoutingTest {

    @Test
    public void canary() throws StartException {
        var handler = new MockHandler();
        var module = Parser.parse(Path.of("./src/test/go-examples/http_routing/main.wasm"));
        ProxyWasm.Builder builder =
                ProxyWasm.builder().withPluginHandler(handler).withPluginConfig(new byte[] {2});
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
                Map<String, String> resultHeaders = handler.getHttpRequestHeaders();
                assertEquals(1, resultHeaders.size());
                assertEquals("my-host.com-canary", resultHeaders.get(":authority"));
            }
        }
    }

    @Test
    public void nonCanary() throws StartException {
        var handler = new MockHandler();
        var module = Parser.parse(Path.of("./src/test/go-examples/http_routing/main.wasm"));
        ProxyWasm.Builder builder =
                ProxyWasm.builder().withPluginHandler(handler).withPluginConfig(new byte[] {1});
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
                Map<String, String> resultHeaders = handler.getHttpRequestHeaders();
                assertEquals(1, resultHeaders.size());
                assertEquals("my-host.com", resultHeaders.get(":authority"));
            }
        }
    }
}
