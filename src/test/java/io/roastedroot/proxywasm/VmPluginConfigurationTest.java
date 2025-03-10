package io.roastedroot.proxywasm;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.*;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class VmPluginConfigurationTest {

    @Test
    public void test() throws StartException {

        // This module uses the 0_2_0 ABI
        var module = Parser.parse(Path.of("./src/test/go-examples/vm_plugin_configuration/main.wasm"));

        ArrayList<String> loggedMessages = new ArrayList<>();

        ProxyWasm.Builder builder = ProxyWasm.builder()
                .withPluginConfig("plugin_config".getBytes(UTF_8))
                .withVmConfig("vm_config".getBytes(UTF_8))
                .withProperties(Map.of(
                        "plugin_name", "vm_plugin_configuration"
                ))
                .withVmHandler(new DefaultHandler() {
                    @Override
                    public void log(LogLevel level, String message) throws WasmException {
                        loggedMessages.add(message);
                    }
                });
        try (var ignored = builder.build(module)) {

                assertEquals(List.of(
                        "vm config: vm_config",
                        "plugin config: plugin_config"
                ), loggedMessages);

        }

    }
}
