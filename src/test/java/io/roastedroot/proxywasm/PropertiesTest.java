package io.roastedroot.proxywasm;

import static io.roastedroot.proxywasm.v1.Helpers.append;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.dylibso.chicory.wasm.Parser;
import io.roastedroot.proxywasm.v1.Action;
import io.roastedroot.proxywasm.v1.ProxyWasm;
import io.roastedroot.proxywasm.v1.StartException;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PropertiesTest {

    static String[] propertyPrefix =
            new String[] {"route_metadata", "filter_metadata", "envoy.filters.http.wasm"};

    private MockHandler handler;
    private ProxyWasm proxyWasm;

    @BeforeEach
    void setUp() throws StartException {
        this.handler = new MockHandler();
        ProxyWasm.Builder builder = ProxyWasm.builder();
        var module = Parser.parse(Path.of("./src/test/go-examples/properties/main.wasm"));
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
            assertEquals(Action.CONTINUE, action);
        }

        handler.assertLogsEqual("no auth header for route", String.format("%d finished", id));
    }

    @Test
    public void userIsAuthenticated() {

        var path = "auth";
        var data = "cookie";
        proxyWasm.setProperty(append(propertyPrefix, path), data);
        var actualData = proxyWasm.getProperty(append(propertyPrefix, path));
        assertEquals(data, actualData);

        int id = 0;
        try (var host = proxyWasm.createHttpContext(handler)) {
            id = host.id();
            handler.setHttpRequestHeader(Map.of("cookie", "value"));
            var action = host.callOnRequestHeaders(false);
            assertEquals(Action.CONTINUE, action);
        }

        handler.assertLogsEqual(
                String.format("auth header is \"%s\"", data), String.format("%d finished", id));
    }

    @Test
    public void userIsUnauthenticated() {

        var path = "auth";
        var data = "cookie";
        proxyWasm.setProperty(append(propertyPrefix, path), data);
        var actualData = proxyWasm.getProperty(append(propertyPrefix, path));
        assertEquals(data, actualData);

        try (var host = proxyWasm.createHttpContext(handler)) {
            var action = host.callOnRequestHeaders(false);
            assertEquals(Action.PAUSE, action);
        }

        var response = handler.getSenthttpResponse();
        assertNotNull(response);
        assertEquals(401, response.statusCode);
        assertArrayEquals(new byte[0], response.body);
    }
}
