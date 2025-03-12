package io.roastedroot.proxywasm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.ProxyWasm;
import io.roastedroot.proxywasm.v1.StartException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class VmPluginConfigurationTest {

    @Test
    public void test() throws StartException {

        // This module uses the 0_2_0 ABI
        var module =
                Parser.parse(Path.of("./src/test/go-examples/vm_plugin_configuration/main.wasm"));

        var handler = new MockHandler();
        ProxyWasm.Builder builder =
                ProxyWasm.builder()
                        .withPluginConfig("plugin_config")
                        .withVmConfig("vm_config")
                        .withProperties(Map.of("plugin_name", "vm_plugin_configuration"))
                        .withPluginHandler(handler);
        try (var ignored = builder.build(module)) {

            assertEquals(
                    List.of("vm config: vm_config", "plugin config: plugin_config"),
                    handler.loggedMessages());
        }
    }
}
