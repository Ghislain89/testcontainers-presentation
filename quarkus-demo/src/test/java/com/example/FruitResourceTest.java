package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Unit integration test: Quarkus boots with REAL PostgreSQL (Dev Services)
 * and REAL Kafka. No mocks — tests the full stack against actual containers.
 *
 * Flyway runs migrations automatically; seed data comes from V2__seed_fruits.sql.
 */
@QuarkusTest
class FruitResourceTest {

    @Test
    void shouldListSeededFruits() {
        given()
            .when().get("/fruits")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(3))
                .body("name", hasItems("Apple", "Banana", "Cherry"));
    }

    @Test
    void shouldCreateAndFetchFruit() {
        int id = given()
            .contentType("application/json")
            .body("{\"name\": \"Mango\", \"description\": \"Tropical and juicy\"}")
            .when().post("/fruits")
            .then()
                .statusCode(201)
                .body("name", is("Mango"))
                .extract().path("id");

        given()
            .when().get("/fruits/" + id)
            .then()
                .statusCode(200)
                .body("name", is("Mango"))
                .body("description", is("Tropical and juicy"));
    }

    @Test
    void shouldDeleteFruit() {
        int id = given()
            .contentType("application/json")
            .body("{\"name\": \"Kiwi\", \"description\": \"Fuzzy green fruit\"}")
            .when().post("/fruits")
            .then()
                .statusCode(201)
                .extract().path("id");

        given()
            .when().delete("/fruits/" + id)
            .then()
                .statusCode(204);

        given()
            .when().get("/fruits/" + id)
            .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn404ForUnknownFruit() {
        given()
            .when().get("/fruits/99999")
            .then()
                .statusCode(404);
    }
}
