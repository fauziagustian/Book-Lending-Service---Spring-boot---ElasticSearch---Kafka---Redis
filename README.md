# Book Lending System (App + Analytics) — Java 17, Spring Boot, Kafka, Redis, Elasticsearch

This project contains **two Spring Boot microservices**:

1) **book-lending-service** (App) — manages Books, Members, Loans and enforces borrowing rules  
2) **library-analytics-service** (Analytics) — consumes Kafka events and builds read models (Redis + Elasticsearch) and reports (native SQL)

![Architecture](docs/architecture.png)

---

## Tech Stack

**Core**
- Java 17, Spring Boot 3.x
- REST + JSON APIs
- PostgreSQL (relational persistence)
- Liquibase (schema migrations)

**Security**
- Spring Security (Basic Auth with predefined users/roles)

**Observability**
- Spring Boot Actuator (health, metrics)
- Micrometer + Prometheus endpoint

**Eventing & Read Models**
- Kafka (Confluent images)
- Redis (leaderboard / top borrowed books)
- Elasticsearch (search over loan events)

**Developer Productivity**
- Lombok
- OpenAPI / Swagger UI (springdoc)

---

## Services & Ports

| Component | Purpose | Port |
|---|---|---|
| App (`app`) | Books/Members/Loans APIs | **8080** |
| Analytics (`analytics`) | Top books, search, reports | **8081** |
| PostgreSQL (`postgres`) | DB | **5432** |
| Kafka (`kafka`) | Event bus | **9092** (host), **29092** (docker internal) |
| Zookeeper (`zookeeper`) | Kafka coordination | **2181** |
| Redis (`redis`) | Read model / caching | **6379** |
| Elasticsearch (`elasticsearch`) | Search | **9200** |

---

## Quick Start (Docker)

### 1) Run everything
From the project root:

```bash
docker compose down -v
docker compose up --build
```

### 2) Verify health
- App health: `http://localhost:8080/actuator/health`
- Analytics health: `http://localhost:8081/actuator/health`

### 3) Open API docs
- App Swagger UI: `http://localhost:8080/swagger-ui.html`
- Analytics Swagger UI: `http://localhost:8081/swagger-ui.html`

---

## Authentication (Basic Auth)

Predefined users:

- **Admin**
  - username: `admin`
  - password: `admin123`
  - role: `ADMIN`

- **Member**
  - username: `member`
  - password: `member123`
  - role: `MEMBER`

---

## Borrowing Rules (Config-based)

Configured in `application.properties` (not stored in DB):

- `library.rules.max-active-loans` — max active loans per member  
- `library.rules.loan-duration-days` — due date = borrowedAt + N days  
- A member **cannot borrow** if they have at least one **overdue** active loan

---

## End-to-End Testing (Postman)

A ready-to-import Postman collection is included:

- `postman/book-lending-e2e.postman_collection.json`

### Steps
1) Open Postman → **Import** → select the file above  
2) Run the collection with Collection Runner (it is ordered for E2E)

### What it tests
**App (8080)**
- Create Member (ADMIN)
- Create Book(s) (ADMIN)
- Borrow Book (MEMBER) → stores `loanId`
- Return Book (MEMBER)

**Analytics (8081)**
- Top Borrowed Books (Redis)
- Search Loan Events (Elasticsearch)
- Overdue Members Report (Advanced Native SQL)

> Note: Analytics is **eventually consistent** because it consumes Kafka events.  
> If analytics calls return empty at first, rerun the Analytics folder once (Kafka consumer may still be catching up).

---

## Run Tests + JaCoCo

```bash
mvn test
mvn verify
```

JaCoCo report:
- `target/site/jacoco/index.html`

---

## Troubleshooting

### Kafka fails because Zookeeper is unhealthy
This repo includes a Zookeeper healthcheck that does **not** rely on `nc/netcat` (common issue on some images).

### Elasticsearch repository fails on startup
Elasticsearch 8.x enables security by default. For local development this compose file **disables security** so Spring Data Elasticsearch can connect over HTTP.

### App container logs (if API is not reachable)
```bash
docker compose logs --tail=200 app
docker compose logs --tail=200 analytics
docker compose logs --tail=200 kafka
```
