package io.roastedroot.proxywasm.examples;

import static io.roastedroot.proxywasm.internal.Helpers.bytes;
import static io.roastedroot.proxywasm.internal.Helpers.string;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.internal.ProxyMap;
import io.roastedroot.proxywasm.internal.ProxyWasm;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Test loading https://github.com/proxy-wasm/proxy-wasm-rust-sdk/tree/c8b2335df66a569a6306c58e346dd0cf9dbc0f3a/examples/envoy_filter_metadata
 */
public class RustEnvoyFilterMetadataTest {
    private static final WasmModule module =
            Parser.parse(Path.of("./src/test/rust-examples/envoy_filter_metadata/main.wasm"));

    @Test
    public void test() throws StartException {
        var handler = new MockHandler();
        try (var host = ProxyWasm.builder().withPluginHandler(handler).build(module)) {

            String value = "SOME-VALUE";
            handler.setProperty(
                    List.of(
                            "metadata",
                            "filter_metadata",
                            "envoy.filters.http.lua",
                            "uppercased-custom-metadata"),
                    bytes(value));
            try (var context = host.createHttpContext(handler)) {
                context.callOnRequestHeaders(true);
                MockHandler.HttpResponse response = handler.getSentHttpResponse();
                assertNotNull(response);
                assertEquals(200, response.statusCode);
                assertEquals(
                        ProxyMap.of("Powered-By", "proxy-wasm", "uppercased-metadata", value),
                        response.headers);
                assertEquals(
                        String.format("Custom response with Envoy metadata: \"%s\"\n", value),
                        string(response.body));
            }
        }
    }
}
