package io.roastedroot.proxywasm.jaxrs.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class HttpHeadersNotSharedTest {

    @Test
    public void testRequest() {

        // since the plugin is not shared, the counter should not increment since each request gets
        // a new plugin instance.
        given().when()
                .get("/test/notSharedHttpHeaders")
                .then()
                .statusCode(200)
                .body(equalTo("hello world"))
                .header("x-proxy-wasm-counter", "1");

        given().when()
                .get("/test/notSharedHttpHeaders")
                .then()
                .statusCode(200)
                .body(equalTo("hello world"))
                .header("x-proxy-wasm-counter", "1");
    }
}
