---
theme: ./theme
title: Introduction to Testcontainers
layout: intro
hideInToc: true
---

# Introduction to Testcontainers

Ghislain Gabriëlse

<!--
Welcome everyone! Today I'm going to talk about Testcontainers — a tool that has fundamentally changed how I think about integration testing. By the end of this session, you'll understand what Testcontainers is, why it's better than traditional mocking for integration tests, and how to start using it in your own projects.
-->

---
hideInToc: true
---

# Ghislain Gabriëlse

Test Automation Consultant <a href="https://detesters.nl/">DeTesters</a>

- Woerden, Netherlands 🇳🇱
- 36 years
- Father of 2 minions
- Butler to a cat
- ~12 years of experience
- Builds Tools that simplify complex tasks

<!--
A quick intro about myself. I'm Ghislain, a test automation consultant at DeTesters. I've been in the testing space for about 12 years now, and I'm passionate about building tools that make testing easier and more reliable. Testcontainers is one of those tools that I wish I had discovered earlier in my career.
-->

---
hideInToc: true
---

# Agenda

<Toc text-xs minDepth="1" maxDepth="1" />

<!--
Here's what we'll cover today. We'll start with what Testcontainers actually is, then look at the problems with traditional mocking approaches, discuss the benefits, walk through how it works under the hood, see some real code examples, and finish with best practices.
-->

---
layout: section
---

# What is Testcontainers?

---
layout: default
hideInToc: true
---

# What is Testcontainers?

Testcontainers is an open-source library that provides lightweight, throwaway instances of real services wrapped in Docker containers.

<div class="grid grid-cols-2 gap-8 mt-8">
<div>

### In a nutshell

- Programmatically start Docker containers from your tests
- Spin up **real** databases, message brokers, browsers, and more
- Containers are created before the test and destroyed after
- Available for Java, Node.js, Python, .NET, Go, Rust, and more

</div>
<div>

### Supported by

- 🐘 **Databases** — PostgreSQL, MySQL, MongoDB, Redis
- 📨 **Messaging** — Kafka, RabbitMQ, Pulsar
- 🌐 **Cloud** — LocalStack (AWS), Azure, GCP emulators
- 🧩 **And much more** — Elasticsearch, Keycloak, Selenium, ...

</div>
</div>

<!--
So what is Testcontainers? In short, it's a library that lets you spin up real services — like databases or message brokers — inside Docker containers, directly from your test code. The key word here is "real." You're not faking anything. You're running the actual PostgreSQL, the actual Kafka, the actual Redis. The containers are lightweight and throwaway — they spin up before your tests and are destroyed after. And it's not just for Java; there are implementations for most popular languages.
-->

---
layout: section
---

# Mocking vs Real Services

---
layout: default
hideInToc: true
---

# The Traditional Approach

When testing code that depends on external services, developers typically resort to:

```mermaid
flowchart LR
    A[Test Code] -->|calls| B[Mock / Stub]
    B -->|returns| C[Fake Response]
    C -->|validates| D[Assertions]
    style B fill:#ff6b6b,stroke:#333,color:#fff
```

- **Mocks** — Simulate behavior at the interface level
- **Stubs** — Return canned responses
- **In-memory replacements** — H2 instead of PostgreSQL, fakes instead of real services
- **Shared test environments** — A single dev/test database everyone connects to

<!--
Let's talk about what most of us do today when we need to test code that talks to a database or an external service. We mock it, we stub it, we use in-memory replacements like H2 instead of a real PostgreSQL, or we all point at a shared test database. These approaches work — to a degree — but they come with real trade-offs. Let me show you what I mean.
-->

---
layout: default
hideInToc: true
---

# Where Mocking Falls Short

<div class="grid grid-cols-2 gap-12 mt-4">
<div>

### 😬 The Risks

- **Behavior drift** — Mocks don't update when the real service changes
- **False confidence** — Tests pass, production breaks
- **SQL dialect gaps** — H2 doesn't behave like PostgreSQL
- **Missing edge cases** — Timeouts, connection limits, constraints
- **Complex setup** — Mocking deep dependency trees is painful

</div>
<div>

### 🤔 A Common Scenario

```java
// This test passes...
when(repo.save(any()))
    .thenReturn(savedEntity);

// But in production, a UNIQUE constraint
// violation causes a 500 error because
// we never tested against a real database.
```

> "Your mocks are only as good as your assumptions about the real system."

</div>
</div>

<!--
Here's where things get tricky. Mocks don't evolve with the real service. If someone adds a NOT NULL constraint to the database, your mock won't catch that. H2 has subtle SQL differences compared to PostgreSQL — I've personally seen tests pass on H2 and fail on production Postgres because of JSON operator differences. And look at this code example: the mock happily returns a saved entity, but in production, a UNIQUE constraint violation would blow up. Your mocks are only as good as your assumptions — and assumptions age badly.
-->

---
layout: default
hideInToc: true
---

# Why Testcontainers?

