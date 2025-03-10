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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class OnRequestHeadersTest {

    @Test
    public void test() throws StartException {

        // This module uses the 0_1_0 ABI
        var module = Parser.parse(Path.of("./src/test/cc-examples/on_request_headers/http.wasm"));

        try (var proxyWasm = ProxyWasm.builder().build(module)) {


            Map<String, String> requestHeaders = Map.of("Hello", "World");
            AtomicInteger getHttpRequestHeaderCounter = new AtomicInteger(0);

            //  TODO: if the proxyWasm is being use in a concurrently, it should
            //  be locked for exclusive access before the handler is set.
            ArrayList<String> loggedMessages = new ArrayList<>();

            // Set the import handler to the current request.
            proxyWasm.setHandler(new DefaultHandler() {
                String test= "test";

                @Override
                public void log(LogLevel level, String message) throws WasmException {
                    loggedMessages.add(message);
                }

                @Override
                public Map<String, String> getHttpRequestHeader() {
                    getHttpRequestHeaderCounter.incrementAndGet();
                    return requestHeaders;
                }
            });

            // create wasm-side context id for current http req
            try (var context = proxyWasm.createContext()) {

                // let the wasm module know the request headers are ready
                assertEquals(0, getHttpRequestHeaderCounter.get());
                context.onRequestHeaders(requestHeaders.size(), true);
                assertEquals(1, getHttpRequestHeaderCounter.get());

                assertEquals(List.of(
                        "[http_wasm_example.cc:33]::onRequestHeaders() print from wasm, onRequestHeaders, context id: 2",
                        "[http_wasm_example.cc:38]::onRequestHeaders() print from wasm, Hello -> World"
                ), loggedMessages);

            }
        }

    }
}
