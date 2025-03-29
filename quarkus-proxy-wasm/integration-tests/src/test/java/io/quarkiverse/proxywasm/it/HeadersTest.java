package io.quarkiverse.proxywasm.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

/**
 * This test verifies that the plugin can modify request and response headers.
 * <p>
 * It also verifies that the plugin state is shared between requests or can be isolated using configuration.
 */
@QuarkusTest
public class HeadersTest {

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
