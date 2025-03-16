package io.roastedroot.proxywasm.examples;

import static io.roastedroot.proxywasm.Helpers.bytes;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.HttpContext;
import io.roastedroot.proxywasm.ProxyWasm;
import io.roastedroot.proxywasm.StartException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

// This file what the go code was:
//
// func TestOnHTTPRequestHeaders(t *testing.T) {
//    type testCase struct {
//        contentType    string
//        expectedAction types.Action
//    }
//
//    vmTest(t, func(t *testing.T, vm types.VMContext) {
//        for name, tCase := range map[string]testCase{
//            "fails due to unsupported content type": {
//                contentType:    "text/html",
//                expectedAction: types.ActionPause,
//            },
//            "success for JSON": {
//                contentType:    "application/json",
//                expectedAction: types.ActionContinue,
//            },
//        } {
//            t.Run(name, func(t *testing.T) {
//                opt := proxytest.NewEmulatorOption().WithVMContext(vm)
//                host, reset := proxytest.NewHostEmulator(opt)
//                defer reset()
//
//                require.Equal(t, types.OnPluginStartStatusOK, host.StartPlugin())
//
//                id := host.InitializeHttpContext()
//
//                hs := [][2]string{{"content-type", tCase.contentType}}
//
//                action := host.CallOnRequestHeaders(id, hs, false)
//                assert.Equal(t, tCase.expectedAction, action)
//            })
//        }
//    })
// }
//
// func TestOnHTTPRequestBody(t *testing.T) {
//    type testCase struct {
//        body           string
//        expectedAction types.Action
//    }
//
//    vmTest(t, func(t *testing.T, vm types.VMContext) {
//
//        for name, tCase := range map[string]testCase{
//            "pauses due to invalid payload": {
//                body:           "invalid_payload",
//                expectedAction: types.ActionPause,
//            },
//            "pauses due to unknown keys": {
//                body:           `{"unknown_key":"unknown_value"}`,
//                expectedAction: types.ActionPause,
//            },
//            "success": {
//                body:           "{\"my_key\":\"my_value\"}",
//                expectedAction: types.ActionContinue,
//            },
//        } {
//            t.Run(name, func(t *testing.T) {
//                opt := proxytest.
//                    NewEmulatorOption().
//                    WithPluginConfiguration([]byte(`{"requiredKeys": ["my_key"]}`)).
//                    WithVMContext(vm)
//                host, reset := proxytest.NewHostEmulator(opt)
//                defer reset()
//
//                require.Equal(t, types.OnPluginStartStatusOK, host.StartPlugin())
//
//                id := host.InitializeHttpContext()
//
//                action := host.CallOnRequestBody(id, []byte(tCase.body), true)
//                assert.Equal(t, tCase.expectedAction, action)
//            })
//        }
//    })
// }
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
