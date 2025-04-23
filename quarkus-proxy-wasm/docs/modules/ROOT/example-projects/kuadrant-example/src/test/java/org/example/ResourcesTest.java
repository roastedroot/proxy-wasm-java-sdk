package org.example;

import static io.restassured.RestAssured.given;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(value = LimitadorTestContainer.class)
public class ResourcesTest {

    @ConfigProperty(name = "limitador.http.url")
    String limitadorUrl;

    @Test
    public void testCountersRemaining() throws InterruptedException {

        var remaining = 30;
        while (remaining > 0) {
            given().header("Host", "test.example.com").when().get("/").then().statusCode(200);
            remaining -= 1;
            given().when()
                    .get(limitadorUrl + "/counters/basic")
                    .then()
                    .statusCode(200)
                    .body(
                            sameJSONAs(String.format("[{\"remaining\":%s}]", remaining))
                                    .allowingExtraUnexpectedFields());
        }

        given().header("Host", "test.example.com").when().get("/").then().statusCode(429);
        given().when()
                .get(limitadorUrl + "/counters/basic")
                .then()
                .statusCode(200)
                .body(
                        sameJSONAs(String.format("[{\"remaining\":%s}]", 0))
                                .allowingExtraUnexpectedFields());
    }
}
