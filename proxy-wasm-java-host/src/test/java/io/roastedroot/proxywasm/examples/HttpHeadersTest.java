package io.roastedroot.proxywasm.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.internal.Action;
import io.roastedroot.proxywasm.internal.ProxyMap;
import io.roastedroot.proxywasm.internal.ProxyWasm;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/ab4161dcf9246a828008b539a82a1556cf0f2e24/examples/http_headers/main_test.go
 */
public class HttpHeadersTest {
    private static final WasmModule module =
            Parser.parse(Path.of("./src/test/go-examples/http_headers/main.wasm"));

    private MockHandler handler = new MockHandler();

    @Test
    public void onHttpRequestHeaders() throws StartException {
        try (var proxyWasm = ProxyWasm.builder().build(module)) {

            int id = 0;
            try (var host = proxyWasm.createHttpContext(handler)) {
                id = host.id();
                handler.setHttpRequestHeaders(
                        ProxyMap.of(
                                "key1", "value1",
                                "key2", "value2"));
                var action = host.callOnRequestHeaders(false);
                Assertions.assertEquals(Action.CONTINUE, action);

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
        handler.setPluginConfig(config);
        try (var proxyWasm = ProxyWasm.builder().withPluginHandler(handler).build(module)) {
            int id = 0;
            try (var host = proxyWasm.createHttpContext(handler)) {
                id = host.id();
                handler.setHttpResponseHeaders(
                        ProxyMap.of(
                                "key1", "value1",
                                "key2", "value2"));
                var action = host.callOnResponseHeaders(false);
                assertEquals(Action.CONTINUE, action);
            }

            // Check headers
            assertEquals(
                    ProxyMap.of(
                            "key1", "value1",
                            "key2", "value2",
                            "x-proxy-wasm-go-sdk-example", "http_headers",
                            "x-proxy-wasm-counter", "1",
                            "x-wasm-header", "x-value"),
                    handler.getHttpResponseHeaders());

            // Check logs
            handler.assertSortedLogsEqual(
                    String.format("%d finished", id),
                    "loading plugin config",
                    "header from config: x-wasm-header = x-value",
                    "response header <-- key2: value2",
                    "response header <-- key1: value1",
                    "adding header: x-wasm-header=x-value",
                    "response header <-- x-wasm-header: x-value",
                    "response header <-- x-proxy-wasm-counter: 1",
                    "response header <-- x-proxy-wasm-go-sdk-example: http_headers");
        }
    }
}
