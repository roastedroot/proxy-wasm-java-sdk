package io.roastedroot.proxywasm.examples;

import static io.roastedroot.proxywasm.internal.Helpers.bytes;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.internal.ProxyWasm;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/mosn/proxy-wasm-go-host/blob/25a9e133320ed52aee6ef87f6dcbed77f526550e/example/main_test.go
 */
public class OnRequestHeadersTest {
    private static final WasmModule module =
            Parser.parse(Path.of("./src/test/cc-examples/on_request_headers/http.wasm"));

    @Test
    public void test() throws StartException {
        // This module uses the 0_1_0 ABI
        var handler = new MockHandler();
        handler.setProperty(List.of("plugin_root_id"), bytes(""));

        ProxyWasm.Builder builder = ProxyWasm.builder().withPluginHandler(handler);

        try (var proxyWasm = builder.build(module)) {

            Map<String, String> requestHeaders = Map.of("Hello", "World");

            // create wasm-side context id for current http req
            try (var context = proxyWasm.createHttpContext(handler)) {

                // let the wasm module know the request headers are ready
                handler.setHttpRequestHeaders(requestHeaders);
                context.callOnRequestHeaders(true);
                assertEquals(
                        List.of(
                                "[http.cc:36]::onRequestHeaders() print from wasm,"
                                        + " onRequestHeaders, context id: 2",
                                "[http.cc:41]::onRequestHeaders() print from wasm,"
                                        + " Hello -> World"),
                        handler.loggedMessages());
            }
        }
    }
}
