package io.roastedroot.proxywasm.examples;

import static io.roastedroot.proxywasm.Helpers.bytes;
import static io.roastedroot.proxywasm.Helpers.string;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.HttpContext;
import io.roastedroot.proxywasm.ProxyMap;
import io.roastedroot.proxywasm.ProxyWasm;
import io.roastedroot.proxywasm.StartException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/ab4161dcf9246a828008b539a82a1556cf0f2e24/examples/http_body/main_test.go
 */
public class HttpBodyTest {
    private static final WasmModule module =
            Parser.parse(Path.of("./src/test/go-examples/http_body/main.wasm"));

    private MockHandler handler;
    private ProxyWasm proxyWasm;
    private HttpContext httpContext;

    @BeforeEach
    void setUp() throws StartException {
        this.handler = new MockHandler();
        ProxyWasm.Builder builder = ProxyWasm.builder();
        this.proxyWasm = builder.build(module);
        this.httpContext = proxyWasm.createHttpContext(handler);
    }

    @AfterEach
    void tearDown() {
        httpContext.close();
        proxyWasm.close();
    }

    @Test
    public void testOnHttpRequestHeadersRemoveRequestHeader() throws StartException {

        // Call OnRequestHeaders.
        handler.setHttpRequestHeaders(
                ProxyMap.of(
                        "content-length", "10",
                        "buffer-operation", "replace"));
        var action = httpContext.callOnRequestHeaders(false);

        // Must be continued.
        Assertions.assertEquals(Action.CONTINUE, action);

        var headers = handler.getHttpRequestHeaders();
        assertEquals(ProxyMap.of("buffer-operation", "replace"), headers);
    }

    @Test
    public void testOnHttpRequestHeaders400Response() throws StartException {
        // Call OnRequestHeaders.
        var action = httpContext.callOnRequestHeaders(false);

        // Must be paused.
        assertEquals(Action.PAUSE, action);

        // Check the local response.
        var response = handler.getSentHttpResponse();
        assertNotNull(response);
        assertEquals(400, response.statusCode);
        assertEquals("content must be provided", string(response.body));
    }

    @Test
    public void testOnHttpRequestBodyPauseUntilEOS() throws StartException {
        // Call callOnRequestBody
        handler.setHttpRequestBody(bytes("aaaa"));
        var action = httpContext.callOnRequestBody(false /* end of stream */);

        // Must be paused.
        assertEquals(Action.PAUSE, action);
    }

    @Test
    public void testOnHttpRequestBodyAppend() throws StartException {
        // Call callOnRequestBody
        handler.setHttpRequestHeaders(
                ProxyMap.of(
                        "content-length", "10",
                        "buffer-operation", "append"));

        var action = httpContext.callOnRequestHeaders(false /* end of stream */);

        // Must be continued.
        assertEquals(Action.CONTINUE, action);

        // Call callOnRequestBody
        handler.setHttpRequestBody("[original body]".getBytes(StandardCharsets.UTF_8));
        action = httpContext.callOnRequestBody(true);
        assertEquals(Action.CONTINUE, action);

        handler.assertLogsEqual("original request body: [original body]");
        assertEquals(
                "[original body][this is appended body]", string(handler.getHttpRequestBody()));
    }

    @Test
    public void testOnHttpRequestBodyPrepend() throws StartException {
        // Call callOnRequestBody
        handler.setHttpRequestHeaders(
                ProxyMap.of(
                        "content-length", "10",
                        "buffer-operation", "prepend"));

        var action = httpContext.callOnRequestHeaders(false /* end of stream */);

        // Must be continued.
        assertEquals(Action.CONTINUE, action);

        // Call callOnRequestBody
        handler.setHttpRequestBody("[original body]".getBytes(StandardCharsets.UTF_8));
        action = httpContext.callOnRequestBody(true);
        assertEquals(Action.CONTINUE, action);

        handler.assertLogsEqual("original request body: [original body]");
        assertEquals(
                "[this is prepended body][original body]", string(handler.getHttpRequestBody()));
    }

    @Test
    public void testOnHttpRequestBodyReplace() throws StartException {
        // Call callOnRequestBody
        handler.setHttpRequestHeaders(
                ProxyMap.of(
                        "content-length", "10",
                        "buffer-operation", "replace"));

        var action = httpContext.callOnRequestHeaders(false /* end of stream */);

        // Must be continued.
        assertEquals(Action.CONTINUE, action);

        // Call callOnRequestBody
        handler.setHttpRequestBody(bytes("[original body]"));
        action = httpContext.callOnRequestBody(true);
        assertEquals(Action.CONTINUE, action);

        handler.assertLogsEqual("original request body: [original body]");
        assertEquals("[this is replaced body]", string(handler.getHttpRequestBody()));
    }

    @Test
    public void testOnHttpResponseBodyAppend() throws StartException {

        // Call OnRequestHeaders
        handler.setHttpRequestHeaders(
                ProxyMap.of(
                        "buffer-replace-at", "response",
                        "content-length", "10",
                        "buffer-operation", "append"));
        var action = httpContext.callOnRequestHeaders(false /* end of stream */);
        assertEquals(Action.CONTINUE, action);

        handler.setHttpResponseBody(bytes("[original body]"));
        action = httpContext.callOnResponseBody(true);
        assertEquals(Action.CONTINUE, action);

        handler.assertLogsEqual("original response body: [original body]");
        assertEquals(
                "[original body][this is appended body]", string(handler.getHttpResponseBody()));
    }

    @Test
    public void testOnHttpResponseBodyPrepend() throws StartException {

        // Call OnRequestHeaders
        handler.setHttpRequestHeaders(
                ProxyMap.of(
                        "buffer-replace-at", "response",
                        "content-length", "10",
                        "buffer-operation", "prepend"));
        var action = httpContext.callOnRequestHeaders(false /* end of stream */);
        assertEquals(Action.CONTINUE, action);

        handler.setHttpResponseBody(bytes("[original body]"));
        action = httpContext.callOnResponseBody(true);
        assertEquals(Action.CONTINUE, action);

        handler.assertLogsEqual("original response body: [original body]");
        assertEquals(
                "[this is prepended body][original body]", string(handler.getHttpResponseBody()));
    }

    @Test
    public void testOnHttpResponseBodyReplace() throws StartException {

        // Call OnRequestHeaders
        handler.setHttpRequestHeaders(
                ProxyMap.of(
                        "buffer-replace-at", "response",
                        "content-length", "10",
                        "buffer-operation", "replace"));
        var action = httpContext.callOnRequestHeaders(false /* end of stream */);
        assertEquals(Action.CONTINUE, action);

        handler.setHttpResponseBody(bytes("[original body]"));
        action = httpContext.callOnResponseBody(true);
        assertEquals(Action.CONTINUE, action);

        handler.assertLogsEqual("original response body: [original body]");
        assertEquals("[this is replaced body]", string(handler.getHttpResponseBody()));
    }
}
