package io.roastedroot.proxywasm.examples;

import static io.roastedroot.proxywasm.Helpers.bytes;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.ProxyWasm;
import io.roastedroot.proxywasm.StartException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/ab4161dcf9246a828008b539a82a1556cf0f2e24/examples/vm_plugin_configuration/main_test.go
 */
public class VmPluginConfigurationTest {
    // This module uses the 0_2_0 ABI
    private static final WasmModule module =
            Parser.parse(Path.of("./src/test/go-examples/vm_plugin_configuration/main.wasm"));

    @Test
    public void test() throws StartException {
        var handler = new MockHandler();
        handler.setPluginConfig("plugin_config");
        handler.setVmConfig("vm_config");

        handler.setProperty(List.of("plugin_name"), bytes("vm_plugin_configuration"));
        ProxyWasm.Builder builder = ProxyWasm.builder().withPluginHandler(handler);
        try (var ignored = builder.build(module)) {

            assertEquals(
                    List.of("vm config: vm_config", "plugin config: plugin_config"),
                    handler.loggedMessages());
        }
    }
}
