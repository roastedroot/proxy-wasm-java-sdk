package io.roastedroot.proxywasm.examples;

import static io.roastedroot.proxywasm.Helpers.bytes;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.MetricType;
import io.roastedroot.proxywasm.NetworkContext;
import io.roastedroot.proxywasm.ProxyWasm;
import io.roastedroot.proxywasm.StartException;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/ab4161dcf9246a828008b539a82a1556cf0f2e24/examples/network/main_test.go
 */
public class NetworkTest {
    private final MockHandler handler = new MockHandler();
    private ProxyWasm host;
    private NetworkContext context;

    @BeforeEach
    void setUp() throws StartException {
        var module = Parser.parse(Path.of("./src/test/go-examples/network/main.wasm"));
        this.host = ProxyWasm.builder().withPluginHandler(handler).build(module);
        this.context = host.createNetworkContext(handler);
    }

    @AfterEach
    void tearDown() {
        context.close();
        host.close();
    }

    @Test
    public void testNetworkOnNewConnection() throws StartException {
        Action action = context.callOnNewConnection();

        // Verify the action is CONTINUE
        assertEquals(Action.CONTINUE, action, "Expected CONTINUE action for new connection");

        // Check logs for expected message
        handler.assertLogsContain("new connection!");
    }

    @Test
    public void testNetworkOnDownstreamClose() throws StartException {
        Action action = context.callOnNewConnection();
        assertEquals(Action.CONTINUE, action, "Expected CONTINUE action for new connection");

        // Call onDownstreamClose
        context.callOnDownstreamConnectionClose();

        // Check logs for expected message
        handler.assertLogsContain("downstream connection close!");
    }

    @Test
    public void testNetworkOnDownstreamData() throws StartException {
        Action action = context.callOnNewConnection();
        assertEquals(Action.CONTINUE, action, "Expected CONTINUE action for new connection");

        // Call onDownstreamData with test message
        String msg = "this is downstream data";
        byte[] data = bytes(msg);
        handler.setDownStreamData(data);
        context.callOnDownstreamData(false); // false = not end of stream

        // Check logs for expected message
        handler.assertLogsContain(">>>>>> downstream data received >>>>>>\n" + msg);
    }

    @Test
    public void testNetworkOnUpstreamData() throws StartException {
        Action action = context.callOnNewConnection();
        assertEquals(Action.CONTINUE, action, "Expected CONTINUE action for new connection");

        // Call onUpstreamData with test message
        String msg = "this is upstream data";
        byte[] data = bytes(msg);
        handler.setUpstreamData(data);
        context.callOnUpstreamData(false); // false = not end of stream

        // Check logs for expected message
        handler.assertLogsContain("<<<<<< upstream data received <<<<<<\n" + msg);
    }

    @Test
    public void testNetworkCounter() throws StartException {
        Action action = context.callOnNewConnection();
        assertEquals(Action.CONTINUE, action, "Expected CONTINUE action for new connection");

        // Complete the connection
        context.close();

        // Check logs for expected message
        handler.assertLogsContain("connection complete!");

        // Check counter metric
        String metricName = "proxy_wasm_go.connection_counter";
        MockHandler.Metric metric = handler.getMetric(metricName);
        Assertions.assertEquals(MetricType.COUNTER, metric.type, "Expected metric to be a counter");
        assertEquals(1, metric.value, "Expected connection counter to be 1");
    }
}
