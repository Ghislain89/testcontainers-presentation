# Quarkus + Testcontainers Demo

A Quarkus CRUD service demonstrating **three levels of testing** with Testcontainers, Kafka, WireMock, and BDD-style component tests.

## What's Inside

| File | Purpose |
|------|---------|
| `Fruit.java` | JPA entity using Panache (active record pattern) |
| `FruitResource.java` | REST endpoint (`/fruits`) with CRUD operations |
| `FruitEventProducer.java` | Publishes domain events to Kafka |
| `NutritionClient.java` | REST client calling external nutrition API |
| `V1__create_fruit_table.sql` | Flyway migration — creates the fruit table |
| `V2__seed_fruits.sql` | Flyway migration — seeds Apple, Banana, Cherry |

## Three Test Levels

### Level 1 — Unit Tests (`FruitResourceUnitTest`)

Uses `@InjectMock` to mock Kafka and the REST client while keeping a real database via Dev Services.

### Level 2 — Integration Tests (`FruitResourceTest`)

Zero-config — `@QuarkusTest` with Dev Services auto-starts PostgreSQL and Kafka. No mocks.

### Level 3 — Component Tests (`FruitComponentTest`)

Full BDD-style tests with WireMock for external APIs, real Kafka for event assertions, and real PostgreSQL. Uses shared test infrastructure from `testsupport/`.

### Explicit Testcontainers (`FruitResourceExplicitContainerTest`)

Demonstrates manual container management via `QuarkusTestProfile` when you need full control.

## Prerequisites

- Java 21+
- Docker running

## Run

```bash
# Dev mode (auto-starts PostgreSQL + Kafka containers)
./mvnw quarkus:dev

# Run all tests
./mvnw test
```

## API

```bash
curl http://localhost:8080/fruits              # List all
curl http://localhost:8080/fruits/1/details     # Get with nutrition info
curl -X POST http://localhost:8080/fruits \
  -H 'Content-Type: application/json' \
  -d '{"name":"Mango","description":"Tropical"}'  # Create
curl -X DELETE http://localhost:8080/fruits/1   # Delete
```
