package io.roastedroot.proxywasm.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.MetricType;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.internal.Action;
import io.roastedroot.proxywasm.internal.ProxyWasm;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/ab4161dcf9246a828008b539a82a1556cf0f2e24/examples/metrics/main_test.go
 */
public class MetricsTest {
    private static final WasmModule module =
            Parser.parse(Path.of("./src/test/go-examples/metrics/main.wasm"));

    @Test
    public void testMetric() throws StartException {
        var handler = new MockHandler();
        ProxyWasm.Builder builder = ProxyWasm.builder().withPluginHandler(handler);
        try (var host = builder.build(module)) {
            try (var context = host.createHttpContext(handler)) {
                // Create headers with custom header
                Map<String, String> headers = Map.of("my-custom-header", "foo");

                // Call OnRequestHeaders multiple times
                long expectedCount = 3;
                for (int i = 0; i < expectedCount; i++) {
                    handler.setHttpRequestHeaders(headers);
                    Action action = context.callOnRequestHeaders(false);
                    assertEquals(Action.CONTINUE, action);
                }

                // Check metrics
                var metric =
                        handler.getMetric(
                                "custom_header_value_counts_value=foo_reporter=wasmgosdk");
                assertNotNull(metric);
                Assertions.assertEquals(MetricType.COUNTER, metric.type);
                assertEquals(expectedCount, metric.value);
            }
        }
    }
}
