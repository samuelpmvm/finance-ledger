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
├── common/            # Shared utilities
│   ├── exception/     # Global exception handling
│   ├── tenant/        # Multi-tenant context management
│   └── validator/     # Business rule validators (deletion policies)
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

### Accounts

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/finance-ledger/api/v1/accounts` | List all accounts (paginated) |
| `POST` | `/finance-ledger/api/v1/accounts` | Create a new account |
| `GET` | `/finance-ledger/api/v1/accounts/{id}` | Get account by ID |
| `PUT` | `/finance-ledger/api/v1/accounts` | Update an account |
| `PATCH` | `/finance-ledger/api/v1/accounts/{id}?archive={true\|false}` | Archive or unarchive an account |
| `DELETE` | `/finance-ledger/api/v1/accounts/{id}` | Delete an account |
| `DELETE` | `/finance-ledger/api/v1/accounts` | Delete all accounts |

### Categories

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/finance-ledger/api/v1/categories` | List all categories (paginated) |
| `POST` | `/finance-ledger/api/v1/categories` | Create a new category |
| `GET` | `/finance-ledger/api/v1/categories/{id}` | Get category by ID |
| `PUT` | `/finance-ledger/api/v1/categories` | Update a category |
| `DELETE` | `/finance-ledger/api/v1/categories/{id}` | Delete a category |
| `DELETE` | `/finance-ledger/api/v1/categories` | Delete all categories |

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/finance-ledger/api/v1/transactions` | List all transactions (paginated) |
| `POST` | `/finance-ledger/api/v1/transactions` | Create a new transaction |
| `GET` | `/finance-ledger/api/v1/transactions/{id}` | Get transaction by ID |
| `PUT` | `/finance-ledger/api/v1/transactions` | Update a transaction |
| `DELETE` | `/finance-ledger/api/v1/transactions/{id}` | Delete a transaction |
| `DELETE` | `/finance-ledger/api/v1/transactions` | Delete all transactions |

### User

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/finance-ledger/api/v1/me` | Get current authenticated user |

### Account Types

- `bank` - Bank accounts
- `cash` - Cash accounts
- `investment` - Investment accounts
- `savings` - Savings accounts
- `other` - Other account types

---

## 📋 Business Rules

### Account Archive Feature

Accounts can be archived instead of deleted to preserve historical data:

- **Archive an account**: `PATCH /accounts/{id}?archive=true`
- **Unarchive an account**: `PATCH /accounts/{id}?archive=false`
- Archived accounts are still accessible but can be filtered out from active account lists
- This provides a soft-delete alternative that preserves transaction history

### Deletion Policies

The application enforces the following deletion policies to maintain data integrity:

#### Account Deletion Policy
- **Accounts with associated transactions cannot be deleted**
- When attempting to delete an account that has transactions, the API returns HTTP status `226 IM_USED` with an error message
- This applies to both single account deletion (`DELETE /accounts/{id}`) and bulk deletion (`DELETE /accounts`)
- To delete an account, first delete or reassign all associated transactions

#### Category Deletion Policy
- **Categories with child categories cannot be deleted**
- When attempting to delete a category that has child categories, the API returns HTTP status `226 IM_USED` with an error message
- This applies to both single category deletion (`DELETE /categories/{id}`) and bulk deletion (`DELETE /categories`)
- To delete a parent category, first delete or reassign all child categories

### Error Responses

| HTTP Status | Error Type | Description |
|-------------|------------|-------------|
| `404 Not Found` | AccountNotFoundException | Account not found with the given ID |
| `404 Not Found` | CategoryNotFoundException | Category not found with the given ID |
| `404 Not Found` | TransactionNotFoundException | Transaction not found with the given ID |
| `226 IM Used` | AccountDeletionNotAllowedException | Account cannot be deleted due to associated transactions |
| `226 IM Used` | CategoryDeletionNotAllowedException | Category cannot be deleted due to child categories |

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
