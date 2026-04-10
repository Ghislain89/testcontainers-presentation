package com.example;

import com.example.testsupport.AbstractComponentTest;
import com.example.testsupport.ComponentTest;
import com.example.testsupport.TestCase;
import com.example.testsupport.kafka.InjectKafkaMessageConsumer;
import com.example.testsupport.kafka.KafkaDriver;
import com.example.testsupport.kafka.KafkaMessageConsumer;
import com.example.testsupport.kafka.KafkaMessagingResource;
import com.example.testsupport.wiremock.RestMock;
import com.example.testsupport.wiremock.WireMockLifecycleManager;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * COMPONENT TEST — Full stack with real PostgreSQL, real Kafka, and WireMock
 * for external HTTP dependencies. Uses BDD-style given/when/then steps
 * mirroring the ace-quarkus-core component test pattern.
 */
@QuarkusTest
@QuarkusTestResource(WireMockLifecycleManager.class)
@QuarkusTestResource(KafkaMessagingResource.class)
class FruitComponentTest extends AbstractComponentTest {

    @WireMockLifecycleManager.WireMockPort
    int wireMockPort;

    @InjectKafkaMessageConsumer
    KafkaMessageConsumer kafkaConsumer;

    @BeforeEach
    void resetMocks() {
        WireMock.configureFor(wireMockPort);
        WireMock.reset();
    }

    @ComponentTest
    void createFruitAndVerifyKafkaEvent() {
        var fruitName = new String[]{ null };
        var fruitId = new int[]{ 0 };

        TestCase.given("a new fruit payload", () -> {
            fruitName[0] = "Dragonfruit";
        });

        TestCase.when("the fruit is created via REST API", () -> {
            fruitId[0] = given()
                    .contentType("application/json")
                    .body("{\"name\": \"" + fruitName[0] + "\", \"description\": \"Exotic pink fruit\"}")
                    .when().post("/fruits")
                    .then()
                    .statusCode(201)
                    .body("name", is(fruitName[0]))
                    .extract().path("id");
        });

        TestCase.then("the fruit is persisted in the database", () -> {
            given()
                    .when().get("/fruits/" + fruitId[0])
                    .then()
                    .statusCode(200)
                    .body("name", is(fruitName[0]))
                    .body("description", is("Exotic pink fruit"));
        });

        TestCase.and("a FRUIT_CREATED event is published to Kafka", () -> {
            String event = KafkaDriver.awaitMessage(
                    kafkaConsumer, "fruit-events",
                    KafkaDriver.messageContains("FRUIT_CREATED"));
            assertThat(event).contains(fruitName[0]);
        });
    }

    @ComponentTest
    void getDetailsWithExternalNutritionApi() {
        TestCase.given("the external nutrition API returns data for 'apple'", () -> {
            RestMock.stubFor(
                    RestMock.get("/api/nutrition/apple"),
                    WireMock.aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"calories\": 95, \"sugar\": \"19g\", \"fiber\": \"4.4g\"}")
            );
        });

        TestCase.when("we request details for the seeded Apple fruit", () -> {
            given()
                    .when().get("/fruits/1/details")
                    .then()
                    .statusCode(200)
                    .body("name", is("Apple"))
                    .body("nutrition.calories", is(95))
                    .body("nutrition.sugar", is("19g"))
                    .body("nutrition.fiber", is("4.4g"));
        });

        TestCase.then("the external nutrition API was called exactly once", () -> {
            RestMock.verify(1, RestMock.get("/api/nutrition/apple"));
        });
    }

    @ComponentTest
    void deleteFruitAndVerifyKafkaEvent() {
        var fruitId = new int[]{ 0 };

        TestCase.given("a fruit exists in the database", () -> {
            fruitId[0] = given()
                    .contentType("application/json")
                    .body("{\"name\": \"Papaya\", \"description\": \"Tropical orange fruit\"}")
                    .when().post("/fruits")
                    .then()
                    .statusCode(201)
                    .extract().path("id");
        });

        TestCase.when("the fruit is deleted", () -> {
            given()
                    .when().delete("/fruits/" + fruitId[0])
                    .then()
                    .statusCode(204);
        });

        TestCase.then("the fruit is no longer in the database", () -> {
            given()
                    .when().get("/fruits/" + fruitId[0])
                    .then()
                    .statusCode(404);
        });

        TestCase.and("a FRUIT_DELETED event is published to Kafka", () -> {
            String event = KafkaDriver.awaitMessage(
                    kafkaConsumer, "fruit-events",
                    KafkaDriver.messageContains("FRUIT_DELETED"));
            assertThat(event).contains("Papaya");
        });
    }
}
