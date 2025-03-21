package io.roastedroot.proxywasm.jaxrs.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class HttpHeadersTest {

    @Test
    public void testRequest() {
        given().when()
                .get("/test/httpHeaders")
                .then()
                .statusCode(200)
                .header("x-proxy-wasm-go-sdk-example", "http_headers")
                .header("x-wasm-header", "foo")
                .header("x-proxy-wasm-counter", "1")
                .body(equalTo("hello world"));

        given().when()
                .get("/test/httpHeaders")
                .then()
                .statusCode(200)
                .header("x-proxy-wasm-go-sdk-example", "http_headers")
                .header("x-wasm-header", "foo")
                .header("x-proxy-wasm-counter", "2")
                .body(equalTo("hello world"));
    }
}