| Aspect | Mocking / Stubs | Testcontainers |
|---|---|---|
| **Fidelity** | Simulated behavior | Real service behavior |
| **SQL Dialect** | Generic (H2) | Exact production DB |
| **Environment** | No infra needed | Docker required |
| **Speed** | Very fast | Slightly slower |
| **Confidence** | Medium | High |
| **Isolation** | Per-test by design | Per-test containers |
| **Maintenance** | Keep mocks in sync | Self-updating |

> 💡 Testcontainers doesn't replace **all** mocking — it shines for **integration tests**.

<!--
Let's compare the two approaches side by side. The big wins for Testcontainers are fidelity and confidence — you're testing against the real thing, so you can trust the results. The trade-off is speed: containers take a few seconds to start, whereas mocks are instant. But here's the important nuance — Testcontainers is not meant to replace all your mocks. Unit tests with mocks are still great for fast feedback. Testcontainers is for your integration tests, where you need to know that the pieces actually fit together.
-->

---
layout: default
hideInToc: true
---

# Key Benefits

<div class="grid grid-cols-2 gap-8 mt-4">
<div>

### 🎯 Test against real services
No more behavior drift — your tests run against the same database engine as production.

### 🏝️ Complete isolation
Every test run gets fresh containers. No shared state, no flaky tests from leftover data.

### 🔄 Reproducible everywhere
Works the same on your laptop and in CI. No "works on my machine" problems.

</div>
<div>

### ⚡ Easy to set up
A few lines of code to spin up a fully configured PostgreSQL, Kafka, or Redis instance.

### 📦 No external infrastructure
No need to maintain shared test databases or services. Just Docker.

### 🧹 Automatic cleanup
Containers are automatically stopped and removed when tests finish. The **Ryuk** sidecar ensures nothing leaks.

</div>
</div>

<!--
Let me highlight the benefits that matter most in practice. First, you're testing against the real thing — no behavior drift. Second, every test run starts clean. If you've ever dealt with flaky tests because someone else's test left data in a shared database, you know how valuable this is. Third, it works the same everywhere — your laptop, your colleague's laptop, CI. Fourth, setup is trivial — a few lines of code. And finally, cleanup is automatic. There's a sidecar container called Ryuk that tracks everything and cleans up even if your test crashes. You don't have to worry about leaked containers.
-->

---
layout: section
---

# How It Works

---
layout: default
hideInToc: true
---

# Testcontainers Lifecycle

```mermaid {scale: 0.75}
sequenceDiagram
    participant Test as Test Suite
    participant TC as Testcontainers
    participant Docker as Docker Engine

    Test->>TC: Request container (e.g. PostgreSQL)
    TC->>Docker: Pull image & start container
    TC->>Docker: Wait until ready (health check)
    TC-->>Test: Return connection details (host, port)
    Test->>Docker: Run tests against real service
    Test->>TC: Tests complete
    TC->>Docker: Stop & remove container
```

<!--
Here's the lifecycle. Your test asks Testcontainers for a container — say PostgreSQL. The library pulls the Docker image if needed, starts the container, and then waits until it's actually ready — not just "running," but ready to accept connections. This is important because Docker's "running" state doesn't mean the service inside is ready. Once it's healthy, Testcontainers gives your test the connection details — host, port, JDBC URL — and your test runs against it. When tests are done, the container is stopped and removed. Clean slate every time.
-->

---
layout: default
hideInToc: true
---

# Architecture

```mermaid
flowchart TB
    subgraph Your Test
        A[Test Code] --> B[Testcontainers Library]
    end
    subgraph Docker
        B -->|manages| C[🐘 PostgreSQL Container]
        B -->|manages| D[📨 Kafka Container]
        B -->|manages| E[🔴 Redis Container]
        B -->|manages| F[🐳 Ryuk - cleanup sidecar]
    end
    A -->|connects to| C
    A -->|produces/consumes| D
    A -->|reads/writes| E
```

**Ryuk** is a special sidecar container that Testcontainers starts automatically. It tracks all containers created during the test session and cleans them up — even if your test crashes.

<!--
From an architecture perspective, your test code talks to the Testcontainers library, which manages containers through the Docker API. You can have multiple containers running at once — a database, a message broker, a cache — whatever your application needs. And notice Ryuk in the corner there. It's a special cleanup container that Testcontainers starts automatically. It keeps track of all containers created during the session and ensures they're cleaned up, even if your test process gets killed or crashes unexpectedly. It's a safety net that prevents Docker from filling up with orphaned containers.
-->

---
layout: default
hideInToc: true
---

# Example 1 — PostgreSQL Integration Test

```java {all|2-7|9-13|16-21}{maxHeight:'420px'}
@Testcontainers
class UserRepositoryTest {
    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test").withPassword("test");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired UserRepository userRepository;

    @Test
    void shouldPersistAndRetrieveUser() {
        userRepository.save(new User("Ghislain", "ghislain@example.com"));
        Optional<User> found = userRepository.findByEmail("ghislain@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Ghislain");
    }
}
```

