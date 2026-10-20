# TaskFlow — Project Management

Full-stack starter project: React + TypeScript + Vite, Java 17 + Spring Boot 3, Spring Security, JWT, BCrypt, Spring Data JPA, MySQL.

## Included
- Register/login with email uniqueness, BCrypt password hashing, JWT bearer authentication.
- Per-user project and task isolation (each project/task is scoped to its owner).
- Project CRUD and task CRUD REST APIs.
- Dashboard summary API.
- Frontend login/register, dashboard, projects, Kanban tasks, light/dark theme.
- MySQL schema in `database/schema.sql`.
- Docker Compose for local MySQL.

> Scope note: This is a runnable MVP foundation. Project memberships/invitations, comments, checklist, notification delivery and file storage are represented in the roadmap, not fully implemented. Do not expose it publicly before adding rate limits, refresh-token strategy, production secrets, validation tests and secure file storage.

## Requirements
- JDK 17+
- Maven 3.9+
- Node.js 20+
- MySQL 8+ (or Docker Desktop)

## 1. Configure MySQL
Option A — Docker:
```bash
docker compose up -d mysql
```
Option B — local MySQL:
```sql
CREATE DATABASE taskflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'taskflow'@'localhost' IDENTIFIED BY 'change_me';
GRANT ALL PRIVILEGES ON taskflow.* TO 'taskflow'@'localhost';
```
The schema is created by Hibernate for local development. `database/schema.sql` is also provided as a reference/manual setup script.

## 2. Start backend
```bash
cd backend
```
PowerShell:
```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/taskflow?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME="taskflow"
$env:DB_PASSWORD="change_me"
$env:JWT_SECRET="change-this-secret-to-a-random-32-byte-or-longer-secret"
mvn spring-boot:run
```
Bash:
```bash
export DB_URL='jdbc:mysql://localhost:3306/taskflow?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME=taskflow
export DB_PASSWORD=change_me
export JWT_SECRET='change-this-secret-to-a-random-32-byte-or-longer-secret'
mvn spring-boot:run
```
Backend: `http://localhost:8080`

## 3. Start frontend
In another terminal:
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173`.

## API overview
- `POST /api/auth/register` `{ "fullName":"Demo User", "email":"demo@example.com", "password":"Password123!" }`
- `POST /api/auth/login` `{ "email":"demo@example.com", "password":"Password123!" }`
- `GET /api/auth/me` (Bearer token)
- `GET|POST /api/projects`
- `GET|PUT|DELETE /api/projects/{id}`
- `GET|POST /api/tasks`
- `GET|PUT|DELETE /api/tasks/{id}`
- `GET /api/dashboard/summary`

All protected requests use `Authorization: Bearer <token>`. Project/task endpoints intentionally return only the signed-in user's own records.

## Production checklist
- Store secrets in a secret manager, not source control.
- Use HTTPS, restrictive CORS, database migrations (Flyway), request validation and integration tests.
- Add role/membership-based access before collaboration features.
- Store uploads outside the web root, validate content type/size and scan files.
- Add notification delivery, audit logging, pagination and rate limiting.
