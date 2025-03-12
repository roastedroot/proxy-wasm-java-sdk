package io.roastedroot.proxywasm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.Action;
import io.roastedroot.proxywasm.v1.Handler;
import io.roastedroot.proxywasm.v1.LogLevel;
import io.roastedroot.proxywasm.v1.ProxyWasm;
import io.roastedroot.proxywasm.v1.StartException;
import io.roastedroot.proxywasm.v1.WasmException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class HttpBodyTest {

    @Test
    @Disabled("implementation is still in progress")
    public void testSetBodyContent() throws StartException {

        var loggedMessages = new ArrayList<String>();

        var handler =
                new Handler() {
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
                var action =
                        httpContext.onRequestHeaders(
                                Map.of(
                                        "content-length", "10",
                                        "buffer-operation", "replace"),
                                false);

                // Must be continued.
                assertEquals(Action.CONTINUE, action);
            }
        }
    }
}
