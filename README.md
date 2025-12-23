# Book Lending Microservice (Java 17, Spring Boot)

A self-contained microservice for managing **books**, **members**, and **loans (borrow/return)** with:
- **Secured REST APIs** (Basic Auth, in-memory users)
- **Relational DB persistence** (PostgreSQL)
- **DB migrations** (Liquibase)
- **Observability** (structured logging, Spring Boot Actuator health + metrics, Prometheus endpoint)
- **API documentation** (OpenAPI/Swagger)
- **Tests** (JUnit 5 + Mockito + integration tests) and **JaCoCo** coverage report
- **Consistent API responses** via `BaseResponse<T>`

---

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Web + Spring Data JPA
- Spring Security (Basic Auth)
- PostgreSQL
- Liquibase
- Springdoc OpenAPI (Swagger UI)
- Actuator + Micrometer (Prometheus)
- JUnit 5 + Mockito
- JaCoCo

---

## 1) Run Locally (Docker only)

```bash
docker compose up --build
```

When the service is up:
- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`
- Prometheus metrics: `http://localhost:8080/actuator/prometheus`

---

## 2) Run with Maven (without Docker) — optional

### 2.1 Start PostgreSQL
You can run Postgres locally in any way you prefer. Example with Docker:

```bash
docker run --name library-postgres -e POSTGRES_DB=library -e POSTGRES_USER=library -e POSTGRES_PASSWORD=library -p 5432:5432 -d postgres:16
```

### 2.2 Run the application
```bash
mvn spring-boot:run
```

Environment variables (optional; defaults are already provided in `application.properties`):
- `DB_URL` (default: `jdbc:postgresql://localhost:5432/library`)
- `DB_USER` (default: `library`)
- `DB_PASSWORD` (default: `library`)

---

## 3) Configuration (Borrowing Rules)

Configured in `src/main/resources/application.properties`:

```properties
library.rules.max-active-loans=3
library.rules.loan-duration-days=14
```

Meaning:
- A member can have at most **3 active loans** at a time.
- Due date is calculated as **borrowedAt + 14 days**.

> Note: These rules are **not stored in DB**; they are read from config at runtime.

---

## 4) Security (Basic Auth)

This service uses **Basic Authentication** with predefined users.

| User | Password | Roles |
|------|----------|-------|
| admin | admin123 | ROLE_ADMIN |
| member | member123 | ROLE_MEMBER |

Authorization rules (high level):
- **ADMIN** can create/update/delete books and members.
- **MEMBER** can read books, borrow/return books, and view their loans (depending on endpoint policy).

---

## 5) Response Format (BaseResponse)

All endpoints return a consistent wrapper:

```json
{
  "responseMessage": "SUCCESS",
  "responseData": { }
}
```

On error, the API responds with:
```json
{
  "responseMessage": "Some error message"
}
```

Null fields are excluded (`NON_NULL`).

---

## 6) Database & Migrations (Liquibase)

Liquibase is enabled automatically on startup:

- Master changelog:
  - `src/main/resources/db/changelog/db.changelog-master.yaml`
- Initial schema changeset:
  - `src/main/resources/db/changelog/001-init.sql` (Liquibase formatted SQL)

Liquibase will also create:
- `databasechangelog`
- `databasechangeloglock`

---

## 7) API Documentation (Swagger)

Open Swagger UI:
- `http://localhost:8080/swagger-ui.html`

You can also access the OpenAPI JSON:
- `http://localhost:8080/v3/api-docs`

---

## 8) End-to-End (E2E) cURL Commands (Postman-friendly)

These commands are designed for **step-by-step** E2E testing.  
You can paste each cURL into Postman (Import → Raw text) or create requests manually.

### Base URL
```bash
BASE_URL="http://localhost:8080"
```

### Authentication
- ADMIN: `-u admin:admin123`
- MEMBER: `-u member:member123`

---

### Step 1 — Health check (optional)
```bash
curl -i -u admin:admin123   "$BASE_URL/actuator/health"
```

---

### Step 2 — Create a Member (ADMIN)
```bash
curl -i -u admin:admin123   -H "Content-Type: application/json"   -d '{
    "name": "Budi Santoso",
    "email": "budi.santoso@example.com"
  }'   "$BASE_URL/api/members"
```

✅ Save the `responseData.id` from the response as `MEMBER_ID` (example: `1`).

---

### Step 3 — Create a Book (ADMIN)
```bash
curl -i -u admin:admin123   -H "Content-Type: application/json"   -d '{
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "9780132350884",
    "totalCopies": 3
  }'   "$BASE_URL/api/books"
```

✅ Save the `responseData.id` as `BOOK_ID` (example: `1`).

---

### Step 4 — List Books (MEMBER) and check `availableCopies`
```bash
curl -i -u member:member123   "$BASE_URL/api/books"
```

---

### Step 5 — Borrow a Book (MEMBER)
Replace `memberId` and `bookId` with the IDs you created.

```bash
curl -i -u member:member123   -H "Content-Type: application/json"   -d '{
    "memberId": 1,
    "bookId": 1
  }'   "$BASE_URL/api/loans/borrow"
```

✅ Save the `responseData.id` as `LOAN_ID` (example: `10`).

---

