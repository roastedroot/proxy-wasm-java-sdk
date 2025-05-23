package io.roastedroot.proxywasm.jaxrs.example.tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

/**
 * This test verifies that the plugin can modify request and response headers.
 * <p>
 * It also verifies that the plugin state is shared between requests or can be isolated using configuration.
 */
public class HeadersTest extends BaseTest {

    @Test
    public void testShared() {
        given().when()
                .get("/headerTests")
                .then()
                .statusCode(200)
                .header("x-response-counter", "1")
                .body(equalTo("counter: 1"));

        given().when()
                .get("/headerTests")
                .then()
                .statusCode(200)
                .header("x-response-counter", "2")
                .body(equalTo("counter: 2"));
    }

    @Test
    public void testNotShared() {
        given().when()
                .get("/headerTestsNotShared")
                .then()
                .statusCode(200)
                .header("x-response-counter", "1")
                .body(equalTo("counter: 1"));

        given().when()
                .get("/headerTestsNotShared")
                .then()
                .statusCode(200)
                .header("x-response-counter", "1")
                .body(equalTo("counter: 1"));
    }
}
