# Finance Ledger

[![Last Commit](https://img.shields.io/github/last-commit/samuelpmvm/finance-ledger)](https://github.com/samuelpmvm/finance-ledger)

[![Java CI with Maven](https://github.com/samuelpmvm/finance-ledger/actions/workflows/maven.yml/badge.svg)](https://github.com/samuelpmvm/device-manager/actions/workflows/maven.yml)

[![CI - Docker build and Docker Compose Test](https://github.com/samuelpmvm/finance-ledger/actions/workflows/docker.yml/badge.svg)](https://github.com/samuelpmvm/device-manager/actions/workflows/docker.yml)

[![License](https://img.shields.io/github/license/samuelpmvm/finance-ledger)](LICENSE)

Finance Ledger is a backend-first SaaS for personal finance management.

The goal is to provide a solid, scalable technical foundation for:
- manual management of accounts and transactions
- future Open Banking integration
- multi-month budgeting
- secure, multi-tenant architecture

This repository contains **only the backend (REST API)**, built with Java and Spring Boot.

---

## 🚀 Tech Stack

- Java 21
- Spring Boot 4
- Spring Security (OAuth2 Resource Server)
- Keycloak (Identity Provider)
- PostgreSQL
- Liquibase (database migrations)
- Docker & Docker Compose
- Maven
- GitHub Actions (CI)

---

## 🏗 Architecture Overview

The application follows a **modular monolith** approach, organized by functional domains:

- `userauth` – authentication and user management
- `accounts` – financial accounts
- `transactions` – transactions, categories, and rules
- `budget` – budgeting logic
- `openbanking` – external bank integrations (future)

Each module owns its domain logic and database tables.

---

## ▶️ Running locally

### Prerequisites
- Docker
- Docker Compose

### Start the application

```bash
docker compose up

