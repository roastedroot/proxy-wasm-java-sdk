package io.roastedroot.proxywasm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.ProxyWasm;
import io.roastedroot.proxywasm.v1.StartException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/ab4161dcf9246a828008b539a82a1556cf0f2e24/examples/foreign_call_on_tick/main_test.go
 */
public class ForeignCallOnTickTest {

    static int tickMilliseconds = 1;

    @Test
    public void testOnTick() throws StartException {

        var handler = new MockHandler();
        var module = Parser.parse(Path.of("./src/test/go-examples/foreign_call_on_tick/main.wasm"));
        ProxyWasm.Builder builder = ProxyWasm.builder().withPluginHandler(handler);
        try (var host = builder.build(module)) {
            assertEquals(tickMilliseconds, handler.getTickPeriodMilliseconds());

            host.registerForeignFunction("compress", data -> data);

            for (int i = 1; i <= 10; i++) {
                host.tick(); // call OnTick
                handler.assertLogsContain(
                        String.format(
                                "foreign function (compress) called: %d, result: %s",
                                i, "68656c6c6f20776f726c6421"));
            }
        }
    }
}
