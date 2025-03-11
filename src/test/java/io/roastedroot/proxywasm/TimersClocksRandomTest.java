package io.roastedroot.proxywasm;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class TimersClocksRandomTest {

    @Test
    public void test() throws StartException {

        var loggedMessages = new ArrayList<String>();
        var setTickPeriodMillisecondsCalls = new AtomicInteger(0);

        var handler = new DefaultHandler() {
            @Override
            public void log(LogLevel level, String message) throws WasmException {
                loggedMessages.add(message);
            }

            @Override
            public WasmResult setTickPeriodMilliseconds(int tick_period) {
                setTickPeriodMillisecondsCalls.incrementAndGet();
                return WasmResult.OK;
            }

        };
        ProxyWasm.Builder builder = ProxyWasm.builder()
                .withPluginHandler(handler);

        var module = Parser.parse(Path.of("./src/test/go-examples/helloworld/main.wasm"));
        try (var proxyWasm = builder.build(module)) {

            assertEquals(List.of(
                    "OnPluginStart from Go!"
            ), loggedMessages);
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
