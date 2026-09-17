# Product Catalog Service

A small REST microservice for managing a product catalog, built with **Java 21 + Spring Boot 3** and backed by **PostgreSQL**.

Each product has a `name`, `price`, `category`, and `quantity`.

## Design at a glance

- **Layers:** `ProductController` (HTTP) → `ProductService` (business logic) → `ProductRepository` (Spring Data JPA) → PostgreSQL.
- **Explicit bean wiring:** the service is defined in a Java `@Configuration` beans file (`config/BeansConfig.java`) with an `@Bean` method rather than component scanning.
- **DTOs** (`ProductRequest`/`ProductResponse`) keep the JPA entity off the API surface.
- **Validation** via Jakarta Bean Validation on the request DTO.
- **Consistent errors** via a `@RestControllerAdvice` returning a structured `ApiError`.
- **Schema** is owned by `schema.sql` (not Hibernate-generated) so the SQL is explicit and reviewable.

## API

Base path: `/api/v1/products`

| Method | Path                   | Description        | Success | Errors            |
|--------|------------------------|--------------------|---------|-------------------|
| POST   | `/api/v1/products`      | Create a product   | 201     | 400               |
| GET    | `/api/v1/products`      | List / search / page | 200   | —                 |
| GET    | `/api/v1/products/{id}` | Get one product    | 200     | 404               |
| PUT    | `/api/v1/products/{id}` | Update a product   | 200     | 400, 404          |
| DELETE | `/api/v1/products/{id}` | Delete a product   | 204     | 404               |

`201 Created` responses include a `Location` header pointing at the new resource.

`GET /api/v1/products` always returns a paged envelope. Query parameters (all optional):

- `name` — case-insensitive substring match on `name` (e.g. `?name=hammer`).
- `description` — case-insensitive substring match on `description`.
- `page` — zero-based page index (default `0`).
- `size` — page size (default: unbounded, so no params returns everything). Set it to paginate.

`name` and `description` are independent filters; when both are given they combine with **AND**.
Everything combines, e.g. `/api/v1/products?name=hammer&description=heavy&page=0&size=20`.

### Request body (create / update)

```json
{
  "name": "Widget",
  "price": 9.99,
  "category": "tools",
  "quantity": 5
}
```

### Validation rules

- `name` — non-empty
- `price` — strictly greater than zero
- `category` — non-empty
- `quantity` — zero or positive

Invalid input returns `400` with per-field messages:

```json
{
  "timestamp": "2026-09-16T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": { "price": "price must be greater than zero" }
}
```

## Run with Docker (one command)

```bash
docker compose up --build
```

This starts PostgreSQL and the service together. The API is available at `http://localhost:8080`.

If you changed source and the container still runs old code (cached build layers, reused containers), force a clean rebuild:

```bash
docker compose down
docker compose build --no-cache app
docker compose up --force-recreate
```

Quick check:

```bash
curl -s -X POST http://localhost:8080/api/v1/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Widget","price":9.99,"category":"tools","quantity":5}'

curl -s http://localhost:8080/api/v1/products
```

## Run locally (without Docker)

Requires JDK 21, Maven, and a running PostgreSQL.

Start a matching PostgreSQL (skip if you already have one):

```bash
docker run --name catalog-db -p 5432:5432 \
  -e POSTGRES_DB=catalog \
  -e POSTGRES_USER=catalog \
  -e POSTGRES_PASSWORD=catalog \
  -d postgres:16-alpine
```

Then run the app:

```bash
# Point at your database (defaults shown, must match the DB above)
export DB_URL=jdbc:postgresql://localhost:5432/catalog
export DB_USER=catalog
export DB_PASSWORD=catalog

mvn spring-boot:run
```

## Tests

```bash
mvn test
```

- `ProductServiceTest` — unit tests of the service logic with Mockito (create, not-found, update, delete guard).
- `ProductApiIntegrationTest` — full-stack test (controller + validation + JPA) against in-memory H2, covering status codes and the create→get→update→delete lifecycle.

Tests need no external database.

## Build the jar

```bash
mvn clean package
java -jar target/product-catalog-1.0.0.jar
```
