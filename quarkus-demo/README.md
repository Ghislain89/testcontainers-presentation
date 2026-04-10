# Quarkus + Testcontainers Demo

A small Quarkus CRUD service demonstrating two ways to use **Testcontainers** for integration testing with a real PostgreSQL database.

## What's Inside

| File | Purpose |
|------|---------|
| `Fruit.java` | JPA entity using Panache (active record pattern) |
| `FruitResource.java` | REST endpoint (`/fruits`) with CRUD operations |
| `FruitResourceTest.java` | Tests using **Dev Services** — Quarkus auto-starts PostgreSQL via Testcontainers |
| `FruitResourceExplicitContainerTest.java` | Tests using **explicit Testcontainers** — manually managed container with custom config |

## Two Testcontainers Approaches

### 1. Dev Services (implicit — zero config)

Quarkus detects the PostgreSQL JDBC driver and **automatically starts a container** during `dev` and `test` — powered by Testcontainers under the hood. No datasource URL needed!

```java
@QuarkusTest  // That's it — PostgreSQL is running!
class FruitResourceTest { ... }
```

### 2. Explicit Testcontainers (full control)

Manually create and configure a `PostgreSQLContainer`, then wire it into Quarkus via a test profile. Useful when you need a specific version or custom setup.

```java
static final PostgreSQLContainer<?> POSTGRES =
    new PostgreSQLContainer<>("postgres:16-alpine");

public static class PostgresProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("quarkus.datasource.jdbc.url", POSTGRES.getJdbcUrl(), ...);
    }
}
```

## Prerequisites

- Java 21+
- Docker running

## Run

```bash
# Dev mode (auto-starts PostgreSQL container)
./mvnw quarkus:dev

# Run tests (both approaches)
./mvnw test
```

## API

```bash
curl http://localhost:8080/fruits            # List all
curl -X POST http://localhost:8080/fruits \
  -H 'Content-Type: application/json' \
  -d '{"name":"Mango","description":"Tropical"}' # Create
curl -X DELETE http://localhost:8080/fruits/1   # Delete
```
