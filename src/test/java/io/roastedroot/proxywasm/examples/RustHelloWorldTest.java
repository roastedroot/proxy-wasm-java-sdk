package io.roastedroot.proxywasm.examples;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.ProxyWasm;
import io.roastedroot.proxywasm.StartException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Test loading https://github.com/proxy-wasm/proxy-wasm-rust-sdk/tree/c8b2335df66a569a6306c58e346dd0cf9dbc0f3a/examples/hello_world
 */
public class RustHelloWorldTest {

    @Test
    public void pauseUntilEOS() throws StartException {
        var handler = new MockHandler();
        var module = Parser.parse(Path.of("./src/test/rust-examples/hello_world/main.wasm"));
        try (var host = ProxyWasm.builder().withPluginHandler(handler).build(module)) {

            handler.assertLogsEqual("Hello, World!");
            host.tick();

            assertTrue(handler.loggedMessages().get(1).contains("your lucky number is"));
        }
    }
}
