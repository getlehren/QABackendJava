package tests;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class AccountTest extends BaseTest {
    String ACCOUNT_GET_URL = "/account/{username}";

    @Test
    void getAccountInfoTest() {
        given()
                .spec(requestSpecificationWithAuth)
                .when()
                .get(ACCOUNT_GET_URL, username)
                .then()
                .spec(positiveResponseSpecification);
    }

    @Test
    void getAccountInfoWithLogingTest() {
        given()
                .spec(requestSpecificationWithAuth)
                .log()
                .method()
                .log()
                .uri()
                .when()
                .get(ACCOUNT_GET_URL, username)
                .then()
                .spec(positiveResponseSpecification);
    }
}
