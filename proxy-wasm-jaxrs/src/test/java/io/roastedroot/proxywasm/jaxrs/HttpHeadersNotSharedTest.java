package io.roastedroot.proxywasm.jaxrs;

import static io.restassured.RestAssured.given;
import static io.roastedroot.proxywasm.jaxrs.TestHelpers.parseTestModule;

import io.quarkus.test.junit.QuarkusTest;
import io.roastedroot.proxywasm.StartException;
import jakarta.enterprise.inject.Produces;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class HttpHeadersNotSharedTest {

    @Produces
    public WasmPluginFactory create() throws StartException {
        return () ->
                WasmPlugin.builder()
                        .withName("notSharedHttpHeaders")
                        .withShared(false)
                        .withPluginConfig("{\"header\": \"x-wasm-header\", \"value\": \"foo\"}")
                        .build(parseTestModule("/go-examples/http_headers/main.wasm"));
    }

    @Test
    public void testRequest() {

        // since the plugin is not shared, the counter should not increment since each request gets
        // a new plugin instance.
        given().when()
                .get("/test/notSharedHttpHeaders")
                .then()
                .statusCode(200)
                .header("x-proxy-wasm-counter", "1");

        given().when()
                .get("/test/notSharedHttpHeaders")
                .then()
                .statusCode(200)
                .header("x-proxy-wasm-counter", "1");
    }
}
