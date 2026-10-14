# VoyaAI — Spring Boot & PostgreSQL Backend Service

This directory contains the Spring Boot REST backend for **VoyaAI Travel Planner & Price Optimizer**.

---

## 1. Architecture & Tech Stack

* **Language:** Java 21+ (Tested on Java 25 LTS)
* **Framework:** Spring Boot (Spring Web, Spring Data JPA, Jakarta Validation)
* **Database:** PostgreSQL 17
* **Build Tool:** Maven (via included Maven Wrapper `mvnw.cmd` / `mvnw`)
* **ORM:** Hibernate / JPA (Auto-DDL schema migration)

---

## 2. Package Structure

```
com.voyaai.backend/
├── BackendApplication.java       # Spring Boot main entry point
├── config/
│   └── CorsConfig.java           # Centralized CORS configuration (React dev origins)
├── controller/
│   ├── HealthController.java     # Health check endpoint (/api/health)
│   └── UserController.java       # User REST controller (/api/users)
├── dto/
│   ├── HealthResponse.java       # Health response model
│   ├── UserRequest.java          # User creation input with Jakarta Validation
│   └── UserResponse.java         # Safe user projection (never exposes password)
├── entity/
│   └── User.java                 # JPA Entity mapped to PostgreSQL "users" table
├── repository/
│   └── UserRepository.java       # Spring Data JPA repository
├── service/
│   └── UserService.java          # Transactional business logic
└── exception/
    ├── GlobalExceptionHandler.java # @RestControllerAdvice for clean JSON error responses
    ├── ResourceNotFoundException.java # 404 handler
    └── ErrorResponse.java        # Structured error response contract
```

---

## 3. Environment Variables & Configuration

The application reads configuration from environment variables with safe developer defaults in `src/main/resources/application.properties`:

| Variable | Description | Default Value |
|---|---|---|
| `DB_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/voyaai` |
| `DB_USERNAME` | PostgreSQL database user | `postgres` |
| `DB_PASSWORD` | PostgreSQL user password | `postgres` |
| `SERVER_PORT` | Spring Boot server port | `8080` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed frontend origins | `http://localhost:5173,http://localhost:5174,http://localhost:3000` |

An example file is provided at [`backend/.env.example`](file:///c:/Users/MAB12/Downloads/project/backend/.env.example). Never commit real database passwords or secrets to Git.

---

## 4. How to Run Locally

### Step 1: Start PostgreSQL

Run the helper script from the project directory:

```powershell
.\backend\scripts\start-postgres.bat
```

Or manually using the included PostgreSQL binaries:

```powershell
& '.\pgsql\pgsql\bin\pg_ctl.exe' -D '.\pgsql\data' -l '.\pgsql\server.log' start
```

To stop PostgreSQL later:

```powershell
.\backend\scripts\stop-postgres.bat
```

---

### Step 2: Start Spring Boot Backend

From the `backend` directory, run:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The server starts on `http://localhost:8080`.

---

### Step 3: Start React Frontend

From the project root:

```powershell
npm run dev
```

The frontend will run on `http://localhost:5173`. When loaded in the browser, the frontend automatically calls `GET /api/health` and displays the live **Spring Boot + PostgreSQL: Connected** status pill in the bottom-left corner!

---

## 5. Verification Commands

### 1. Test Health Endpoint
```bash
curl -X GET http://localhost:8080/api/health
```

Expected Response:
```json
{
  "status": "ok",
  "service": "VoyaAI backend"
}
```

### 2. Create a User (PostgreSQL Persistence)
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name": "Traveler Demo", "email": "traveler@voyaai.com", "password": "password123"}'
```

Expected Response (`201 Created`):
```json
{
  "id": 1,
  "name": "Traveler Demo",
  "email": "traveler@voyaai.com",
  "createdAt": "2026-10-03T16:09:30"
}
```

### 3. List All Users
```bash
curl -X GET http://localhost:8080/api/users
```

### 4. Test 404 Error Handling
```bash
curl -X GET http://localhost:8080/api/users/999
```

Expected Response (`404 Not Found`):
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "User not found with id: 999",
  "path": "/api/users/999"
}
```
