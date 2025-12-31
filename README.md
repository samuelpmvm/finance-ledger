# Finance Ledger

[![Last Commit](https://img.shields.io/github/last-commit/samuelpmvm/finance-ledger)](https://github.com/samuelpmvm/finance-ledger)

[![Java CI with Maven](https://github.com/samuelpmvm/finance-ledger/actions/workflows/maven.yml/badge.svg)](https://github.com/samuelpmvm/finance-ledger/actions/workflows/maven.yml)

[![CI - Docker build and Docker Compose Test](https://github.com/samuelpmvm/finance-ledger/actions/workflows/docker.yml/badge.svg)](https://github.com/samuelpmvm/finance-ledger/actions/workflows/docker.yml)

[![License](https://img.shields.io/github/license/samuelpmvm/finance-ledger)](LICENSE)

Finance Ledger is a backend-first SaaS for personal finance management.

The goal is to provide a solid, scalable technical foundation for:
- Manual management of accounts and transactions
- Future Open Banking integration
- Multi-month budgeting
- Secure, multi-tenant architecture

This repository contains **only the backend (REST API)**, built with Java and Spring Boot.

---

## 🚀 Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.0.0 |
| **Security** | Spring Security + OAuth2 Resource Server |
| **Identity Provider** | Keycloak 26.4.7 |
| **Database** | PostgreSQL 18 |
| **Migrations** | Liquibase 5.0.1 |
| **API Documentation** | OpenAPI 3.0 + OpenAPI Generator |
| **Object Mapping** | MapStruct 1.6.3 |
| **Metrics** | Micrometer + Prometheus |
| **Containerization** | Docker & Docker Compose |
| **Build Tool** | Maven |
| **CI/CD** | GitHub Actions |
| **Testing** | JUnit 5, Testcontainers, JaCoCo |

---

## 🏗 Architecture Overview

The application follows a **modular monolith** approach with **multi-tenant** support, organized by functional domains:

```
src/main/java/com/fintech/finance/ledger/
├── accounts/          # Financial accounts module (future)
├── common/            # Shared utilities
│   ├── exception/     # Global exception handling
│   └── tenant/        # Multi-tenant context management
├── controller/        # REST API controllers
├── entity/            # JPA entities (Account, Budget, Category, Transaction, User, Tenant)
├── mapper/            # MapStruct mappers
├── repository/        # Data access layer with tenant-aware queries
├── service/           # Business logic layer
└── userauth/          # Authentication & user provisioning
    ├── config/        # Security configuration
    └── filter/        # JWT filter & user context
```

### Key Features

- **Multi-Tenant Architecture**: Each user belongs to a tenant, ensuring data isolation
- **Automatic User Provisioning**: Users are automatically created on first login via Keycloak
- **API-First Design**: OpenAPI specifications drive code generation
- **Tenant-Aware Repositories**: All data access is scoped to the authenticated tenant

---

## 📚 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/finance-ledger/api/me` | Get current authenticated user |
| `GET` | `/finance-ledger/api/accounts` | List all accounts (paginated) |
| `POST` | `/finance-ledger/api/accounts` | Create a new account |
| `GET` | `/finance-ledger/api/accounts/{id}` | Get account by ID |
| `PUT` | `/finance-ledger/api/accounts/{id}` | Update an account |
| `DELETE` | `/finance-ledger/api/accounts/{id}` | Delete an account |
| `DELETE` | `/finance-ledger/api/accounts` | Delete all accounts |

### Account Types

- `bank` - Bank accounts
- `cash` - Cash accounts
- `investment` - Investment accounts
- `savings` - Savings accounts
- `other` - Other account types

---

## ▶️ Running Locally

### Prerequisites

Ensure you have the following installed:
- Docker
- Docker Compose

### Start the Application

1. Clone the repository:
   ```bash
   git clone https://github.com/samuelpmvm/finance-ledger.git
   cd finance-ledger
   ```

2. Start the application using Docker Compose:
   ```bash
   docker compose up
   ```

3. Access the application:
   - **API Base URL**: `http://localhost:8080/finance-ledger`
   - **Swagger UI**: `http://localhost:8080/finance-ledger/swagger-ui.html`
   - **Keycloak Admin Console**: `http://localhost:8081` (admin/admin)

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | PostgreSQL host |
| `DB_NAME` | finance-ledger_db | Database name |
| `DB_USER` | finance-ledger | Database user |
| `DB_PASSWORD` | finance-ledger | Database password |
| `KEYCLOAK_HOST` | localhost | Keycloak host |
| `KEYCLOAK_PORT` | 8081 | Keycloak port |
| `REALM` | finance | Keycloak realm |

---

## 🧪 Running Tests

Run the test suite:
```bash
./mvnw test
```

Run tests with coverage report:
```bash
./mvnw test jacoco:report
```

The coverage report will be available at `target/site/jacoco/index.html`.

---

## 🔧 Development

### Build the Application

```bash
./mvnw clean package
```

### Run Without Docker

```bash
# Start PostgreSQL and Keycloak first
docker compose up postgres keycloak -d

# Run the application
./mvnw spring-boot:run
```

### Code Generation

The project uses OpenAPI Generator to create API models and interfaces from OpenAPI specifications located in `src/main/resources/openapi/`.

---

## 📊 Monitoring

The application exposes the following management endpoints:

- `/actuator/health` - Health status
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

---

## 📜 License

This project is licensed under the terms of the [Apache 2.0 License](LICENSE).
