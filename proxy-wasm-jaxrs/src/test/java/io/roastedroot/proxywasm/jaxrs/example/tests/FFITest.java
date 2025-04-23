package io.roastedroot.proxywasm.jaxrs.example.tests;

import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

public class FFITest extends BaseTest {

    @Test
    public void reverse() throws InterruptedException {
        given().body("My Test")
                .when()
                .post("/ffiTests/reverse")
                .then()
                .statusCode(200)
                .body(equalTo("tseT yM"));
    }
}
