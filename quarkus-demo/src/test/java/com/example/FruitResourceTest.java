package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Unit integration test: Quarkus boots with REAL PostgreSQL (Dev Services)
 * and REAL Kafka. No mocks — tests the full stack against actual containers.
 *
 * Flyway runs migrations automatically; seed data comes from V2__seed_fruits.sql.
 *
 * Several tests here demonstrate issues that ONLY a real database catches —
 * mocks and H2 would miss these entirely.
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

    // --- Tests that ONLY pass with a real database (mocks would miss these) ---

    /**
     * A mock would happily accept a null name. Real PostgreSQL enforces NOT NULL.
     * This is the classic example from the slides: "tests pass, production breaks."
     */
    @Test
    void shouldRejectFruitWithNullName() {
        given()
            .contentType("application/json")
            .body("{\"name\": null, \"description\": \"Missing a name\"}")
            .when().post("/fruits")
            .then()
                .statusCode(500);
    }

    /**
     * A mock would save both without complaint. Real PostgreSQL enforces the
     * UNIQUE constraint added in V3 migration — a constraint that only exists
     * in the real database, not in any mock or in-memory fake.
     */
    @Test
    void shouldRejectDuplicateFruitName() {
        given()
            .contentType("application/json")
            .body("{\"name\": \"Durian\", \"description\": \"Love it or hate it\"}")
            .when().post("/fruits")
            .then()
                .statusCode(201);

        given()
            .contentType("application/json")
            .body("{\"name\": \"Durian\", \"description\": \"Another one\"}")
            .when().post("/fruits")
            .then()
                .statusCode(500);
    }

    /**
     * PostgreSQL uses nextval('fruit_seq') — sequence-based IDs that are always
     * positive integers. H2 might behave differently (e.g., different increment,
     * or starting value). This test verifies the real PostgreSQL sequence works.
     */
    @Test
    void shouldGenerateSequentialIdsFromPostgresSequence() {
        int id1 = given()
            .contentType("application/json")
            .body("{\"name\": \"Lychee\", \"description\": \"Sweet and fragrant\"}")
            .when().post("/fruits")
            .then()
                .statusCode(201)
                .extract().path("id");

        int id2 = given()
            .contentType("application/json")
            .body("{\"name\": \"Guava\", \"description\": \"Tropical pink\"}")
            .when().post("/fruits")
            .then()
                .statusCode(201)
                .extract().path("id");

        assertThat(id1).isGreaterThan(0);
        assertThat(id2).isGreaterThan(id1);
    }
}
