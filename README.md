# Introduction to Testcontainers

A ~30-minute presentation about [Testcontainers](https://testcontainers.com) — real Docker containers in your tests — with a focus on how [Quarkus](https://quarkus.io) Dev Services makes integration testing effortless.

Built with [Slidev](https://sli.dev) using a custom **Sakura Night** theme.

## 🚀 Getting Started

```bash
npm install
npm run dev
```

Open [localhost:3030](http://localhost:3030) to view the presentation.

## 📦 Export

```bash
npm run export
```

Generates `slides-export.pdf`. Requires [Playwright Chromium](https://playwright.dev).

## 🗂 Structure

| Path | Description |
|---|---|
| `slides.md` | All slides, speaker notes, and diagrams |
| `theme/` | Custom Sakura Night theme (CSS, layouts) |
| `public/` | Static assets (logos, QR code, landscape SVG) |
| `quarkus-demo/` | Quarkus demo app with three test levels |

## 🎯 What's Covered

1. **What is Testcontainers?** — Real services in Docker, throwaway containers
2. **Mocking vs Real Services** — Where mocks fall short
3. **How It Works** — Lifecycle, PostgreSQL & GenericContainer examples
4. **Quarkus + Testcontainers** — Dev Services, Spring Boot comparison
5. **Live Demo** — Two test levels against a Fruit CRUD API
6. **Best Practices** — Tips for container reuse, wait strategies, getting started

## 🧪 Demo Application

The `quarkus-demo/` folder contains a Quarkus Fruit CRUD API with:

- **Level 1 — Unit Tests**: `@InjectMock` for Kafka/REST client, real PostgreSQL via Dev Services
- **Level 2 — Integration Tests**: Full stack with real PostgreSQL + Kafka, no mocks

See [`quarkus-demo/README.md`](quarkus-demo/README.md) for setup and run instructions.

## 📋 Prerequisites

- [Node.js](https://nodejs.org) 18+ (for Slidev)
- [Docker](https://www.docker.com) (for running the demo tests)
- [Java 17+](https://adoptium.net) & Maven (for the Quarkus demo)

## 📄 License

MIT
