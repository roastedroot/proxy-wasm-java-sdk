package io.quarkiverse.proxywasm.it;

import static io.quarkiverse.proxywasm.it.Helpers.isTrue;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TickTest {

    @Test
    public void tick() throws InterruptedException {

        // plugin should not have received any ticks.
        given().when().get("/tickTests/get").then().statusCode(200).body(equalTo("0"));

        // ask the plugin to enable tick events..
        given().when().get("/tickTests/enable").then().statusCode(200).body(equalTo("ok"));

        // wait a little to allow the plugin to receive some ticks. (every 100 ms)
        Thread.sleep(300);

        // stop getting ticks.
        given().when().get("/tickTests/disable").then().statusCode(200).body(equalTo("ok"));

        var counter = new String[] {"0"};

        // plugin should have received at least 1 tick.
        given().when()
                .get("/tickTests/get")
                .then()
                .statusCode(200)
                .body(
                        isTrue(
                                (String x) -> {
                                    counter[0] = x;
                                    return Integer.parseInt(x) >= 1;
                                }));

        // since ticks were disabled the tick counter should not have changed.
        Thread.sleep(300);

        given().when().get("/tickTests/get").then().statusCode(200).body(equalTo(counter[0]));
    }
}