<!--
Let's look at some real code. This is a Spring Boot test that verifies a UserRepository against an actual PostgreSQL database. The @Testcontainers annotation tells JUnit to manage the container lifecycle. The @Container annotation marks our PostgreSQL container — we're using the official postgres:16-alpine image and configuring database name, username, and password. The @DynamicPropertySource method is the magic glue — it injects the container's JDBC URL, username, and password into Spring's configuration at runtime. Notice we never hardcode a port — Testcontainers maps to a random available port. The test itself is clean and simple: save a user, find it by email, assert it's there. And this is running against real PostgreSQL — not H2, not a mock.
-->

---
layout: default
hideInToc: true
---

# Example 2 — REST API + Database Test

```java {all|1-3|5-11|14-22}{maxHeight:'420px'}
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class ProductApiTest {
    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired TestRestTemplate restTemplate;

    @Test
    void shouldCreateAndRetrieveProduct() {
        var product = new Product("Testcontainers T-Shirt", 29.99);
        var created = restTemplate.postForEntity("/api/products", product, Product.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var found = restTemplate.getForEntity(
            "/api/products/" + created.getBody().getId(), Product.class);
        assertThat(found.getBody().getName()).isEqualTo("Testcontainers T-Shirt");
    }
}
```

<!--
This second example takes it up a notch. We're now testing a full REST API — a Spring Boot app with a real database behind it. The setup is almost identical: same PostgreSQL container, same DynamicPropertySource wiring. But now we're using TestRestTemplate to make actual HTTP calls to our API endpoints. We POST a product, verify we get a 201 Created back, then GET it by ID and verify the data came through correctly. This is a true end-to-end integration test — HTTP request, through the controller, service layer, repository, into a real PostgreSQL database and back. If there's a serialization issue, a constraint violation, or a query bug, this test will catch it.
-->

---
layout: default
hideInToc: true
---

# Example 3 — GenericContainer (Any Docker Image)

Not limited to pre-built modules — use **any** Docker image:

```java {all|3-5|8-14}
@Testcontainers
class CustomServiceTest {
    @Container
    static GenericContainer<?> wiremock = new GenericContainer<>("wiremock/wiremock:3.5.4")
        .withExposedPorts(8080)
        .waitingFor(Wait.forHttp("/__admin/mappings").forStatusCode(200));

    @Test
    void shouldConnectToWiremock() {
        String baseUrl = "http://" + wiremock.getHost()
            + ":" + wiremock.getMappedPort(8080);
        given().baseUri(baseUrl)
            .when().get("/__admin/mappings")
            .then().statusCode(200);
    }
}
```

> 💡 `GenericContainer` — if it runs in Docker, you can test with it.

<!--
Now here's where it gets really powerful. Testcontainers isn't limited to databases — you can run any Docker image using GenericContainer. In this example, we're spinning up a WireMock server to simulate an external API. We expose port 8080, and we use a wait strategy to make sure the WireMock admin API is responding before our test starts. Then we use getMappedPort to get the actual port Docker assigned. This pattern is great for testing against services that don't have a dedicated Testcontainers module yet. Basically, if it runs in Docker, you can use it in your tests.
-->

---
layout: section
---

# Quarkus + Testcontainers in Practice

---
layout: default
hideInToc: true
---

# Why Quarkus Makes Testcontainers Shine

<div class="grid grid-cols-2 gap-8 mt-4">
<div>

### 🍃 Spring Boot

- Full classpath scan + auto-config **at runtime**
- `@SpringBootTest` startup: **5–15 seconds**
- `@MockBean` / `@DynamicPropertySource` change → **new ApplicationContext** → may restart containers
- Container setup: **manual** (`@Container` + `@DynamicPropertySource`)
- No built-in continuous test mode

</div>
<div>

### ⚡ Quarkus

- Bean wiring + config resolution **at build time**
- `@QuarkusTest` startup: **1–3 seconds**
- `@InjectMock` swaps beans **in-place** → single shared context across all tests
- Container setup: **automatic** (Dev Services)
- `quarkus:dev` → tests re-run on save in **< 2 seconds**

</div>
</div>

<div class="mt-6 text-center">

| | Spring Boot | Quarkus |
|---|---|---|
| **App boot** | 5–15s (runtime wiring) | 1–3s (build-time wiring) |
| **Container reuse** | New context = new containers | Shared across all test classes |
| **Config wiring** | Manual (`@DynamicPropertySource`) | Zero-config (Dev Services) |
| **Live testing** | ❌ Restart on change | ✅ `quarkus:dev` continuous mode |

</div>

<!--
Before we look at the demo, let's talk about why Quarkus is a particularly good fit for Testcontainers — and why this matters.

The number one objection I hear against Testcontainers is speed. "Real containers are slow." And that's true — spinning up a PostgreSQL container takes 3-5 seconds, Kafka maybe 5-8 seconds. But here's the thing: that container startup cost is fixed. What varies dramatically is how the framework itself contributes to test startup time.

Let's start on the left with Spring Boot. When you run a @SpringBootTest, Spring does everything at runtime. It scans the classpath to discover beans, evaluates hundreds of @Conditional annotations for auto-configuration, creates the ApplicationContext, and wires everything together. That's 5 to 15 seconds — and that's BEFORE any Testcontainers even start. So your actual test startup is container time PLUS framework time. In a typical Spring Boot project, you're looking at 15-25 seconds before your first test assertion runs.

