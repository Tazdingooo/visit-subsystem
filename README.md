# MR Visit Subsystem

Small Spring Boot app for tracking pharmaceutical rep visits to hospitals.

- **Plan** a visit (HCP, product, department, planned time)
- **Check in / check out** with GPS (typed manually to simulate device capture)
- **Call report** after each visit (talking points, feedback, materials distributed)
- **Compliance**: flags visits under 5 minutes and check-outs more than 500 m from the hospital
- **Monthly dashboard** of visit counts per product (Chart.js, vendored locally)

Stack: Java 21, Spring Boot 3.5, Spring Data JPA, Flyway, H2 (embedded file DB), Thymeleaf. No external database.

## Run with Docker (recommended)

```bash
docker compose up --build
```

Open http://localhost:8080. The H2 database is persisted in the `visit-subsystem-data` volume, so data survives `docker compose down`.

Stop with `docker compose down` (add `-v` to also delete the database volume).

## Run locally

Requires JDK 21 (the Maven wrapper is included, no separate Maven needed).

```bash
./mvnw spring-boot:run
```

Open http://localhost:8080. The local run uses `./data/visitdb` and enables the H2 console at http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/visitdb`, user `sa`, empty password).

Run the tests:

```bash
./mvnw test
```

## Endpoints

| Page | Path |
|------|------|
| Monthly dashboard | `/` |
| Visit list | `/visits` |
| Plan a visit | `/visits/new` |
| Visit detail | `/visits/{id}` |

REST API under `/api`: `GET /api/visits`, `POST /api/visits`, `GET /api/visits/{id}`, `POST /api/visits/{id}/check-in`, `POST /api/visits/{id}/check-out`, `POST /api/visits/{id}/report`, `GET /api/hcps`, `GET /api/products`.

## Configuration

| Property | Default | Meaning |
|----------|---------|---------|
| `visit.compliance.min-duration-seconds` | `300` | Minimum compliant visit duration |
| `visit.compliance.max-hospital-distance-meters` | `500` | Maximum allowed check-out distance from hospital |
| `visit.time-zone` | `Asia/Shanghai` | Zone for month bucketing and displayed timestamps |

The `docker` profile (`application-docker.yml`) sets an absolute DB path under `/app/data` and disables the H2 console.
