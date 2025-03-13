package io.roastedroot.proxywasm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.Action;
import io.roastedroot.proxywasm.v1.ProxyWasm;
import io.roastedroot.proxywasm.v1.StartException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class HttpHeadersTest {

    private MockHandler handler = new MockHandler();

    @Test
    public void onHttpRequestHeaders() throws StartException {
        var module = Parser.parse(Path.of("./src/test/go-examples/http_headers/main.wasm"));
        try (var proxyWasm = ProxyWasm.builder().build(module)) {

            int id = 0;
            try (var host = proxyWasm.createHttpContext(handler)) {
                id = host.id();
                handler.setHttpRequestHeaders(
                        Map.of(
                                "key1", "value1",
                                "key2", "value2"));
                var action = host.callOnRequestHeaders(false);
                assertEquals(Action.CONTINUE, action);

                // Check headers
                var httpRequestHeaders = handler.getHttpRequestHeaders();
                assertNotNull(httpRequestHeaders);
                assertEquals("best", httpRequestHeaders.get("test"));
            }

            // Check logs
            handler.assertSortedLogsEqual(
                    String.format("%d finished", id),
                    "request header --> key2: value2",
                    "request header --> key1: value1",
                    "request header --> test: best");
        }
    }

    @Test
    public void onHttpResponseHeaders() throws StartException {
        var module = Parser.parse(Path.of("./src/test/go-examples/http_headers/main.wasm"));
        var config =
                String.format(
                        "{\"header\": \"%s\", \"value\": \"%s\"}", "x-wasm-header", "x-value");
        try (var proxyWasm = ProxyWasm.builder().withPluginConfig(config).build(module)) {
            int id = 0;
            try (var host = proxyWasm.createHttpContext(handler)) {
                id = host.id();
                handler.setHttpResponseHeaders(
                        Map.of(
                                "key1", "value1",
                                "key2", "value2"));
                var action = host.callOnResponseHeaders(false);
                assertEquals(Action.CONTINUE, action);
            }

            // Check headers
            assertEquals(
                    Map.of(
                            "key1", "value1",
                            "key2", "value2",
                            "x-wasm-header", "x-value",
                            "x-proxy-wasm-go-sdk-example", "http_headers"),
                    handler.getHttpResponseHeaders());

            // Check logs
            handler.assertSortedLogsEqual(
                    String.format("%d finished", id),
                    "response header <-- key2: value2",
                    "response header <-- key1: value1",
                    "adding header: x-wasm-header=x-value",
                    "response header <-- x-wasm-header: x-value",
                    "response header <-- x-proxy-wasm-go-sdk-example: http_headers");
        }
    }
}