Now here's where it gets worse. In Spring Boot, if you use @MockBean on one test class but not another, Spring creates separate ApplicationContexts for each unique configuration. Each new context may trigger new container startups. I've seen Spring Boot test suites where the same PostgreSQL container gets started three or four times because of context pollution. It's death by a thousand cuts.

Now look at the Quarkus side. Quarkus does bean discovery, injection wiring, and configuration resolution at BUILD time — during mvn compile. So when your test starts, all that's left is instantiation and connecting to containers. The app boots in 1 to 3 seconds. That's not a best case — that's the normal case.

The @InjectMock annotation is also fundamentally different from @MockBean. In Spring, @MockBean creates a new bean definition, which changes the context fingerprint, which triggers a new context. In Quarkus, @InjectMock swaps the bean in place within the existing context. No restart, no re-wiring, no new containers. Every @QuarkusTest class shares the same application context and the same Dev Services containers.

And then there's quarkus:dev — this is the killer feature for developer experience. You run mvn quarkus:dev once, and Quarkus starts your app with containers. As you edit code and save, it detects the changes, hot-reloads only what changed, and re-runs only the affected tests. The containers stay running. Your test feedback loop drops to under 2 seconds. There is nothing equivalent in Spring Boot — Spring DevTools does hot-reload but doesn't re-run tests, and you still pay the full context load time.

Look at the comparison table at the bottom. Every row is a win for Quarkus when it comes to test speed. This is where Testcontainers goes from "slightly slower than mocks" to "as fast as you'd ever need for day-to-day development."
-->

---
layout: default
hideInToc: true
---

# Closing the Gap in Spring Boot

<div class="grid grid-cols-2 gap-8 mt-4">
<div>

### `@ServiceConnection` (3.1+)

Eliminates manual wiring — Spring auto-discovers connection properties:

```java
@Container @ServiceConnection
static PostgreSQLContainer<?> postgres =
    new PostgreSQLContainer<>("postgres:16-alpine");
// No @DynamicPropertySource needed!
```

### Container reuse

Keep containers alive across JVM restarts:

```java
new PostgreSQLContainer<>("postgres:16-alpine")
    .withReuse(true);
```

<div class="text-xs mt-1">

`~/.testcontainers.properties` → `testcontainers.reuse.enable=true`

</div>

</div>
<div>

### Test slicing (avoid loading everything)

| Annotation | Loads only |
|---|---|
| `@DataJpaTest` | JPA + DB (~2–3s) |
| `@WebMvcTest` | Controllers only |
| `@JsonTest` | Serialization only |

### Avoid context pollution

- ❌ Each unique `@MockBean` combo → **new ApplicationContext** → new containers
- ❌ `@DirtiesContext` → forces full rebuild
- ✅ Group tests sharing the same mock setup
- ✅ Use `@Import` with test configurations

</div>
</div>

<div class="mt-4 text-center text-sm">

> 💡 Spring Boot can get close — but you're **opting in** to optimizations that Quarkus provides **by default**.

</div>

<!--
Now, to be fair — I don't want to turn this into a "Quarkus good, Spring bad" talk. Spring Boot has made significant improvements, and there are concrete things you can do to close this gap. Let me walk you through them.

Starting top-left: @ServiceConnection. This was introduced in Spring Boot 3.1 and it's a game changer for reducing boilerplate. Instead of writing that @DynamicPropertySource method we saw earlier — mapping JDBC URL, username, password manually — you just annotate your container with @ServiceConnection. Spring auto-discovers what type of container it is and wires the properties automatically. It's essentially Spring's answer to Quarkus Dev Services, though you still declare the container yourself.

Below that: container reuse. This is actually a Testcontainers feature, not a Spring feature, but it's especially valuable in Spring Boot where container restarts are more common. You call withReuse(true) on your container and set testcontainers.reuse.enable=true in your home directory's testcontainers.properties file. What this does is keep containers running even after your JVM shuts down. Next test run, Testcontainers detects the existing container and reconnects instead of starting a new one. This can save you 5-10 seconds per test run. In Quarkus Dev Services, this is the default behavior — containers are always shared and reused.

On the right side: test slicing. This is crucial and often overlooked. Many developers default to @SpringBootTest for everything, which loads the entire application. But if you're just testing a JPA repository, use @DataJpaTest — it only loads the persistence layer and boots in 2-3 seconds instead of 15. @WebMvcTest loads only the controller layer with MockMvc. @JsonTest loads only serialization. These sliced contexts are much faster because they don't load beans you don't need.

And finally, the most impactful advice: avoid context pollution. In Spring Boot, the ApplicationContext is cached and reused across test classes — but only if the configuration is identical. The moment you add a @MockBean that another test class doesn't have, Spring creates a separate context. I've worked on projects where the test suite had 8 different ApplicationContexts because every test class had a slightly different @MockBean setup. Each context loads the full app and may start fresh containers. The fix? Group tests that share the same mock configuration. Or even better, use @Import with explicit test configuration classes instead of @MockBean. And never, ever use @DirtiesContext unless you have no other choice — it forces a complete context teardown and rebuild.