### Step 6 — Verify the book `availableCopies` decreased
```bash
curl -i -u member:member123   "$BASE_URL/api/books/1"
```

---

### Step 7 — List loans by member (verify `returnedAt` is absent/null)
```bash
curl -i -u member:member123   "$BASE_URL/api/loans?memberId=1"
```

---

### Step 8 — Return the book
```bash
curl -i -u member:member123   -X POST   "$BASE_URL/api/loans/10/return"
```

---

### Step 9 — Verify the book `availableCopies` increased again
```bash
curl -i -u member:member123   "$BASE_URL/api/books/1"
```

---

## 9) Negative Scenarios (Rules)

### 9.1 Borrow when stock is empty
1) Create a book with `totalCopies = 1`
2) Borrow it once (success)
3) Borrow it again (should fail)

Create book:
```bash
curl -i -u admin:admin123   -H "Content-Type: application/json"   -d '{
    "title": "Refactoring",
    "author": "Martin Fowler",
    "isbn": "9780201485677",
    "totalCopies": 1
  }'   "$BASE_URL/api/books"
```

Borrow #1:
```bash
curl -i -u member:member123   -H "Content-Type: application/json"   -d '{"memberId":1,"bookId":2}'   "$BASE_URL/api/loans/borrow"
```

Borrow #2 (expected error):
```bash
curl -i -u member:member123   -H "Content-Type: application/json"   -d '{"memberId":1,"bookId":2}'   "$BASE_URL/api/loans/borrow"
```

---

### 9.2 Max active loans per member
If `library.rules.max-active-loans=3`, then borrowing a 4th active loan should fail.

Borrow multiple different books until reaching the limit, then attempt one more.

---

### 9.3 Overdue loans block borrowing
A member cannot borrow new books if they have at least one overdue active loan.

For E2E testing, you can temporarily set a very small duration:

```properties
library.rules.loan-duration-days=1
```

Then:
1) Borrow a book
2) Wait until it becomes overdue (past dueDate), or adjust system time in test env
3) Try to borrow another book → expected error

> In automated tests, this is verified deterministically using a controlled `Clock`.

---

## 10) Tests & Coverage (Mockito + JaCoCo)

### Run unit + integration tests
```bash
mvn test
```

### Generate JaCoCo coverage report
```bash
mvn verify
```

Open:
- `target/site/jacoco/index.html`

---

## 11) Observability

### Health
```bash
curl -i -u admin:admin123   "$BASE_URL/actuator/health"
```

### Prometheus metrics
```bash
curl -i -u admin:admin123   "$BASE_URL/actuator/prometheus"
```

---

## 12) Postman Tips (optional)

Recommended Postman setup:
- Create collection variables:
  - `baseUrl = http://localhost:8080`
  - `memberId`, `bookId`, `loanId`
- Use Authorization tab:
  - Basic Auth for ADMIN folder and MEMBER folder
- In Postman “Tests” script after create/borrow, store IDs:
```javascript
pm.collectionVariables.set("memberId", pm.response.json().responseData.id);
pm.collectionVariables.set("bookId", pm.response.json().responseData.id);
pm.collectionVariables.set("loanId", pm.response.json().responseData.id);
```

Then you can reference variables in request bodies:
```json
{ "memberId": {{memberId}}, "bookId": {{bookId}} }
```

---

## License
For assignment / evaluation use.
---

## Additional Microservice: Library Analytics Service (Kafka + Redis + Elasticsearch)

To demonstrate **Kafka / stream-based processing**, **Redis caching**, **Elasticsearch (non-relational DB)**, and **advanced native SQL**, this repository includes a second service:

- **Service name:** `library-analytics-service`
- **Port:** `8081`
- **Consumes Kafka topic:** `library.loan-events`
- **Caches:** Redis sorted-set (`analytics:top-books`) for “Top Borrowed Books”
- **Indexes/searches:** Elasticsearch index `loan-events`
- **Runs advanced native SQL:** window function query for overdue member ranking (Postgres)

### Run both services locally (Docker)
From the repository root:
```bash
docker compose up --build
```

### Useful endpoints (Analytics Service)
Swagger UI:
- http://localhost:8081/swagger-ui.html

Health:
- http://localhost:8081/actuator/health

Top borrowed books (Redis + Java Streams):
```bash
curl -u member:member123 "http://localhost:8081/api/analytics/top-books?limit=10"
```

Search loan events (Elasticsearch):
```bash
curl -u member:member123 "http://localhost:8081/api/search/loan-events?page=0&size=20"
curl -u member:member123 "http://localhost:8081/api/search/loan-events?bookId={{BOOK_ID}}"
curl -u member:member123 "http://localhost:8081/api/search/loan-events?memberId={{MEMBER_ID}}"
```

Overdue members ranking (Advanced Native SQL + window function):
```bash
curl -u admin:admin123 "http://localhost:8081/api/reports/overdue-members?limit=10"
```

### Event flow (Kafka)
Whenever a member **borrows** or **returns** a book in the main service, the Book Lending Service publishes a Kafka event:
- `BORROWED` when borrowing succeeds
- `RETURNED` when a return succeeds

The analytics service consumes these events and updates Redis + Elasticsearch in near real-time.
