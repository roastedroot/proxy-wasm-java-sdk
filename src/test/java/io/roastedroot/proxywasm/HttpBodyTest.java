package io.roastedroot.proxywasm;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpBodyTest {

    @Test
    public void testSetBodyContent() throws StartException {

        var loggedMessages = new ArrayList<String>();

        var handler = new DefaultHandler() {
            @Override
            public void log(LogLevel level, String message) throws WasmException {
                loggedMessages.add(message);
            }
        };
        ProxyWasm.Builder builder = ProxyWasm.builder();

        var module = Parser.parse(Path.of("./src/test/go-examples/http_body/main.wasm"));
        try (var proxyWasm = builder.build(module)) {

            // Create http context.
            try (var httpContext = proxyWasm.createHttpContext(handler)) {

                // Call OnRequestHeaders.
                var action = httpContext.onRequestHeaders(Map.of(
                        "content-length", "10",
                        "buffer-operation", "replace"
                ), false);

                // Must be continued.
                assertEquals(Action.CONTINUE, action);


            }
        }

    }
}