The key takeaway here is the quote at the bottom: Spring Boot CAN get close to Quarkus' test performance, but you have to actively make these choices. In Quarkus, fast tests are the default path. In Spring Boot, fast tests require discipline and awareness of these pitfalls. Both can get the job done — Quarkus just makes it harder to accidentally make it slow.
-->

---
layout: default
hideInToc: true
---

# The Demo Application

A simple **Fruit CRUD API** built with Quarkus, demonstrating three test levels.

```mermaid
flowchart LR
    subgraph Quarkus App
        A[FruitResource] -->|JPA| B[🐘 PostgreSQL]
        A -->|Kafka| C[📨 Kafka]
        A -->|REST Client| D[🌐 Nutrition API]
    end
    subgraph Test Infrastructure
        B -.->|Dev Services| E[🐳 Testcontainers]
        C -.->|Dev Services| E
        D -.->|WireMock| F[🔌 Mock Server]
    end
```

<div class="grid grid-cols-3 gap-4 mt-4 text-sm">
<div>

**Database** — Panache ORM + Flyway

</div>
<div>

**Messaging** — Kafka events on CRUD

</div>
<div>

**REST Client** — External nutrition API

</div>
</div>

<!--
Let's move from theory to practice. I've built a demo application — a simple Fruit CRUD API in Quarkus — that demonstrates three different levels of testing with Testcontainers. The app has three external dependencies: PostgreSQL for persistence, Kafka for domain events, and a REST client calling an external nutrition API. This gives us a realistic service to test at different levels.
-->

---
layout: default
hideInToc: true
---

# Quarkus Dev Services — Zero Config Containers

In Quarkus, **Dev Services** automatically starts containers — no manual setup needed:

```properties {all|1-2|4-5|7-8}
# Just declare the DB kind — Quarkus starts PostgreSQL automatically
quarkus.datasource.db-kind=postgresql

# Flyway runs migrations against the Dev Services container
quarkus.flyway.migrate-at-start=true

# Kafka — Quarkus auto-starts a Redpanda container
mp.messaging.outgoing.fruit-events-out.connector=smallrye-kafka
```

<div class="mt-4">

> 💡 No container declaration, no port mapping, no JDBC URL.
> Quarkus detects the dependencies and starts what's needed.

</div>

<div class="mt-2 text-sm">

**Dev Services starts:** PostgreSQL 16 + Redpanda (Kafka) — both via Testcontainers under the hood.

</div>

<!--
Now here's where Quarkus makes Testcontainers even more powerful. With Quarkus Dev Services, you don't need to declare containers at all. You just say "I need PostgreSQL" in your config, and Quarkus automatically spins up a PostgreSQL container using Testcontainers. Same for Kafka — just add the Kafka connector dependency, and Quarkus starts a Redpanda container for you. No JDBC URLs, no port mapping, no container lifecycle code. It's Testcontainers with zero boilerplate.
-->

---
layout: default
hideInToc: true
---

# The Testing Pyramid — Three Levels

<div class="grid grid-cols-3 gap-6 mt-6">
<div class="border rounded p-4 bg-green-900/20 border-green-500/40">

### 🏎️ Unit Test

**`@InjectMock`** replaces CDI beans

- Kafka producer → mocked
- REST client → mocked
- Database → real (Dev Services)
- ⚡ Fast feedback on logic

</div>
<div class="border rounded p-4 bg-blue-900/20 border-blue-500/40">

### 🔗 Integration Test

**Dev Services** provides all containers

- Kafka → real (Redpanda)
- Database → real (PostgreSQL)
- REST client → not tested
- 🎯 Full stack validation

</div>
<div class="border rounded p-4 bg-purple-900/20 border-purple-500/40">

### 🧩 Component Test

**WireMock** + **Kafka** + **BDD**

- External APIs → WireMock
- Kafka events → asserted
- Database → real (PostgreSQL)
- 🏗️ End-to-end confidence

</div>
</div>

<!--
In our demo app, we test at three distinct levels. At the bottom, unit tests mock out Kafka and the REST client using Quarkus' @InjectMock, but still use a real database — because Dev Services makes it free. Integration tests use real containers for everything: real PostgreSQL, real Kafka — testing the full stack without any external API calls. And at the top, component tests bring in WireMock for external APIs and use BDD-style given/when/then steps — this is the pattern from ace-quarkus-core. Each level catches different types of bugs.
-->

---
layout: default
hideInToc: true
---

# Level 1 — Unit Test with `@InjectMock`

