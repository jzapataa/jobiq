# Jobiq

Jobiq is a mobile-first job discovery product built around a swipe-first experience for candidates and a simple publishing/applicant flow for recruiters.

**Status:** V1 in development. This repository currently contains only the Slice 0 technical foundation; business functionality is intentionally not implemented yet.

## Architecture

```text
Jobiq Mobile (React Native + Expo + TypeScript)
                    |
                    | HTTPS / REST
                    v
Jobiq Backend (Spring Boot modular monolith)
                    |
                    | JPA / JDBC
                    v
               PostgreSQL
```

The repository is a monorepo:

```text
backend/                  Spring Boot API foundation
mobile/                   Expo mobile foundation
docs/                     Product, architecture and deployment documentation
.github/workflows/        Monorepo CI
docker-compose.yml        Local PostgreSQL only
```

## Technical baseline

### Backend

- Java 25 LTS
- Spring Boot 4.1.1
- Maven Wrapper
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Bean Validation
- Spring Boot Actuator
- JUnit + Testcontainers

### Mobile

- Node 24 LTS
- Expo SDK 57
- React Native 0.86.x
- React 19.2.3
- TypeScript
- Expo Router
- Axios
- Zustand
- Expo SecureStore
- React Native Gesture Handler
- React Native Reanimated
- Expo Font

## Local development

### 1. Start PostgreSQL

```bash
docker compose up -d postgres
docker compose ps
```

Local-only credentials:

```text
database: jobiq
username: jobiq
password: jobiq-local
port: 5432
```

These credentials are intentionally local development values only. DEV and PROD credentials are never committed.

### 2. Start the backend

```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

The API listens on `http://localhost:8080` by default.

Liveness:

```text
GET http://localhost:8080/actuator/health/liveness
```

### 3. Start the mobile app

```bash
cd mobile
cp .env.example .env.local
npm ci
npm start
```

Set `EXPO_PUBLIC_API_URL` for the device/emulator you are using.

## Validation

Backend:

```bash
cd backend
./mvnw verify
```

Mobile:

```bash
cd mobile
npm ci
npm run typecheck
npm run lint
npm test
npx expo-doctor@latest
```

## Git model

```text
feature/*
   |
   v
 develop
   |
   v
  main
```

- `develop`: integration branch
- `main`: stable/release branch
- normal feature work is performed through Pull Requests

## Architecture documents

- `docs/JOBIQ_BLUEPRINT_v0.1_FINAL.md`
- `docs/JOBIQ_TECHNICAL_DESIGN_v0.1_FINAL.md`
- `docs/recovery.md`
- `docs/deployment.md`

## Not implemented yet

Slice 0 does **not** implement registration, login, JWT behavior, users, profiles, companies, jobs, skills, feed, swipes, applications, business REST endpoints, domain Flyway migrations, remote deployments or EAS builds.

See the FINAL Blueprint and Technical Design before implementing later slices.
