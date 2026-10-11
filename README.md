# CertifyMe Backend

Spring Boot REST API for the CertifyMe student and certificate management application.

## Technology stack

- Java 25
- Spring Boot 4
- Spring Web
- Spring Data JPA and Hibernate
- MySQL Connector/J
- Maven Wrapper

Automated tests use H2 through the test profile; the application runtime uses MySQL.

## Prerequisites

- JDK 25
- MySQL 8.0 or compatible, with a database and application account provisioned
- PowerShell on Windows (commands below use the Maven Wrapper for Windows)

## Database and environment configuration

The application listens on port `8081`. Configure these environment variables
before starting it:

| Variable | Purpose | Default |
| --- | --- | --- |
| `CERTIFYME_DB_URL` | JDBC connection URL | `jdbc:mysql://localhost:3306/certifyme_db` |
| `CERTIFYME_DB_USERNAME` | MySQL application username | `certifyme_user` |
| `CERTIFYME_DB_PASSWORD` | MySQL application password | Required; no default |

For the local MySQL database, provision `certifyme_db` and an account with only
the privileges the application needs. In PowerShell, set the values in the
terminal session before running the application. Replace the password
placeholder locally; do not put the actual password in a tracked file:

```powershell
$env:CERTIFYME_DB_URL = "jdbc:mysql://localhost:3306/certifyme_db"
$env:CERTIFYME_DB_USERNAME = "certifyme_user"
$env:CERTIFYME_DB_PASSWORD = "<your-local-MySQL-password>"
```

The JPA schema setting is `update`, so Hibernate updates the schema without
requesting destructive recreation. Back up data before applying database or
schema changes.

## Setup and run

From this directory, run the backend tests and start Spring Boot:

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

The test suite uses its H2 test configuration and does not require connecting
to or modifying the local MySQL database.

## API

The local API base URL is `http://localhost:8081/api`; the frontend defaults to
this URL. The configured local CORS origin is `http://localhost:5173`.

Main routes:

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/dashboard` | Dashboard statistics |
| GET, POST | `/api/students` | List or create students |
| GET, PUT, DELETE | `/api/students/{id}` | Read, update, or delete a student |
| GET, POST | `/api/certificates` | List or issue certificates |
| GET, PUT, DELETE | `/api/certificates/{id}` | Read, update, or delete a certificate |
| PUT | `/api/certificates/{id}/revoke` | Revoke a certificate |
| GET | `/api/certificates/verify/{certificateId}` | Verify a certificate |

A student with associated certificates cannot be deleted; the API returns
`409 Conflict` to preserve certificate history.