```java {all|1-5|7-10|12-23|25-26}{maxHeight:'420px'}
@QuarkusTest
class FruitResourceUnitTest {
    @InjectMock
    FruitEventProducer eventProducer;  // Kafka producer → mocked

    @InjectMock @RestClient
    NutritionClient nutritionClient;   // External API → mocked

    @Test
    void createFruitShouldFireEvent() {
        Mockito.doNothing().when(eventProducer).send(any());

        given()
            .contentType("application/json")
            .body("{\"name\": \"Grape\", \"description\": \"Small and round\"}")
        .when().post("/fruits")
        .then()
            .statusCode(201)
            .body("name", is("Grape"));

        Mockito.verify(eventProducer).send(any(FruitEvent.class));
    }

    @Test
    void getDetailsShouldCallNutritionClient() {
        Mockito.when(nutritionClient.getNutrition(eq("apple")))
            .thenReturn(new NutritionInfo(95, "19g", "4.4g"));

        given().when().get("/fruits/1/details")
        .then()
            .statusCode(200)
            .body("nutrition.calories", is(95));
    }
}
```

<!--
Let's start with unit tests. Notice that even though this is a "unit" test, we still have a real PostgreSQL database running — Dev Services starts it for free. We only mock the things we want to isolate: the Kafka producer and the external REST client. The @InjectMock annotation replaces the CDI bean with a Mockito mock. This lets us test the controller logic — routing, serialization, event firing — without needing real Kafka or a real external API. Fast feedback on business logic, with real database behavior.
-->

---
layout: default
hideInToc: true
---

# Level 2 — Integration Test (Real Containers)

```java {all|1-3|5-14|16-25}{maxHeight:'420px'}
@QuarkusTest  // Dev Services auto-starts PostgreSQL + Kafka
class FruitResourceTest {

    @Test
    void shouldListSeededFruits() {
        // Flyway runs V1 + V2 migrations → Apple, Banana, Cherry exist
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
            .then().statusCode(201).extract().path("id");

        given().when().get("/fruits/" + id)
            .then().statusCode(200).body("name", is("Mango"));
    }
}
```

<div class="mt-2">

> 💡 No mocks, no container declarations. Just `@QuarkusTest` — Dev Services handles the rest.

</div>

<!--
Integration tests are the cleanest. Just annotate with @QuarkusTest and write your test. No mock setup, no container declarations, no port configuration. Dev Services automatically provides a real PostgreSQL and a real Kafka. Flyway runs our migrations, seed data is inserted, and we test against the real full stack. Notice how little boilerplate there is compared to the Spring Boot examples we saw earlier — this is what framework integration with Testcontainers looks like.
-->

---
layout: default
hideInToc: true
---

# Level 2b — Explicit Testcontainers (When You Need Control)

```java {all|1-8|10-19|21-29}{maxHeight:'420px'}
@QuarkusTest
@TestProfile(FruitResourceExplicitContainerTest.PostgresProfile.class)
class FruitResourceExplicitContainerTest {

    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("fruits_test")
            .withUsername("test").withPassword("test");
    static { POSTGRES.start(); }

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
        int id = given()
            .contentType("application/json")
            .body("{\"name\": \"Pineapple\", \"description\": \"Spiky but sweet\"}")
            .when().post("/fruits")
            .then().statusCode(201).extract().path("id");

        given().when().get("/fruits/" + id)
            .then().statusCode(200).body("name", is("Pineapple"));
    }
}
```

<!--
Sometimes you need full control over the container — a specific PostgreSQL version, custom init scripts, or a particular configuration. In that case, you can still use explicit Testcontainers. In Quarkus, you wire it through a TestProfile that overrides the datasource config and disables Dev Services. The pattern is the same as the Spring Boot examples we saw, but uses Quarkus' config override mechanism instead of DynamicPropertySource. Use this approach when Dev Services' defaults don't match your needs.
-->

---
layout: section
---

# Building Reusable Test Infrastructure

---
layout: default
hideInToc: true
---

# The Shared Library Pattern

In a large project, every service needs the same test setup. Instead of copy-pasting:

```mermaid
flowchart TB
    subgraph "ace-quarkus-core (shared library)"
        A[🔌 WireMock Lifecycle Manager]
        B[📨 Kafka Test Resources]
        C[🧪 BDD TestCase Utility]
        D[⚙️ RestAssured Config]
    end
    subgraph "Services"
        E[credit-service] -->|depends on| A & B & C & D
        F[order-service] -->|depends on| A & B & C & D
        G[fruit-demo] -->|depends on| A & B & C & D
    end
```

<div class="mt-4 text-sm">

| Component | What it does |
|---|---|
| `WireMockLifecycleManager` | Auto-starts WireMock, injects base URL into REST client config |
| `KafkaMessagingResource` | Provides `KafkaMessageConsumer` / `Producer` for event assertions |
| `TestCase` | BDD given/when/then with execution logging (✓/✘ tree on failure) |
| `RestAssuredConfigurationExtension` | Auto-configures RestAssured for all `@QuarkusTest` classes |

</div>

<!--
When you have dozens of microservices that all need PostgreSQL, Kafka, and WireMock in their tests, you don't want to copy-paste the same setup into each one. In our project, we extracted this into a shared library called ace-quarkus-core. It provides reusable test resources: a WireMock lifecycle manager, Kafka test utilities, BDD scaffolding, and RestAssured configuration. Every service just depends on this library and gets consistent, well-maintained test infrastructure out of the box.
-->

