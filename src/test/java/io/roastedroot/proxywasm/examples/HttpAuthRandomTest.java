package io.roastedroot.proxywasm.examples;

import static io.roastedroot.proxywasm.Helpers.bytes;
import static io.roastedroot.proxywasm.Helpers.string;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.ProxyWasm;
import io.roastedroot.proxywasm.StartException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/ab4161dcf9246a828008b539a82a1556cf0f2e24/examples/http_auth_random/main_test.go
 */
public class HttpAuthRandomTest {
    private static WasmModule module =
            Parser.parse(Path.of("./src/test/go-examples/http_auth_random/main.wasm"));

    private static String clusterName = "httpbin";

    private MockHandler handler;
    private ProxyWasm host;

    @BeforeEach
    void setUp() throws StartException {
        this.handler = new MockHandler();
        ProxyWasm.Builder builder = ProxyWasm.builder();
        this.host = builder.build(module);
    }

    @AfterEach
    void tearDown() {
        host.close();
    }

    @Test
    public void onHttpRequestHeaders() throws StartException {
        try (var context = host.createHttpContext(handler)) {

            // Call OnRequestHeaders.
            handler.setHttpRequestHeaders(Map.of("key", "value"));
            var action = context.callOnRequestHeaders(false);
            Assertions.assertEquals(Action.PAUSE, action);

            // Verify DispatchHttpCall is called.
            var calls = handler.getHttpCalls();
            assertEquals(1, calls.size());
            MockHandler.HttpCall call = calls.values().stream().findFirst().get();
            assertEquals(clusterName, call.uri);

            // Check Envoy logs.
            handler.assertLogsContain(
                    "http call dispatched to " + clusterName, "request header: key: value");
        }
    }

    @Test
    public void onHttpCallResponse() throws StartException {
        var headers =
                Map.of(
                        "HTTP/1.1", "200 OK",
                        "Date:", "Thu, 17 Sep 2020 02:47:07 GMT",
                        "Content-Type", "application/json",
                        "Content-Length", "53",
                        "Connection", "keep-alive",
                        "Server", "gunicorn/19.9.0",
                        "Access-Control-Allow-Origin", "*",
                        "Access-Control-Allow-Credentials", "true");

        // Access granted case -> Local response must not be sent.
        try (var context = host.createHttpContext(handler)) {

            // Call OnRequestHeaders.
            handler.setHttpRequestHeaders(Map.of());
            var action = context.callOnRequestHeaders(false);
            assertEquals(Action.PAUSE, action);

            // Verify DispatchHttpCall is called.
            var calls = handler.getHttpCalls();
            assertEquals(1, calls.size());
            var body = bytes("\"uuid\": \"7b10a67a-1c67-4199-835b-cbefcd4a63d4\"");
            MockHandler.HttpCall call = calls.values().stream().findFirst().get();
            host.sendHttpCallResponse(call.id, headers, null, body);
            calls.remove(call.id);

            // Check local response.
            assertNull(handler.getSentHttpResponse());

            // CHeck Envoy logs.
            handler.assertLogsContain("access granted");
        }

        // Access denied case -> Local response must be sent.
        try (var context = host.createHttpContext(handler)) {

            // Call OnRequestHeaders.
            handler.setHttpRequestHeaders(Map.of());
            var action = context.callOnRequestHeaders(false);
            assertEquals(Action.PAUSE, action);

            // Verify DispatchHttpCall is called.
            var calls = handler.getHttpCalls();
            assertEquals(1, calls.size());
            var body = bytes("\"uuid\": \"aaaaaaaa-1c67-4199-835b-cbefcd4a63d4\"");
            MockHandler.HttpCall call = calls.values().stream().findFirst().get();
            host.sendHttpCallResponse(call.id, headers, null, body);
            // Check local response.
            MockHandler.HttpResponse localResponse = handler.getSentHttpResponse();
            assertNotNull(localResponse);
            assertEquals(403, localResponse.statusCode);
            assertEquals("access forbidden", string(localResponse.body));
            assertEquals(Map.of("powered-by", "proxy-wasm-go-sdk!!"), localResponse.headers);

            // CHeck Envoy logs.
            handler.assertLogsContain("access forbidden");
        }
    }
}
