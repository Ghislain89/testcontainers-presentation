package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Demonstrates Quarkus Dev Services: Quarkus automatically starts a PostgreSQL
 * container via Testcontainers when it detects the postgresql JDBC driver on the
 * classpath and no explicit datasource URL is configured. Zero configuration needed!
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
        // Create a new fruit
        int id = given()
            .contentType("application/json")
            .body("{\"name\": \"Mango\", \"description\": \"Tropical and juicy\"}")
            .when().post("/fruits")
            .then()
                .statusCode(201)
                .body("name", is("Mango"))
                .extract().path("id");

        // Fetch it back — data is persisted in the real PostgreSQL container
        given()
            .when().get("/fruits/" + id)
            .then()
                .statusCode(200)
                .body("name", is("Mango"))
                .body("description", is("Tropical and juicy"));
    }

    @Test
    void shouldDeleteFruit() {
        // Create a fruit to delete
        int id = given()
            .contentType("application/json")
            .body("{\"name\": \"Kiwi\", \"description\": \"Fuzzy green fruit\"}")
            .when().post("/fruits")
            .then()
                .statusCode(201)
                .extract().path("id");

        // Delete it
        given()
            .when().delete("/fruits/" + id)
            .then()
                .statusCode(204);

        // Verify it's gone
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
