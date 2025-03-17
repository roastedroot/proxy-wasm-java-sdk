package io.roastedroot.proxywasm.examples;

import static io.roastedroot.proxywasm.Helpers.append;
import static io.roastedroot.proxywasm.Helpers.bytes;
import static io.roastedroot.proxywasm.Helpers.string;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dylibso.chicory.wasm.Parser;
import com.dylibso.chicory.wasm.WasmModule;
import io.roastedroot.proxywasm.Action;
import io.roastedroot.proxywasm.ProxyWasm;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.WasmException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Java port of https://github.com/proxy-wasm/proxy-wasm-go-sdk/blob/ab4161dcf9246a828008b539a82a1556cf0f2e24/examples/properties/main_test.go
 */
public class PropertiesTest {
    private static final WasmModule module =
            Parser.parse(Path.of("./src/test/go-examples/properties/main.wasm"));

    static String[] propertyPrefix =
            new String[] {"route_metadata", "filter_metadata", "envoy.filters.http.wasm"};

    private MockHandler handler;
    private ProxyWasm proxyWasm;

    @BeforeEach
    void setUp() throws StartException {
        this.handler = new MockHandler();
        ProxyWasm.Builder builder = ProxyWasm.builder().withPluginHandler(this.handler);
        this.proxyWasm = builder.build(module);
    }

    @AfterEach
    void tearDown() {
        proxyWasm.close();
    }

    @Test
    public void routeIsUnauthenticated() {
        int id = 0;
        try (var host = proxyWasm.createHttpContext(handler)) {
            id = host.id();
            var action = host.callOnRequestHeaders(false);
            Assertions.assertEquals(Action.CONTINUE, action);
        }

        handler.assertLogsEqual("no auth header for route", String.format("%d finished", id));
    }

    @Test
    public void userIsAuthenticated() throws WasmException {

        var path = "auth";
        var data = bytes("cookie");

        List<String> key = Arrays.asList(append(propertyPrefix, path));
        handler.setProperty(key, data);
        var actualData = handler.getProperty(key);
        assertEquals(data, actualData);

        int id = 0;
        try (var host = proxyWasm.createHttpContext(handler)) {
            id = host.id();
            handler.setHttpRequestHeaders(Map.of("cookie", "value"));
            var action = host.callOnRequestHeaders(false);
            assertEquals(Action.CONTINUE, action);
        }

        handler.assertLogsEqual(
                String.format("auth header is \"%s\"", string(data)),
                String.format("%d finished", id));
    }

    @Test
    public void userIsUnauthenticated() throws WasmException {

        var path = "auth";
        var data = bytes("cookie");

        List<String> key = Arrays.asList(append(propertyPrefix, path));

        handler.setProperty(key, data);
        var actualData = handler.getProperty(key);
        assertEquals(data, actualData);

        try (var host = proxyWasm.createHttpContext(handler)) {
            var action = host.callOnRequestHeaders(false);
            assertEquals(Action.PAUSE, action);
        }

        var response = handler.getSentHttpResponse();
        assertNotNull(response);
        assertEquals(401, response.statusCode);
        assertArrayEquals(new byte[0], response.body);
    }
}
