package io.roastedroot.proxywasm;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.Handler;
import io.roastedroot.proxywasm.v1.LogLevel;
import io.roastedroot.proxywasm.v1.ProxyWasmEnv;
import io.roastedroot.proxywasm.v1.WasmException;
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
public class ExampleTest {

    @Test
    public void example() {
        assertTrue(true);
        var module = Parser.parse(Path.of("./src/test/resources/data/http.wasm"));

        try (var proxyWasm = ProxyWasmEnv.builder(module).build()) {


            Map<String, String> requestHeaders = Map.of("Hello", "World");
            AtomicInteger getHttpRequestHeaderCounter = new AtomicInteger(0);

            //  TODO: if the proxyWasm is being use in a concurrently, it should
            //  be locked for exclusive access before the handler is set.
            ArrayList<String> loggedMessages = new ArrayList<>();


            // Set the import handler to the current request.
            proxyWasm.setHandler(new Handler() {

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