---
layout: default
hideInToc: true
---

# WireMock as a Quarkus Test Resource

```java {all|1-11|13-18|20-24}{maxHeight:'420px'}
public class WireMockLifecycleManager
        implements QuarkusTestResourceLifecycleManager {
    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();

        String endpoint = "http://localhost:" + wireMockServer.port();
        return Map.of(
            "wiremock.server.base-url", endpoint,
            "quarkus.rest-client.nutrition-api.url", endpoint  // ← rewires REST client
        );
    }

    @Override
    public void stop() { wireMockServer.stop(); }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(wireMockServer.port(),
            new TestInjector.AnnotatedAndMatchesType(WireMockPort.class, Integer.TYPE));
    }
}
```

> 💡 Returning config map entries **automatically overrides** `application.properties` — the REST client now calls WireMock instead of the real API.

<!--
Here's the core of the WireMock integration. The WireMockLifecycleManager implements Quarkus' QuarkusTestResourceLifecycleManager interface. When Quarkus starts the test, it calls start() — we spin up WireMock on a dynamic port and return a map of configuration overrides. The key trick is that we override the REST client URL to point at WireMock. So the NutritionClient that normally calls an external API now transparently calls WireMock. The inject method lets us inject the WireMock port directly into test fields. Cleanup happens automatically in stop().
-->

---
layout: default
hideInToc: true
---

# BDD Component Test — Full Stack

```java {all|1-7|9-13|15-22|24-30}{maxHeight:'420px'}
@QuarkusTest
@QuarkusTestResource(WireMockLifecycleManager.class)
@QuarkusTestResource(KafkaMessagingResource.class)
class FruitComponentTest extends AbstractComponentTest {

    @InjectKafkaMessageConsumer
    KafkaMessageConsumer kafkaConsumer;

    @ComponentTest
    void createFruitAndVerifyKafkaEvent() {
        var fruitId = new int[]{ 0 };

        TestCase.given("a new fruit payload", () -> { /* setup */ });

        TestCase.when("the fruit is created via REST API", () -> {
            fruitId[0] = given()
                .contentType("application/json")
                .body("{\"name\": \"Dragonfruit\", \"description\": \"Exotic pink fruit\"}")
                .when().post("/fruits")
                .then().statusCode(201).extract().path("id");
        });

        TestCase.then("the fruit is persisted in the database", () -> {
            given().when().get("/fruits/" + fruitId[0])
                .then().statusCode(200).body("name", is("Dragonfruit"));
        });

        TestCase.and("a FRUIT_CREATED event is published to Kafka", () -> {
            String event = KafkaDriver.awaitMessage(
                kafkaConsumer, "fruit-events",
                KafkaDriver.messageContains("FRUIT_CREATED"));
            assertThat(event).contains("Dragonfruit");
        });
    }
}
```

<!--
This is the crown jewel — a full BDD component test. Look at the annotations: @QuarkusTest boots the app, @QuarkusTestResource starts WireMock and Kafka test resources. The test extends AbstractComponentTest which manages the BDD lifecycle. Inside the test, we use TestCase.given/when/then steps that are logged with checkmarks or crosses on failure — super helpful for debugging in CI. We create a fruit via the REST API, verify it's persisted in the real database, and then assert that a FRUIT_CREATED event appeared on the real Kafka topic. This is testing the full chain: HTTP → business logic → database → messaging. Everything is real except external APIs which use WireMock.
-->

---
layout: default
hideInToc: true
---

# WireMock + BDD — External API Testing

```java {all|1-11|13-20|22-24}{maxHeight:'420px'}
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
                .body("nutrition.calories", is(95));
    });

    TestCase.then("the external nutrition API was called exactly once", () -> {
        RestMock.verify(1, RestMock.get("/api/nutrition/apple"));
    });
}
```

> 💡 `RestMock` wraps WireMock with a clean API — stub ↔ verify pattern for external service dependencies.

<!--
This test demonstrates WireMock integration beautifully. In the given step, we stub the external nutrition API using RestMock — our simplified wrapper around WireMock. When the application calls GET /fruits/1/details, it internally calls the NutritionClient which now hits WireMock instead of the real API. We verify the full response chain and then assert that WireMock was called exactly once. This pattern is powerful because it lets you test error scenarios too — what happens when the external API returns 500? Just change the stub. No need for a real external service.
-->

---
layout: default
hideInToc: true
---

# What the Failure Logging Looks Like

When a BDD step fails, the `TestCase` utility prints an execution tree:

```
✘ Scenario createFruitAndVerifyKafkaEvent
  ✓ Given  a new fruit payload
  ✓ When   the fruit is created via REST API
  ✓ Then   the fruit is persisted in the database
  ✘ and    a FRUIT_CREATED event is published to Kafka
           → Timeout waiting for message matching predicate on topic 'fruit-events'
```

<div class="mt-4">

### Why this matters

- Instantly see **which step** failed — no stack trace hunting
- **Passed steps** are marked with ✓ — see how far execution got
- Works great in **CI logs** — readable without IDE tooling
- Inspired by the `ace-quarkus-core` test framework used across 30+ services

