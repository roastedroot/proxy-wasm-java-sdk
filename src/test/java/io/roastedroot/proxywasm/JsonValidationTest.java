package io.roastedroot.proxywasm;

import static io.roastedroot.proxywasm.v1.Helpers.bytes;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.Action;
import io.roastedroot.proxywasm.v1.HttpContext;
import io.roastedroot.proxywasm.v1.ProxyWasm;
import io.roastedroot.proxywasm.v1.StartException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/ab4161dcf9246a828008b539a82a1556cf0f2e24/examples/json_validation/main_test.go
 */
public class JsonValidationTest {
    private final MockHandler handler = new MockHandler();

    @Nested
    class OnHttpRequestHeaders {
        private ProxyWasm host;
        private HttpContext context;

        @BeforeEach
        void setUp() throws StartException {
            var module = Parser.parse(Path.of("./src/test/go-examples/json_validation/main.wasm"));
            this.host = ProxyWasm.builder().build(module);
            this.context = host.createHttpContext(handler);
        }

        @AfterEach
        void tearDown() {
            context.close();
            host.close();
        }

        @Test
        void testFailsDueToUnsupportedContentType() {
            var contentType = "application/json";
            var expectedAction = Action.CONTINUE;

            handler.setHttpRequestHeaders(Map.of("content-type", contentType));
            var action = context.callOnRequestHeaders(false);
            assertEquals(expectedAction, action);
        }

        @Test
        void successForJson() {
            var contentType = "application/json";
            var expectedAction = Action.CONTINUE;

            handler.setHttpRequestHeaders(Map.of("content-type", contentType));
            var action = context.callOnRequestHeaders(false);
            assertEquals(expectedAction, action);
        }
    }

    @Nested
    class OnHttpRequestBody {
        private ProxyWasm host;
        private HttpContext context;

        @BeforeEach
        void setUp() throws StartException {
            var config = "{\"requiredKeys\": [\"my_key\"]}";
            var module = Parser.parse(Path.of("./src/test/go-examples/json_validation/main.wasm"));
            this.host = ProxyWasm.builder().withPluginConfig(config).build(module);
            this.context = host.createHttpContext(handler);
        }

        @AfterEach
        void tearDown() {
            context.close();
            host.close();
        }

        @Test
        public void pausesDueToInvalidPayload() {
            String body = "invalid_payload";
            Action expectedAction = Action.PAUSE;

            handler.setHttpRequestBody(bytes(body));
            var action = context.callOnRequestBody(true);
            assertEquals(expectedAction, action);
        }

        @Test
        public void pausesDueToUnknownKeys() {
            String body = "{\"unknown_key\":\"unknown_value\"}";
            Action expectedAction = Action.PAUSE;

            handler.setHttpRequestBody(bytes(body));
            var action = context.callOnRequestBody(true);
            assertEquals(expectedAction, action);
        }

        @Test
        public void success() {
            String body = "{\"my_key\":\"my_value\"}";
            Action expectedAction = Action.CONTINUE;

            handler.setHttpRequestBody(bytes(body));
            var action = context.callOnRequestBody(true);
            assertEquals(expectedAction, action);
        }
    }
}
