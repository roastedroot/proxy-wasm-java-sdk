package io.roastedroot.proxywasm;

import static io.roastedroot.proxywasm.v1.Helpers.bytes;
import static io.roastedroot.proxywasm.v1.Helpers.string;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.Action;
import io.roastedroot.proxywasm.v1.HttpContext;
import io.roastedroot.proxywasm.v1.ProxyWasm;
import io.roastedroot.proxywasm.v1.StartException;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EchoHttpBodyTest {

    private MockHandler handler;
    private ProxyWasm proxyWasm;
    private HttpContext httpContext;

    @BeforeEach
    void setUp() throws StartException {
        this.handler = new MockHandler();
        ProxyWasm.Builder builder = ProxyWasm.builder();
        builder.withPluginConfig("echo");
        var module = Parser.parse(Path.of("./src/test/go-examples/http_body/main.wasm"));
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
        assertEquals(Action.PAUSE, action);
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
