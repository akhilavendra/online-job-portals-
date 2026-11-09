# Online Job Portal

A responsive job portal built with Java 21, Spring Boot, Thymeleaf, Spring Security, MySQL, and Maven. Job seekers can register, maintain an education and skills profile, upload a resume, search openings, apply, and track application status. Administrators can publish and maintain listings and update candidate statuses.

## Requirements

- Java 21 or newer
- Maven 3.9+
- Docker and Docker Compose for the containerized local setup
- MySQL 8 for a standalone local or production database

## Run locally

The easiest local setup runs MySQL and the application in Docker. Create a local environment file from the blank template and enter unique development passwords before starting:

```bash
cp .env.example .env
# Set distinct DB_PASSWORD and MYSQL_ROOT_PASSWORD values, plus ADMIN_PASSWORD, in .env.
docker compose up --build
```

Open [http://localhost:8080](http://localhost:8080). `.env` is ignored by Git and Docker build context; never commit it. The local Compose stack binds MySQL to loopback and disables DB TLS only for its isolated development setup.

Compose enables five sample jobs for local testing. They are inserted only when the jobs table is empty, so restarting the app does not duplicate them. Sample seeding is disabled by default outside Compose; keep `SEED_SAMPLE_JOBS=false` in production.

To run the application directly with Maven, start MySQL and set its connection values first:

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=job_portal
export DB_USER=portal
export DB_PASSWORD='your-local-password'
export DB_SSL_MODE=DISABLED
export DB_ALLOW_PUBLIC_KEY_RETRIEVAL=true
export SESSION_COOKIE_SECURE=false
export ADMIN_EMAIL=admin@example.com
export ADMIN_PASSWORD='change-this-password'
mvn spring-boot:run
```

The optional admin account is created on startup only when both `ADMIN_EMAIL` and `ADMIN_PASSWORD` are set. Passwords are stored with BCrypt. Registration always creates a job-seeker account; public registration cannot assign administrator privileges.

## Features

- Email/password sign-in, BCrypt password hashing, CSRF protection, and role-based authorization
- Registration with email, mobile number, and password validation
- Candidate profile with professional headline, skills, location, and education details
- Private resume upload, download, replacement, and removal (PDF, DOC, or DOCX, up to 5 MB)
- Job search by keyword/skill, location, and employment type
- Job detail pages with company, role, location, salary, and required skills
- Duplicate-safe applications and candidate application status tracking
- Administrator dashboard for creating, editing, deactivating, and deleting jobs and updating applications
- MySQL persistence, local Docker Compose database, and environment-based configuration

The sample roles cover different skills, locations, and work types so keyword, location, and work-type filtering can be tried immediately.

Uploaded resumes are stored on the application filesystem. In production, mount persistent storage and set `UPLOAD_DIR` to its mount path. The database stores only the generated resume filename.

## Test and package

The automated suite uses H2 in MySQL compatibility mode; it does not require a running MySQL server.

```bash
mvn test
mvn package
```

Tests cover Spring/database startup, registration and login, email and phone validation, job search filtering, application rules, and resume upload checks. `docker compose up --build` additionally verifies that the application can connect to the MySQL service. For a production release, also test against the same MySQL provider/version used by deployment.

## Configuration

| Variable | Purpose | Default |
| --- | --- | --- |
| `PORT` | HTTP port | `8080` |
| `DATABASE_URL` | Full JDBC URL, if preferred | Built from `DB_*` values |
| `DB_HOST` | MySQL host | `localhost` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | Database name | `job_portal` |
| `DB_USER` | Database user | `portal` |
| `DB_PASSWORD` | Database password | Required |
| `DB_SSL_MODE` | MySQL Connector/J TLS mode | `VERIFY_IDENTITY` |
| `DB_ALLOW_PUBLIC_KEY_RETRIEVAL` | MySQL auth key retrieval; local development only | `false` |
| `DDL_AUTO` | Hibernate schema mode | `update` |
| `UPLOAD_DIR` | Resume storage directory | `./uploads` |
| `ADMIN_EMAIL` | Optional first administrator email | Empty (disabled) |
| `ADMIN_PASSWORD` | Optional first administrator password | Empty (disabled) |
| `SESSION_COOKIE_SECURE` | Set secure cookie flag behind HTTPS | `true` |

Use a dedicated least-privilege database user and strong secrets. The app defaults MySQL connections to `sslMode=VERIFY_IDENTITY` and secure session cookies. The local Compose stack explicitly overrides these for its isolated development setup. Keep database and admin credentials in the hosting provider's secret environment settings, never in source control. For schema changes beyond this starter project's `DDL_AUTO=update` setup, add and review database migrations before upgrading production.

## Docker image

The Dockerfile builds a Java 21 executable jar in a Maven stage and runs it in a minimal Java runtime as a non-root user. Resume files are written to `/var/data/uploads` in the image and should be backed by a persistent volume in deployment.

```bash
docker build -t online-job-portal .
docker run --rm -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_NAME=job_portal \
  -e DB_USER=portal \
  -e DB_PASSWORD='your-password' \
  -e DB_SSL_MODE=DISABLED \
  -e DB_ALLOW_PUBLIC_KEY_RETRIEVAL=true \
  -e SESSION_COOKIE_SECURE=false \
  online-job-portal
```

## Deploy to Render

Render deploys the application from the included Dockerfile. Render does not currently provide a managed MySQL database, so provision MySQL with an external managed provider (or your own MySQL server) and allow the Render service to connect to it. Do not replace MySQL with a different database if MySQL is a project requirement.

1. Review `git status --short`; ensure `.env` and other secret files are not staged. `.gitignore` excludes local env files, and `.dockerignore` excludes them from the image build context.
2. Push this repository to GitHub.
3. Create a Render Web Service from the GitHub repository and select the Docker runtime.
4. Provision an external MySQL database. Render does not currently provide managed MySQL. Allow the Render service to connect and use a dedicated database user.
5. In Render environment settings, set `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD` from the provider. Keep `DB_SSL_MODE=VERIFY_IDENTITY` when the provider supports certificate/hostname verification. Alternatively, set `DATABASE_URL` to a complete `jdbc:mysql://...` URL containing the provider's TLS options, and set `DB_USER` and `DB_PASSWORD` separately.
6. Set `ADMIN_EMAIL` and `ADMIN_PASSWORD` as Render secrets. The bootstrap account is created on first successful database startup. Do not set `SEED_SAMPLE_JOBS=true` in production.
7. Add a persistent Render disk mounted at `/var/data`, then set `UPLOAD_DIR=/var/data/uploads`. Without persistent storage, resumes may disappear when the service restarts or redeploys.
8. Keep `SESSION_COOKIE_SECURE=true` (the default), `DB_ALLOW_PUBLIC_KEY_RETRIEVAL=false`, and set `DDL_AUTO=update` for this starter schema. Configure the Render health check path as `/login`; Render supplies `PORT`.
9. Deploy, open the generated `https://<service-name>.onrender.com` address, register a job-seeker account, and sign in to the admin account to publish a listing.

The live URL is generated by Render after an account, GitHub repository, MySQL service, and deployment are configured; it cannot be produced from this source workspace alone.

## Project layout

```text
src/main/java/com/example/jobportal/
  config/       Spring Security configuration
  model/        JPA entities and enums
  repository/   Spring Data repositories
  service/      Account, job, application, and resume rules
  web/          MVC controllers and validated forms
src/main/resources/
  templates/    Thymeleaf views
  static/css/   Responsive application styles
src/test/       Unit and integration tests using H2
```