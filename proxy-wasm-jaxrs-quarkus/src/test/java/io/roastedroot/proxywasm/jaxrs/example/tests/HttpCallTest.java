package io.roastedroot.proxywasm.jaxrs.example.tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.roastedroot.proxywasm.StartException;
import io.roastedroot.proxywasm.jaxrs.WasmPluginFeature;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class HttpCallTest {

    @Inject WasmPluginFeature feature;

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
}
