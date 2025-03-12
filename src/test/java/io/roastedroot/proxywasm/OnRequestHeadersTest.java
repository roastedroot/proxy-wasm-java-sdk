package io.roastedroot.proxywasm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.ProxyWasm;
import io.roastedroot.proxywasm.v1.StartException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class OnRequestHeadersTest {

    @Test
    public void test() throws StartException {

        // This module uses the 0_1_0 ABI
        var module = Parser.parse(Path.of("./src/test/cc-examples/on_request_headers/http.wasm"));

        var handler = new MockHandler();

        try (var proxyWasm = ProxyWasm.builder().withPluginHandler(handler).build(module)) {

            Map<String, String> requestHeaders = Map.of("Hello", "World");

            // create wasm-side context id for current http req
            try (var context = proxyWasm.createHttpContext(handler)) {

                // let the wasm module know the request headers are ready
                handler.setHttpRequestHeader(requestHeaders);
                context.callOnRequestHeaders(true);
                assertEquals(
                        List.of(
                                "[http_wasm_example.cc:33]::onRequestHeaders() print from wasm,"
                                        + " onRequestHeaders, context id: 2",
                                "[http_wasm_example.cc:38]::onRequestHeaders() print from wasm,"
                                        + " Hello -> World"),
                        handler.loggedMessages());
            }
        }
    }
}
