package io.roastedroot.proxywasm;

import static io.roastedroot.proxywasm.v1.Helpers.bytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.Action;
import io.roastedroot.proxywasm.v1.HttpContext;
import io.roastedroot.proxywasm.v1.ProxyWasm;
import io.roastedroot.proxywasm.v1.StartException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HttpBodyChunkTest {

    private MockHandler handler;
    private ProxyWasm proxyWasm;
    private HttpContext host;

    @BeforeEach
    void setUp() throws StartException {
        this.handler = new MockHandler();
        ProxyWasm.Builder builder = ProxyWasm.builder();
        var module = Parser.parse(Path.of("./src/test/go-examples/http_body_chunk/main.wasm"));
        this.proxyWasm = builder.build(module);
        this.host = proxyWasm.createHttpContext(handler);
    }

    @AfterEach
    void tearDown() {
        host.close();
        proxyWasm.close();
    }

    @Test
    public void pauseUntilEOS() {
        var action = host.callOnRequestBody(false);
        assertEquals(Action.PAUSE, action);
    }

    @Test
    public void patternFound() {
        var body = bytes("This is a payload with the pattern word.");
        // Call OnRequestHeaders.
        handler.setHttpRequestHeaders(Map.of("content-length", String.format("%d", body.length)));
        var action = host.callOnRequestHeaders(false);

        // Must be continued.
        assertEquals(Action.CONTINUE, action);

        // Call OnRequestBody.
        handler.setHttpRequestBody(body);
        action = host.callOnRequestBody(true);

        // Must be paused
        assertEquals(Action.PAUSE, action);

        handler.assertLogsContain("pattern found in chunk: 1");

        // Check the local response.
        var response = handler.getSentHttpResponse();
        assertNotNull(response);
        assertEquals(403, response.statusCode);
    }

    @Test
    public void patternFoundInMultipleChunks() {

        var chunks =
                new byte[][] {
                    bytes("chunk1..."),
                    bytes("chunk2..."),
                    bytes("chunk3..."),
                    bytes("chunk4 with pattern ...")
                };

        var chunksSize = 0;
        for (byte[] chunk : chunks) {
            chunksSize += chunk.length;
        }

        // Call OnRequestHeaders.
        handler.setHttpRequestHeaders(Map.of("content-length", String.format("%d", chunksSize)));
        var action = host.callOnRequestHeaders(false);

        // Must be continued.
        assertEquals(Action.CONTINUE, action);

        // Call OnRequestBody.
        for (byte[] chunk : chunks) {
            handler.appendHttpRequestBody(chunk);
            action = host.callOnRequestBody(false);
            // Must be paused.
            assertEquals(Action.PAUSE, action);
        }

        handler.assertLogsContain("pattern found in chunk: 4");
        handler.assertLogsDoNotContain("read data does not match");

        // Check the local response.
        var response = handler.getSentHttpResponse();
        assertNotNull(response);
        assertEquals(403, response.statusCode);
    }

    @Test
    public void patternNotFound() {
        var body = bytes("This is a generic payload.");
        // Call OnRequestHeaders.
        handler.setHttpRequestHeaders(Map.of("content-length", String.format("%d", body.length)));
        var action = host.callOnRequestHeaders(false);

        // Must be continued.
        assertEquals(Action.CONTINUE, action);

        // Call OnRequestBody.
        handler.setHttpRequestBody(body);
        action = host.callOnRequestBody(false);

        // Must be paused
        assertEquals(Action.PAUSE, action);

        // Call OnRequestBody.
        action = host.callOnRequestBody(true);

        // Must be paused
        assertEquals(Action.CONTINUE, action);

        handler.assertLogsContain("pattern not found");
    }
}
