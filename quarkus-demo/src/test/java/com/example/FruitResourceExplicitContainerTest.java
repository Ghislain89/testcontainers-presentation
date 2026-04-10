package com.example;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Demonstrates EXPLICIT Testcontainers usage — manually starting a PostgreSQL
 * container and wiring it into Quarkus via a custom test profile.
 *
 * This gives you full control over the container version, configuration,
 * init scripts, and lifecycle. Useful when you need a specific database version
 * or custom setup that Dev Services doesn't cover.
 */
@QuarkusTest
@TestProfile(FruitResourceExplicitContainerTest.PostgresProfile.class)
class FruitResourceExplicitContainerTest {

    // Explicit container — you control the image version & config
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("fruits_test")
            .withUsername("test")
            .withPassword("test");

    static {
        POSTGRES.start();
    }

    /**
     * A Quarkus test profile that overrides datasource config to point
     * at our manually-started container instead of using Dev Services.
     */
    public static class PostgresProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                "quarkus.datasource.jdbc.url", POSTGRES.getJdbcUrl(),
                "quarkus.datasource.username", POSTGRES.getUsername(),
                "quarkus.datasource.password", POSTGRES.getPassword(),
                "quarkus.datasource.devservices.enabled", "false"
            );
        }
    }

    @Test
    void shouldPersistToExplicitContainer() {
        // Create a fruit in the explicitly-managed PostgreSQL container
        int id = given()
            .contentType("application/json")
            .body("{\"name\": \"Pineapple\", \"description\": \"Spiky but sweet\"}")
            .when().post("/fruits")
            .then()
                .statusCode(201)
                .body("name", is("Pineapple"))
                .extract().path("id");

        // Read it back — proves data round-trips through real PostgreSQL
        given()
            .when().get("/fruits/" + id)
            .then()
                .statusCode(200)
                .body("name", is("Pineapple"))
                .body("description", is("Spiky but sweet"));
    }

    @Test
    void shouldListFruitsFromExplicitContainer() {
        given()
            .when().get("/fruits")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(3)); // seeded data
    }
}