</div>

<!--
One of the best features of the BDD test infrastructure is the failure logging. When a test fails, instead of getting a raw stack trace, you get this execution tree. Every step is marked with a checkmark or a cross. You can instantly see that the first three steps passed and the fourth one failed — it was waiting for a Kafka message that never arrived. This is incredibly valuable in CI where you don't have IDE debugging. It's inspired by the ace-quarkus-core framework that we use across more than thirty microservices.
-->

---
layout: default
hideInToc: true
---

# Results — 12 Tests, 3 Levels, Real Confidence

<div class="grid grid-cols-2 gap-8 mt-4">
<div>

| Test Class | Level | Containers | Tests |
|---|---|---|---|
| `FruitResourceUnitTest` | Unit | 🐘 PostgreSQL | 3 |
| `FruitResourceTest` | Integration | 🐘 + 📨 | 4 |
| `ExplicitContainerTest` | Integration | 🐘 (explicit) + 📨 | 2 |
| `FruitComponentTest` | Component | 🐘 + 📨 + 🔌 | 3 |

</div>
<div>

### Key takeaways

- ⚡ **Unit**: Mock what you isolate, real DB is free
- 🎯 **Integration**: Zero config via Dev Services
- 🏗️ **Component**: BDD + WireMock + Kafka = full confidence
- 🧹 **All cleanup is automatic** — Ryuk handles it
- 🔄 **Reproducible** — works on any machine with Docker

</div>
</div>

<div class="mt-4 text-center text-lg">

`mvn test` → **12 tests** ✅ **BUILD SUCCESS** (26s)

</div>

<!--
Let me show you the final results. We have 12 tests across 4 test classes covering 3 levels. The unit tests mock Kafka and the REST client but use a real database. The integration tests use only real containers — no mocks at all. The explicit container test shows you can still take control when needed. And the component tests combine everything: real database, real Kafka, and WireMock for external APIs with BDD-style steps. All 12 tests pass in about 26 seconds. Everything is reproducible — any developer with Docker can run these on any machine.
-->

---
layout: section
---

# Best Practices

---
layout: default
hideInToc: true
---

# Tips for Real-World Usage

<div class="grid grid-cols-2 gap-8 mt-4">
<div>

### ✅ Do

- **Reuse containers** across tests (`static` + `@Container`)
- **Use `@DynamicPropertySource`** — don't hardcode ports
- **Pin image tags** (e.g. `postgres:16-alpine`)
- **Use `.waitingFor()`** strategies

</div>
<div>

### ❌ Avoid

- New container per test method — it's slow
- Fixed ports — use `getMappedPort()` instead
- Replacing **all** mocks — unit tests are still valuable

</div>
</div>

<div class="mt-6 text-center">

### The Testing Sweet Spot

`Unit Tests (mocks) ⚡ → Integration Tests (Testcontainers) 🎯 → E2E Tests 🐢`

</div>

<!--
A few tips from real-world experience. First, make your containers static and reuse them across test methods — spinning up a new PostgreSQL for every single test is wasteful when you can just clear the data between tests. Always use DynamicPropertySource or equivalent — never hardcode ports, because Testcontainers maps to random ports each time. Pin your image tags — using :latest might surprise you when an image update changes behavior. And use wait strategies so your tests don't start before the container is actually ready. On the flip side, don't go overboard replacing every mock with a container. Unit tests with mocks are still fast and valuable. Think of it as a testing pyramid — mocks for unit tests, Testcontainers for integration tests, and a few end-to-end tests at the top.
-->

---
layout: default
hideInToc: true
---

# Getting Started

Add Testcontainers to your project:

```xml
<!-- Maven -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.20.4</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.20.4</version>
    <scope>test</scope>
</dependency>
```

**Prerequisites:**
- Docker installed and running
- JUnit 5
- That's it! 🎉

📚 Docs: [testcontainers.com](https://testcontainers.com)  
💻 GitHub: [github.com/testcontainers](https://github.com/testcontainers)

<!--
Getting started is straightforward. Add the Testcontainers BOM and the module you need — here we're adding the core library and the PostgreSQL module. You need Docker installed and running, and JUnit 5. That's literally it. No complex infrastructure setup, no test environment provisioning. The official docs at testcontainers.com are excellent and have examples for every supported module.
-->

---
layout: default
hideInToc: true
---

# Thank You!

<div class="mt-8 text-center text-xl">

Questions? 🙋

</div>

<div class="mt-12 text-center">

📚 [testcontainers.com](https://testcontainers.com) — Official documentation  
💻 [github.com/testcontainers](https://github.com/testcontainers) — Source code & examples  
🐘 [testcontainers.com/modules](https://testcontainers.com/modules/) — Browse all available modules

</div>

<!--
That wraps up the presentation! To summarize: Testcontainers lets you test against real services in Docker, giving you much higher confidence than mocks for integration tests. It's easy to set up, fully isolated, reproducible, and cleans up after itself. I'd encourage you to try it on your next project — start with one integration test against a real database and see how it feels. Happy to take any questions!
-->
