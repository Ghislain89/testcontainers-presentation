package com.example;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * UNIT TEST — Quarkus boots but external dependencies are mocked.
 * No real Kafka, no real external HTTP calls.
 * Tests controller logic in isolation: routing, serialization, error handling.
 */
@QuarkusTest
class FruitResourceUnitTest {

    @InjectMock
    FruitEventProducer eventProducer;

    @InjectMock
    @RestClient
    NutritionClient nutritionClient;

    @Test
    void createFruitShouldFireEvent() {
        // Suppress real Kafka — mock just records the call
        Mockito.doNothing().when(eventProducer).send(any());

        int id = given()
                .contentType("application/json")
                .body("{\"name\": \"Grape\", \"description\": \"Small and round\"}")
                .when().post("/fruits")
                .then()
                .statusCode(201)
                .body("name", is("Grape"))
                .extract().path("id");

        // Verify the event producer was called
        Mockito.verify(eventProducer).send(any(FruitEvent.class));
    }

    @Test
    void getDetailsShouldCallNutritionClient() {
        // Mock the external API
        Mockito.when(nutritionClient.getNutrition(eq("apple")))
                .thenReturn(new NutritionInfo(95, "19g", "4.4g"));

        // The seeded "Apple" fruit has id=1 from Flyway migration
        given()
                .when().get("/fruits/1/details")
                .then()
                .statusCode(200)
                .body("name", is("Apple"))
                .body("nutrition.calories", is(95))
                .body("nutrition.sugar", is("19g"));

        Mockito.verify(nutritionClient).getNutrition("apple");
    }

    @Test
    void getDetailsShouldReturn404ForUnknownFruit() {
        given()
                .when().get("/fruits/99999/details")
                .then()
                .statusCode(404);
    }
}
