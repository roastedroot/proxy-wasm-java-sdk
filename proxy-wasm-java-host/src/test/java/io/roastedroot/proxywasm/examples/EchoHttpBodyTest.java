package io.roastedroot.proxywasm.examples;

import static io.roastedroot.proxywasm.Helpers.bytes;
import static io.roastedroot.proxywasm.Helpers.string;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.HttpContext;
import io.roastedroot.proxywasm.ProxyWasm;
import io.roastedroot.proxywasm.StartException;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/ab4161dcf9246a828008b539a82a1556cf0f2e24/examples/http_body/main_test.go
 */
public class EchoHttpBodyTest {
    private static final WasmModule module =
            Parser.parse(Path.of("./src/test/go-examples/http_body/main.wasm"));

    private MockHandler handler;
    private ProxyWasm proxyWasm;
    private HttpContext httpContext;

    @BeforeEach
    void setUp() throws StartException {
        this.handler = new MockHandler();
        ProxyWasm.Builder builder = ProxyWasm.builder();
        builder.withPluginConfig("echo");
        this.proxyWasm = builder.build(module);
        this.httpContext = proxyWasm.createHttpContext(handler);
    }

    @AfterEach
    void tearDown() {
        httpContext.close();
        proxyWasm.close();
    }

    @Test
    public void pauseUntilEOS() throws StartException {
        // Call callOnRequestBody
        handler.setHttpRequestBody(bytes("aaaa"));
        var action = httpContext.callOnRequestBody(false /* end of stream */);

        // Must be paused.
        Assertions.assertEquals(Action.PAUSE, action);
    }

    @Test
    public void echoRequest() throws StartException {
        for (var frame : new String[] {"frame1...", "frame2...", "frame3..."}) {
            // Call callOnRequestBody without "content-length"
            handler.appendHttpRequestBody(bytes(frame));
            var action = httpContext.callOnRequestBody(false /* end of stream */);

            // Must be paused.
            assertEquals(Action.PAUSE, action);
        }

        handler.appendHttpRequestBody(null);
        var action = httpContext.callOnRequestBody(true /* end of stream */);

        // Must be paused.
        assertEquals(Action.PAUSE, action);

        var response = handler.getSentHttpResponse();
        assertNotNull(response);
        assertEquals(200, response.statusCode);
        assertEquals("frame1...frame2...frame3...", string(response.body));
    }
}
