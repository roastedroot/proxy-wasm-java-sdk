package io.quarkiverse.proxywasm.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FFITest {

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
