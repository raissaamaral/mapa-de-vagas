# Mapa de Vagas

A web application to organize my own job search: track applications, the stages of each hiring process, and metrics about the search.

I'm building it as a real tool for my own use and as a hands-on way to learn backend development with a professional workflow: issues, branches, pull requests, tests and incremental releases.

> 🚧 **Status:** V0 (backend) complete. Next: authentication, frontend and deployment.

## Features

- Create, view, update and delete job applications
- Quick save: only company and job title are required, so a job can be saved in seconds and completed later
- Status tracking (`SAVED`, `APPLIED`, `IN_PROGRESS`, `OFFER`, `HIRED`, `REJECTED`, `WITHDRAWN`, `NO_RESPONSE`) with a full history of every change
- `appliedOn` is filled automatically when an application moves to `APPLIED`
- Filters by status, source and work model, which can be combined
- Saved jobs view ordered by application deadline

## Tech stack

- **Backend:** Java 21, Spring Boot 4 (Web, Data JPA, Validation)
- **Database:** PostgreSQL 17, with schema versioned by Flyway
- **Tests:** JUnit 5, Mockito, MockMvc
- **Infrastructure:** Docker Compose for the local database

## Architecture

A layered monolith:

- **Controller:** receives HTTP requests, validates input and returns responses
- **Service:** business rules (status history, automatic dates, lookups)
- **Repository:** data access with Spring Data JPA

Code is organized by feature (`application/`), with cross-cutting error handling in `exception/`.

## API

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

**Prerequisites:** Java 21 and Docker.

```bash
# start the database
docker compose up -d

# run the application
cd backend
./mvnw spring-boot:run

# run the tests (the database must be running)
./mvnw test
```

The API runs at `http://localhost:8080`.

## Technical decisions

- **Flyway instead of auto-generated schema:** every database change is a versioned SQL migration, and Hibernate only validates that entities match the tables.
- **Status changes only through a dedicated endpoint:** creating and updating never change the status, so the history is always complete. Status and history are saved in a single transaction.
- **DTOs instead of exposing entities:** clients can only send the fields they are allowed to set.
- **Free status transitions:** any status can move to any other, so mistakes can be fixed.
- **Monolith:** the simplest architecture that fits the problem; the layered structure keeps it organized as it grows.

## Security

Security is treated as a requirement from the start:

- DTOs prevent mass assignment (clients cannot set `id` or `status`)
- Job URLs accept only `http`/`https`, preventing `javascript:` links (XSS)
- Size limits on all text fields
- Parameterized queries only (Spring Data JPA)
- Error responses never expose stack traces or library exception messages
- The local database is bound to `127.0.0.1` only
- Authentication is required before any public deployment (planned)

## Roadmap

- [x] **V0:** backend: applications, status history, filters, tests
- [ ] **Authentication:** multiple users, each one seeing only their own data
- [ ] **Frontend:** React + TypeScript, including a kanban view
- [ ] **Deployment:** Docker, cloud hosting, CI/CD with GitHub Actions
- [ ] Later: companies and hiring stages, resume versions, dashboard and metrics, AI-assisted job description analysis

## Development workflow

Each feature starts as an issue, is developed in its own branch with [Conventional Commits](https://www.conventionalcommits.org/), and is merged through a pull request with a description of decisions and security considerations.