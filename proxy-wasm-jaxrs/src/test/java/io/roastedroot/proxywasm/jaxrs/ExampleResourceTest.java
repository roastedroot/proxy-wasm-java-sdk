package io.roastedroot.proxywasm.jaxrs;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ExampleResourceTest {

    @Test
    public void testFooBar() {
        given().when().get("/example/foo").then().statusCode(200);
        given().when().get("/example/bar").then().statusCode(200);
    }
}
