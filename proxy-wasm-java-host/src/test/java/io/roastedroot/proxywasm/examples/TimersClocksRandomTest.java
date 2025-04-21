package io.roastedroot.proxywasm.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.internal.ProxyWasm;
import io.roastedroot.proxywasm.internal.WasmResult;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/ab4161dcf9246a828008b539a82a1556cf0f2e24/examples/helloworld/main_test.go
 */
public class TimersClocksRandomTest {
    private static final WasmModule module =
            Parser.parse(Path.of("./src/test/go-examples/helloworld/main.wasm"));

    @Test
    public void test() throws StartException {

        var setTickPeriodMillisecondsCalls = new AtomicInteger(0);

        var handler =
                new MockHandler() {
                    @Override
                    public WasmResult setTickPeriodMilliseconds(int tick_period) {
                        setTickPeriodMillisecondsCalls.incrementAndGet();
                        return WasmResult.OK;
                    }
                };

        ProxyWasm.Builder builder = ProxyWasm.builder().withPluginHandler(handler);

        try (var proxyWasm = builder.build(module)) {
            var loggedMessages = handler.loggedMessages();
            assertEquals(List.of("OnPluginStart from Go!"), loggedMessages);
            loggedMessages.clear();

            // the example requests tick events in the plugin start
            assertEquals(1, setTickPeriodMillisecondsCalls.get());

            // send it a tick.
            proxyWasm.tick();

            assertEquals(2, loggedMessages.size());

            // This log message will look like: "It's %d: random value: %d"
            assertTrue(loggedMessages.get(0).startsWith("It's "));

            assertEquals("OnTick called", loggedMessages.get(1));
        }
    }
}
