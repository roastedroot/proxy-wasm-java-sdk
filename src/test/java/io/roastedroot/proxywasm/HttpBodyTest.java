package io.roastedroot.proxywasm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.Action;
import io.roastedroot.proxywasm.v1.Helpers;
import io.roastedroot.proxywasm.v1.HttpContext;
import io.roastedroot.proxywasm.v1.ProxyWasm;
import io.roastedroot.proxywasm.v1.StartException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HttpBodyTest {

    private MockHandler handler;
    private ProxyWasm proxyWasm;
    private HttpContext httpContext;

    @BeforeEach
    void setUp() throws StartException {
        this.handler = new MockHandler();
        ProxyWasm.Builder builder = ProxyWasm.builder();
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
    public void testRemoveRequestHeader() throws StartException {

        // Call OnRequestHeaders.
        var action =
                httpContext.callOnRequestHeaders(
                        Map.of(
                                "content-length", "10",
                                "buffer-operation", "replace"),
                        false);

        // Must be continued.
        assertEquals(Action.CONTINUE, action);

        var headers = handler.getHttpRequestHeader();
        assertEquals(Map.of("buffer-operation", "replace"), headers);
    }

    @Test
    public void test400Response() throws StartException {
        // Call OnRequestHeaders.
        var action = httpContext.callOnRequestHeaders(Map.of(), false);

        // Must be paused.
        assertEquals(Action.PAUSE, action);

        // Check the local response.
        var response = handler.getSenthttpResponse();
        assertNotNull(response);
        assertEquals(400, response.statusCode);
        assertEquals("content must be provided", Helpers.string(response.body));
    }

    @Test
    public void testPauseUntilEOS() throws StartException {
        // Call callOnRequestBody
        var action =
                httpContext.callOnRequestBody(
                        "aaaa".getBytes(StandardCharsets.UTF_8), false /* end of stream */);

        // Must be paused.
        assertEquals(Action.PAUSE, action);
    }

    @Test
    public void testAppend() throws StartException {
        // Call callOnRequestBody
        var action =
                httpContext.callOnRequestHeaders(
                        Map.of(
                                "content-length", "10",
                                "buffer-operation", "append"),
                        false /* end of stream */);

        // Must be continued.
        assertEquals(Action.CONTINUE, action);

        // Call callOnRequestBody
        //        var action = httpContext.callOnRequestBody("[original
        // body]".getBytes(StandardCharsets.UTF_8), false /* end of stream */);

    }
}
