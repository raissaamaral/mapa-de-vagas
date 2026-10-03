# Mapa de Vagas

A web application to organize my own job search: track applications, the stages of each hiring process, and metrics about the search.

I'm building it as a real tool for my own use and as a hands-on way to learn web development, with a focus on the backend, using a professional workflow: issues, branches, pull requests, tests and incremental releases.

> 🚧 **Status:** V0 (backend) and authentication complete. Frontend in progress; deployment next.

## Features

- Create, view, update and delete job applications
- Quick save: only company and job title are required, so a job can be saved in seconds and completed later
- Status tracking (`SAVED`, `APPLIED`, `IN_PROGRESS`, `OFFER`, `HIRED`, `REJECTED`, `WITHDRAWN`, `NO_RESPONSE`) with a full history of every change
- `appliedOn` is filled automatically when an application moves to `APPLIED`
- Filters by status, source and work model, which can be combined
- Saved jobs view ordered by application deadline
- Multiple users: each user only sees and changes their own applications

## Tech stack

- **Backend:** Java 21, Spring Boot 4 (Web, Data JPA, Validation, Security)
- **Frontend:** React, TypeScript, Vite, Tailwind CSS, shadcn/ui
- **Authentication:** JWT (HS256) in an HttpOnly cookie, validated by Spring Security's OAuth2 Resource Server
- **Database:** PostgreSQL 17, with schema versioned by Flyway
- **Tests:** JUnit 5, Mockito, MockMvc, Spring Security Test, integration tests against PostgreSQL
- **Infrastructure:** Docker Compose for the local database

## Architecture

A monorepo with two parts:

- **`backend/`:** a layered monolith
    - **Controller:** receives HTTP requests, validates input and returns responses
    - **Service:** business rules (status history, automatic dates, ownership)
    - **Repository:** data access with Spring Data JPA
- **`frontend/`:** a single-page application in React that talks to the API

Backend code is organized by feature: `application/` (job applications and status history) and `user/` (accounts and authentication endpoints), with cross-cutting concerns in `security/` and `exception/`.

## API

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/register` | Create an account |
| `POST` | `/auth/login` | Log in; sets the token in an HttpOnly cookie |
| `POST` | `/auth/logout` | Log out; clears the cookie |
| `GET` | `/auth/me` | Current user (requires login) |

### Applications

All endpoints below require login and only access the current user's applications.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/applications` | Create an application (starts as `SAVED`) |
| `GET` | `/applications` | List applications, with optional filters: `status`, `source`, `workModel` |
| `GET` | `/applications/saved` | Saved jobs ordered by deadline |
| `GET` | `/applications/{id}` | View one application |
| `PUT` | `/applications/{id}` | Update an application (status is not changed here) |
| `DELETE` | `/applications/{id}` | Delete an application |
| `PATCH` | `/applications/{id}/status` | Change the status, recording it in the history |
| `GET` | `/applications/{id}/history` | Status history in chronological order |

Errors follow a consistent format:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "company": "must not be blank"
  }
}
```

## Running locally

**Prerequisites:** Java 21, Docker and Node.js 24 LTS (installing via [nvm](https://github.com/nvm-sh/nvm) is recommended).

### Backend

```bash
# 1. create your local environment file and generate a JWT signing key
cp .env.example .env
openssl rand -base64 32   # paste the result as JWT_SECRET in .env

# 2. start the database
docker compose up -d

# 3. run the application
cd backend
./mvnw spring-boot:run

# 4. run the tests (the database must be running)
./mvnw test
```

The API runs at `http://localhost:8080`. The application refuses to start without a valid `JWT_SECRET`.

To try the API with `curl`, register a user, then store the login cookie in a file outside the project:

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "you@example.com", "password": "your-password"}'

curl -c /tmp/cookies.txt -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "you@example.com", "password": "your-password"}'

curl -b /tmp/cookies.txt http://localhost:8080/applications
```

### Frontend

With the backend running:

```bash
cd frontend
npm install
npm run dev
```

The app runs at `http://localhost:5173`. Other scripts: `npm run build` (type-checks and builds for production into `dist/`) and `npm run lint` (ESLint).

## Technical decisions

- **Flyway instead of auto-generated schema:** every database change is a versioned SQL migration, and Hibernate only validates that entities match the tables.
- **Status changes only through a dedicated endpoint:** creating and updating never change the status, so the history is always complete. Status and history are saved in a single transaction.
- **DTOs instead of exposing entities:** clients can only send the fields they are allowed to set.
- **Free status transitions:** any status can move to any other, so mistakes can be fixed.
- **JWT in an HttpOnly cookie instead of `localStorage`:** JavaScript cannot read the token, which reduces the impact of XSS.
- **Ownership enforced inside the queries:** applications are always looked up by id *and* owner, so another user's data is simply not found.
- **JPA Specifications for filters:** optional filters are combined in code, always including the owner.
- **Dev proxy instead of CORS in development:** Vite forwards `/api/*` to the backend, so the browser sees a single origin and the auth cookie works as it will in production. The `/api` prefix keeps frontend routes from colliding with API endpoints.
- **Monolith:** the simplest architecture that fits the problem; the layered structure keeps it organized as it grows.

## Security

Security is treated as a requirement from the start:

- Passwords hashed with BCrypt; never stored, logged or returned
- JWT signed with a 256-bit key from an environment variable; the algorithm, expiration and issuer are validated by Spring Security
- Token stored in an `HttpOnly`, `SameSite=Strict` cookie (`Secure` in production)
- Every endpoint is closed by default; only registration, login and logout are public
- The user id always comes from the validated token, never from the request
- Accessing another user's application returns `404`, without revealing that it exists
- Login returns the same error, in the same time, for an unknown email and a wrong password
- DTOs prevent mass assignment; job URLs accept only `http`/`https` (prevents `javascript:` links)
- Size limits on all text fields; parameterized queries only
- Error responses never expose stack traces or library exception messages
- The local database is bound to `127.0.0.1` only; secrets live in Git-ignored `.env` files

**Required before deployment:** CSRF tokens (with the frontend) and rate limiting of login and registration.

## Roadmap

- [x] **V0:** backend: applications, status history, filters, tests
- [x] **Authentication:** multiple users, each one seeing only their own data
- [ ] **Frontend:** React + TypeScript (in progress)
- [ ] **Before deployment:** rate limiting, CSRF tokens
- [ ] **Deployment:** Docker, cloud hosting, CI/CD with GitHub Actions
- [ ] Later: kanban view, password reset by email, companies and hiring stages, resume versions, dashboard and metrics, AI-assisted job description analysis

## Development workflow

Each feature starts as an issue, is developed in its own branch with [Conventional Commits](https://www.conventionalcommits.org/), and is merged through a pull request with a description of decisions and security considerations.