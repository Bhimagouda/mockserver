# Mock Server

A Spring Boot application that hosts configurable WireMock stubs for payment scenarios.

| Port | Service |
|------|---------|
| 9090 | Spring admin API (manage stubs, verify requests) |
| 9091 | WireMock server (payment stubs hit by the app under test) |

---

## Running with Docker

### Build and start

```bash
docker compose up --build
```

### Start in background

```bash
docker compose up --build -d
```

### Stop and remove container

```bash
docker compose down
```

### Rebuild after code changes

```bash
docker compose up --build
```

---

## Running locally (without Docker)

```bash
./mvnw spring-boot:run
```

---

## Admin API — quick reference

### Activate a payment scenario

```bash
curl -X POST http://localhost:9090/admin/mocks/payment/success
curl -X POST http://localhost:9090/admin/mocks/payment/failure
curl -X POST http://localhost:9090/admin/mocks/payment/timeout
curl -X POST http://localhost:9090/admin/mocks/payment/unauthorized
curl -X POST http://localhost:9090/admin/mocks/payment/rate-limit
```

### Check which scenario is active

```bash
curl http://localhost:9090/admin/mocks/payment/active
```

### Verify received requests

```bash
# All captured requests to /bank/payment
curl http://localhost:9090/admin/mocks/payment/requests

# How many times /bank/payment was called
curl http://localhost:9090/admin/mocks/payment/count
```

### Reset

```bash
# Clear request history only (stubs stay active)
curl -X DELETE http://localhost:9090/admin/mocks/requests

# Clear everything — stubs and history
curl -X DELETE http://localhost:9090/admin/mocks/reset
```

---

## Pointing your application at this mock server

The application under test must send payment calls to the **WireMock port (9091)**, not the admin port.

### Running your app locally against a local container

Set the payment service base URL in your app's config:

```yaml
# application-test.yaml (or equivalent)
payment:
  base-url: http://localhost:9091
```

### Running your app in Docker Compose alongside this server

Add your app as a second service in `docker-compose.yml` and use the service name as the hostname:

```yaml
services:
  mock-server:
    build: .
    container_name: mock-server
    ports:
      - "9090:9090"
      - "9091:9091"

  app-under-test:
    image: your-app-image
    environment:
      PAYMENT_BASE_URL: http://mock-server:9091
    depends_on:
      - mock-server
```

Docker's internal DNS resolves `mock-server` to the container's IP, so inter-container calls reach WireMock without going through the host machine.

### Running your app in CI

Use the host machine's IP or `host.docker.internal` (Docker Desktop) if the app runs outside Docker:

```bash
PAYMENT_BASE_URL=http://host.docker.internal:9091 ./run-tests.sh
```

---

## Typical test flow

```
1. POST /admin/mocks/payment/success      ← set the scenario before the test
2. <trigger your service's checkout flow>
3. GET  /admin/mocks/payment/count        ← assert exactly 1 downstream call
4. GET  /admin/mocks/payment/requests     ← assert correct request body / headers
5. DELETE /admin/mocks/requests           ← clean up before the next test
```
