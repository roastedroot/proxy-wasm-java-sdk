package io.roastedroot.proxywasm.jaxrs.example.tests;

import static org.hamcrest.Matchers.equalTo;

import io.roastedroot.proxywasm.StartException;
import org.junit.jupiter.api.Test;

public class HttpCallTest extends BaseTest {

    @Test
    public void test() throws InterruptedException, StartException {
        // the wasm plugin will forward the request to the /ok endpoint
        given().header("test", "ok")
                .when()
                .get("/httpCallTests")
                .then()
                .statusCode(200)
                .body(equalTo("ok"))
                .header("echo-test", "ok");
    }

    @Test
    public void httpCallTestsAndFFI() throws InterruptedException {
        given().header("test", "ok")
                .when()
                .get("/httpCallTestsAndFFI")
                .then()
                .statusCode(200)
                .body(equalTo("ko"))
                .header("echo-test", "ok");
    }
}
