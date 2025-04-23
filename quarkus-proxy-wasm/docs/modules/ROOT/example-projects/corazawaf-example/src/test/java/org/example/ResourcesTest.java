package org.example;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

// Derived from:
// https://github.com/corazawaf/coraza-proxy-wasm?tab=readme-ov-file#manual-requests
@QuarkusTest
public class ResourcesTest {

    // # True positive requests:

    @Test
    public void testPhase1() throws InterruptedException {

        // # Custom rule phase 1
        // curl -I 'http://localhost:8080/admin'
        given().when().get("/admin").then().statusCode(403);
    }

    @Test
    //    @Disabled("not yet working.")
    public void testPhase2() throws InterruptedException {

        // # Custom rule phase 2
        // curl -i -X POST 'http://localhost:8080/anything' --data "maliciouspayload"
        given().header("Content-Type", "application/x-www-form-urlencoded")
                .body("maliciouspayload")
                .when()
                .post("/anything")
                .then()
                .statusCode(403);
    }

    @Test
    public void testPhase3() throws InterruptedException {
        // # Custom rule phase 3
        // curl -I 'http://localhost:8080/status/406'
        given().when().get("/status/406").then().statusCode(403);
    }

    @Test
    @Disabled(
            "Seems like coraza is not loading the reponse body. it logs: 'Skipping response body"
                    + " processing tx_id=\"xxxx\" response_body_access=true'.")
    public void testPhase4() throws InterruptedException {

        // # Custom rule phase 4
        // curl -i -X POST 'http://localhost:8080/anything' --data "responsebodycode"
        given().header("Content-Type", "application/x-www-form-urlencoded")
                .body("responsebodycode")
                .when()
                .post("/anything")
                .then()
                .statusCode(403);
    }

    @Test
    public void testXssPhase1() throws InterruptedException {
        // # XSS phase 1
        // curl -I 'http://localhost:8080/anything?arg=<script>alert(0)</script>'
        given().when().get("/anything?arg=<script>alert(0)</script>").then().statusCode(403);
    }

    @Test
    public void testSQLIPhase2() throws InterruptedException {

        // # SQLI phase 2 (reading the body request)
        // curl -i -X POST 'http://localhost:8080/anything' --data "1%27%20ORDER%20BY%203--%2B"
        given().header("Content-Type", "application/x-www-form-urlencoded")
                .body("1%27%20ORDER%20BY%203--%2B")
                .when()
                .post("/anything")
                .then()
                .statusCode(403);
    }

    @Test
    public void testCRSScannerDetectionRule() throws InterruptedException {
        // # Triggers a CRS scanner detection rule (913100)
        // curl -I --user-agent "zgrab/0.1 (X11; U; Linux i686; en-US; rv:1.7)"
        // -H "Host: localhost"
        // -H "Accept:
        // text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"
        //  localhost:8080
        given().when()
                .header("User-Agent", "zgrab/0.1 (X11; U; Linux i686; en-US; rv:1.7)")
                .header("Host", "localhost")
                .header(
                        "Accept",
                        "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5")
                .get("/anything")
                .then()
                .statusCode(403);
    }

    @Test
    public void testNegativeRequest1() throws InterruptedException {
        // # True negative requests:
        // # A GET request with a harmless argument
        // curl -I 'http://localhost:8080/anything?arg=arg_1'
        given().when().get("/anything?arg=arg_1").then().statusCode(200);
    }

    @Test
    public void testNegativeRequest2() throws InterruptedException {
        // # A payload (reading the body request)
        // curl -i -X POST 'http://localhost:8080/anything' --data "This is a payload"
        given().header("Content-Type", "application/x-www-form-urlencoded")
                .body("This is a payload")
                .when()
                .post("/anything")
                .then()
                .statusCode(200);
    }

    @Test
    public void testNegativeRequest3() throws InterruptedException {

        // # An harmless response body
        // curl -i -X POST 'http://localhost:8080/anything' --data "Hello world"
        given().header("Content-Type", "application/x-www-form-urlencoded")
                .body("Hello world")
                .when()
                .post("/anything")
                .then()
                .statusCode(200);
    }

    @Test
    public void testNegativeRequest4() throws InterruptedException {

        // # An usual user-agent
        // curl -I --user-agent "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like
        // Gecko) Chrome/105.0.0.0 Safari/537.36" localhost:8080
        given().when()
                .header(
                        "User-Agent",
                        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko)"
                                + " Chrome/105.0.0.0 Safari/537.36")
                .get("/anything")
                .then()
                .statusCode(200);
    }
}
