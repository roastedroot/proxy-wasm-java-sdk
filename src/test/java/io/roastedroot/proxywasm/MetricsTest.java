package io.roastedroot.proxywasm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.Action;
import io.roastedroot.proxywasm.v1.MetricType;
import io.roastedroot.proxywasm.v1.ProxyWasm;
import io.roastedroot.proxywasm.v1.StartException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/ab4161dcf9246a828008b539a82a1556cf0f2e24/examples/metrics/main_test.go
 */
public class MetricsTest {

    @Test
    public void testMetric() throws StartException {
        var handler = new MockHandler();
        var module = Parser.parse(Path.of("./src/test/go-examples/metrics/main.wasm"));
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
                assertEquals(MetricType.COUNTER, metric.type);
                assertEquals(expectedCount, metric.value);
            }
        }
    }
}
