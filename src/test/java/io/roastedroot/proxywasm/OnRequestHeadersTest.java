package io.roastedroot.proxywasm;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for simple App.
 */
public class OnRequestHeadersTest {

    @Test
    public void test() throws StartException {

        // This module uses the 0_1_0 ABI
        var module = Parser.parse(Path.of("./src/test/cc-examples/on_request_headers/http.wasm"));

        //  TODO: if the proxyWasm is being use in a concurrently, it should
        //  be locked for exclusive access before the handler is set.
        ArrayList<String> loggedMessages = new ArrayList<>();

        // Set the import handler to the current request.
        DefaultHandler handler = new DefaultHandler() {
            @Override
            public void log(LogLevel level, String message) throws WasmException {
                loggedMessages.add(message);
            }
        };

        try (var proxyWasm = ProxyWasm.builder().withPluginHandler(handler).build(module)) {

            Map<String, String> requestHeaders = Map.of("Hello", "World");

            // create wasm-side context id for current http req
            try (var context = proxyWasm.createHttpContext(handler)) {

                // let the wasm module know the request headers are ready
                context.onRequestHeaders(requestHeaders, true);
                assertEquals(List.of(
                        "[http_wasm_example.cc:33]::onRequestHeaders() print from wasm, onRequestHeaders, context id: 2",
                        "[http_wasm_example.cc:38]::onRequestHeaders() print from wasm, Hello -> World"
                ), loggedMessages);

            }
        }

    }
}
