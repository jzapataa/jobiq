# JOBIQ — TECHNICAL DESIGN v0.1 FINAL

**Date:** 10 September 2026  
**Target repository:** `jzapataa/jobiq`  
**Status:** FINAL — APPROVED BASELINE  
**Finalizes:** `JOBIQ — TECHNICAL DESIGN v0.2 DRAFT`

> Source of truth: **JOBIQ — PRODUCT BLUEPRINT v0.1 FINAL**
>
> This document preserves the locked V1 product and architecture decisions from the Blueprint and the valid technical decisions from Technical Design v0.1.
>
> This FINAL baseline incorporates the approved content of Technical Design v0.2 DRAFT, which closes the remote deployment architecture, environment strategy, zero-cost hosting policy, CI/CD model, runtime resource baseline and production demo Definition of Done.

Conventions:

- **LOCKED FROM BLUEPRINT** — inherited product/architecture decision.
- **LOCKED DEPLOYMENT POLICY v0.1 FINAL** — explicit V1 deployment constraint introduced for this revision.
- **TECHNICAL DECISION PROPOSED** — implementation decision introduced or refined by this Technical Design.
- **OPEN TECHNICAL DECISION** — unresolved technical question blocking finalization.

---

## 1. Executive Summary

### 1.1 Product architecture

**LOCKED FROM BLUEPRINT**

Jobiq V1 is a mobile-first job discovery application based on swipe interaction with exactly two roles:

```text
CANDIDATE
RECRUITER
```

Target architecture:

```text
┌───────────────────────────────┐
│         Jobiq Mobile          │
│ React Native + Expo + TS      │
└──────────────┬────────────────┘
               │ HTTPS / REST
               ▼
┌───────────────────────────────┐
│         Jobiq Backend         │
│ Spring Boot Modular Monolith  │
└──────────────┬────────────────┘
               │ JPA / JDBC
               ▼
┌───────────────────────────────┐
│          PostgreSQL           │
└───────────────────────────────┘
```

No microservices, gateway, service discovery, message broker or distributed persistence is introduced.

### 1.2 Environment model

**TECHNICAL DECISION PROPOSED**

V1 now has four distinct environments:

```text
LOCAL
TEST
DEV
PRODUCTION
```

```text
LOCAL
Mobile local
    |
Backend local
    |
PostgreSQL Docker
```

```text
TEST
GitHub Actions / test runtime
    |
Spring tests
    |
PostgreSQL Testcontainers
```

```text
DEV
Mobile DEV
    |
Render DEV API
    |
Neon DEV PostgreSQL
```

```text
PRODUCTION
Mobile PROD demo build
    |
Render PROD API
    |
Neon PROD PostgreSQL
```

`PRODUCTION` in this document means the **public Jobiq portfolio/demo environment**. It does not imply commercial-production SLA requirements.

### 1.3 Deployment provider validation

The provider proposal has been verified against current provider documentation as of 10 September 2026.

| Component | Provider | Plan | Verdict |
|---|---|---|---|
| Backend DEV | Render | Free Web Service | PASS WITH ELIGIBILITY GUARD |
| Backend PROD | Render | Free Web Service | PASS WITH ELIGIBILITY GUARD |
| Database DEV | Neon | Free | PASS |
| Database PROD | Neon | Free | PASS |
| Mobile builds | Expo / EAS | Free | PASS |

Render's standard free deployment flow states that no payment is required. Free services receive a shared monthly free allowance, sleep after inactivity and, when no payment method is present, service/build capabilities may be suspended instead of creating an automatic paid overage.

There is one important Render caveat: some accounts can be prompted for card details as an additional account-verification measure. Therefore Render passes at the **platform/design level**, but Jobiq has an explicit provisioning guard: if the actual Jobiq account is asked to provide payment-card details, **no card will be provided and the provider decision must be reopened**.

Neon's Free plan is zero-cost, has no time-limited trial requirement and does not require a credit card for the Free plan. It provides sufficient project capacity to isolate DEV and PROD.

Expo/EAS provides a Free plan with fixed cloud-build quotas. Free accounts cannot simply roll into automatic paid overages; once the relevant free quota is exhausted, builds stop until quota reset or another build method is used.

### 1.4 V1 hosting baseline

**TECHNICAL DECISION PROPOSED**

```text
Backend
  Render Free Web Service
  Docker runtime
  Java 25 LTS
  Spring Boot 4.1.1

Database
  Neon Free
  PostgreSQL 18
  independent DEV + PROD projects

Mobile
  Expo SDK 57
  EAS Free
  Android internal APK for portfolio/demo
```

The core architecture remains portable:

```text
Spring Boot Docker
+
standard PostgreSQL
+
Expo
```

No provider-specific SDK is introduced into the Jobiq business architecture.

---

## 2. Architecture Overview

### 2.1 Macro architecture

**LOCKED FROM BLUEPRINT**

```text
Mobile
  |
  | REST/JSON + JWT Bearer
  |
  v
Backend
  |
  | single transactional boundary
  |
  v
PostgreSQL
```

No internal REST calls between business modules.

No separate databases per module.

### 2.2 Backend modules

**TECHNICAL DECISION PROPOSED — unchanged from v0.1**

```text
com.jobiq
|
|-- auth
|-- users
|-- candidates
|-- recruiters
|-- companies
|-- skills
|-- jobs
|-- feed
|-- swipes
|-- applications
`-- shared
```

Dependency direction:

```text
auth ---------> users
  |               ^
  +--> candidates |
  +--> recruiters |

candidates -----> skills

recruiters -----> companies

jobs -----------> recruiters
 |--------------> companies
 `--------------> skills

applications ---> jobs
 `--------------> candidates

swipes ---------> candidates
 |--------------> jobs
 `--------------> applications

feed -----------> candidates
 |--------------> jobs
 `--------------> swipes

all modules ----> shared
```

Circular dependencies are not allowed.

### 2.3 Deployment topology

```text
                     ┌────────────────────┐
                     │      GitHub        │
                     │ jzapataa/jobiq     │
                     └─────────┬──────────┘
                               │
                   ┌───────────┴────────────┐
                   │                        │
               develop                    main
                   │                        │
                   │ CI PASS                │ CI PASS
                   ▼                        ▼
        ┌───────────────────┐     ┌───────────────────┐
        │ Render Free       │     │ Render Free       │
        │ jobiq-api-dev     │     │ jobiq-api-prod    │
        │ Docker / Java 25  │     │ Docker / Java 25  │
        └─────────┬─────────┘     └─────────┬─────────┘
                  │ TLS                     │ TLS
                  ▼                         ▼
        ┌───────────────────┐     ┌───────────────────┐
        │ Neon Free         │     │ Neon Free         │
        │ jobiq-dev         │     │ jobiq-prod        │
        │ PostgreSQL 18     │     │ PostgreSQL 18     │
        └───────────────────┘     └───────────────────┘
```

DEV and PROD do not share:

```text
database
database credentials
JWT secret
JWT issuer
backend environment variables
user/application data
```

---

## 3. Technology Versions

### 3.1 Backend

**TECHNICAL DECISION PROPOSED**

| Technology | Baseline |
|---|---|
| Java | 25 LTS |
| Spring Boot | 4.1.1 |
| Spring Security | Boot-managed |
| Spring Data JPA | Boot-managed |
| Hibernate | Boot-managed |
| Maven Wrapper | 3.9.x |
| PostgreSQL | Major 18 |
| Local/Test PostgreSQL image | 18.6 baseline |
| PostgreSQL JDBC | Boot-managed |
| Flyway | Boot-managed |
| JUnit | Boot-managed |
| Testcontainers | Boot-managed |
| Runtime image | Eclipse Temurin 25 JRE |
| Build image | Eclipse Temurin 25 JDK |

Spring Boot 4.1.1 supports Java 25, so there is no hosting-driven need to downgrade automatically to Java 21.

### PostgreSQL version policy

v0.1 described PostgreSQL as exactly `18.6`.

The FINAL baseline refines this to:

```text
PostgreSQL major version = 18
```

because:

```text
LOCAL / TEST
→ can pin a specific Docker minor such as 18.6

Neon
→ manages PostgreSQL minor updates
```

This is not an architecture change.

### 3.2 Java 25 versus hosting

**TECHNICAL DECISION PROPOSED**

Keep:

```text
Java 25 LTS
Spring Boot 4.1.1
```

There is no technical reason at this stage to downgrade to Java 21.

Render does not need native Java runtime support because Jobiq will use Render's Docker runtime.

Therefore:

```text
Render host runtime
       X
       |
Docker image controls runtime
       |
       v
Eclipse Temurin Java 25
```

### Java 25 deployment verdict

```text
Java 25
+
Spring Boot 4.1.1
+
Docker
+
Render Free

= technically reasonable
```

The principal limitation is Render Free's compute envelope, not Java 25 compatibility.

### 3.3 Mobile

**TECHNICAL DECISION PROPOSED**

| Technology | Baseline |
|---|---|
| Node | 24 LTS |
| Expo | SDK 57 |
| React Native | 0.86.x, Expo-managed |
| React | 19.2.3 |
| TypeScript | Expo-compatible 6.x |
| Expo Router | SDK 57 compatible |
| Expo SecureStore | SDK 57 compatible |
| Axios | 1.x |
| Zustand | 5.x |
| Gesture Handler | SDK 57 compatible |
| Reanimated | SDK 57 compatible |
| Expo Font | SDK 57 compatible |

The clean migration should start from the latest compatible SDK 57 patch, not hard-pin the earlier v0.1 patch combination.

Native Expo dependencies should be installed using:

```text
npx expo install
```

rather than manually forcing incompatible patch versions.

---

## 4. Monorepo Structure

**LOCKED FROM BLUEPRINT**

```text
jzapataa/jobiq
```

**TECHNICAL DECISION PROPOSED — updated for deployment**

```text
jobiq/
|
|-- backend/
|   |-- .mvn/
|   |   `-- wrapper/
|   |
|   |-- mvnw
|   |-- mvnw.cmd
|   |-- pom.xml
|   |
|   |-- Dockerfile
|   |-- .dockerignore
|   |
|   `-- src/
|       |-- main/
|       |   |-- java/com/jobiq/
|       |   |   |-- JobiqApplication.java
|       |   |   |-- auth/
|       |   |   |-- users/
|       |   |   |-- candidates/
|       |   |   |-- recruiters/
|       |   |   |-- companies/
|       |   |   |-- skills/
|       |   |   |-- jobs/
|       |   |   |-- feed/
|       |   |   |-- swipes/
|       |   |   |-- applications/
|       |   |   `-- shared/
|       |   |
|       |   `-- resources/
|       |       |-- application.yml
|       |       |-- application-local.yml
|       |       |-- application-dev.yml
|       |       |-- application-prod.yml
|       |       `-- db/
|       |           `-- migration/
|       |
|       `-- test/
|           |-- java/com/jobiq/
|           `-- resources/
|               `-- application-test.yml
|
|-- mobile/
|   |-- assets/
|   |   `-- fonts/
|   |
|   |-- src/
|   |   |-- app/
|   |   |-- core/
|   |   |-- features/
|   |   |-- components/
|   |   |-- theme/
|   |   `-- types/
|   |
|   |-- package.json
|   |-- package-lock.json
|   |-- tsconfig.json
|   |-- eslint.config.js
|   |-- app.json / app.config.ts
|   |-- eas.json
|   `-- .env.example
|
|-- docs/
|   |-- JOBIQ_BLUEPRINT_v0.1_FINAL.md
|   |-- JOBIQ_TECHNICAL_DESIGN_v0.1_FINAL.md
|   |-- recovery.md
|   `-- deployment.md
|
|-- .github/
|   `-- workflows/
|       `-- ci.yml
|
|-- docker-compose.yml
|-- .gitignore
`-- README.md
```

### Render-specific infrastructure file

**TECHNICAL DECISION PROPOSED**

Do **not** introduce `render.yaml` in V1.

Reason:

```text
only 2 backend services
+
Render can connect directly to GitHub
+
Dockerfile is already portable
+
service config can be documented
```

Render-specific service configuration will live in Render's dashboard and be documented without secrets in:

```text
docs/deployment.md
```

This keeps Jobiq easier to move away from Render if its Free policy changes.

---

## 5. Backend Module Boundaries

Unchanged from v0.1.

### `users`

Owns:

```text
User
UserRole
UserRepository
```

No public user CRUD in V1.

Role is immutable once registered.

### `auth`

Owns:

```text
registration
login
password hashing
JWT generation/validation
/auth/me
```

Depends on `users` and read-only profile status from candidate/recruiter modules.

### `candidates`

Owns:

```text
CandidateProfile
CandidateSkill
```

Uses `userId` instead of a cross-module JPA `User` graph.

### `skills`

Owns:

```text
Skill
SkillRepository
SkillResolver
```

### `companies`

Owns:

```text
Company
```

### `recruiters`

Owns:

```text
RecruiterProfile
```

### `jobs`

Owns:

```text
Job
JobSkill
JobStatus
WorkMode
ExperienceLevel
```

### `swipes`

Owns:

```text
Swipe
SwipeDecision
```

Also orchestrates:

```text
LIKE
→ Swipe
→ Application
```

inside one transaction.

### `applications`

Owns:

```text
Application
ApplicationStatus
```

### `feed`

Owns no persistence entity.

Its purpose is the candidate feed query.

### `shared`

Allowed:

```text
security principal
common error infrastructure
configuration
request IDs
Clock/time abstraction
small pagination primitives
```

Not allowed:

```text
business entities
generic repositories
business services
large generic utility dumping ground
```

---

## 6. Backend Internal Structure

**TECHNICAL DECISION PROPOSED — unchanged**

Package by business feature first.

Example:

```text
jobs/
|
|-- controller/
|-- dto/
|-- service/
|-- domain/
`-- repository/
```

Concrete example:

```text
jobs/
|
|-- controller/
|   `-- RecruiterJobController.java
|
|-- dto/
|   |-- CreateJobRequest.java
|   |-- UpdateJobRequest.java
|   `-- JobResponse.java
|
|-- service/
|   |-- JobService.java
|   `-- JobReadService.java
|
|-- domain/
|   |-- Job.java
|   |-- JobSkill.java
|   |-- JobStatus.java
|   |-- WorkMode.java
|   `-- ExperienceLevel.java
|
`-- repository/
    `-- JobRepository.java
```

No interface-per-class rule.

No full Clean Architecture.

No full Hexagonal Architecture.

No MapStruct unless mapping complexity later justifies it.

---

## 7. Domain Model

Unchanged from v0.1.

### User

```text
User
- id
- email
- passwordHash
- name
- role
- createdAt
- updatedAt
```

```text
UserRole
- CANDIDATE
- RECRUITER
```

### CandidateProfile

```text
CandidateProfile
- id
- userId
- headline
- location
- bio
- linkedinUrl?
- githubUrl?
- portfolioUrl?
- createdAt
- updatedAt
```

### RecruiterProfile

```text
RecruiterProfile
- id
- userId
- companyId
- position
- createdAt
- updatedAt
```

### Company

```text
Company
- id
- name
- description
- website?
- logoUrl?
- location
- createdAt
- updatedAt
```

### Skill

```text
Skill
- id
- name
- normalizedName
```

### CandidateSkill

```text
CandidateSkill
- candidateProfileId
- skillId
```

### Job

```text
Job
- id
- title
- description
- companyId
- createdByRecruiterId
- location
- workMode
- salaryMin?
- salaryMax?
- currency?
- experienceLevel
- status
- createdAt
- updatedAt
- closedAt?
```

Salary representation:

```text
NO SALARY

salaryMin = null
salaryMax = null
currency = null
```

or:

```text
SALARY RANGE

salaryMin != null
salaryMax != null
currency != null
salaryMin <= salaryMax
```

No partial salary combination.

### Swipe

```text
Swipe
- id
- candidateId
- jobId
- decision
- createdAt
```

```text
LIKE
DISLIKE
```

Immutable in V1.

### Application

```text
Application
- id
- candidateId
- jobId
- status
- createdAt
```

```text
APPLIED
```

Immutable in V1.

---

## 8. PostgreSQL Data Model

### 8.1 ID strategy

**TECHNICAL DECISION PROPOSED — unchanged**

Use UUID for domain IDs:

```text
PostgreSQL uuid
Java UUID
REST UUID string
```

IDs are generated application-side/JPA-side.

### 8.2 Tables

| Table | Purpose | Important constraints |
|---|---|---|
| `users` | identity/auth | unique email |
| `candidate_profiles` | candidate profile | unique user |
| `companies` | company | name/location required |
| `recruiter_profiles` | recruiter profile | unique user, company FK |
| `skills` | canonical skills | unique normalized name |
| `candidate_skills` | candidate-skill N:M | composite PK |
| `jobs` | job offers | ownership/status constraints |
| `job_skills` | job-skill N:M | composite PK |
| `swipes` | candidate decision | candidate/job unique |
| `applications` | candidate application | candidate/job unique |

### 8.3 `users`

```text
id UUID PK
email VARCHAR(320) NOT NULL UNIQUE
password_hash VARCHAR(100) NOT NULL
name VARCHAR(120) NOT NULL
role VARCHAR(20) NOT NULL
created_at TIMESTAMPTZ NOT NULL
updated_at TIMESTAMPTZ NOT NULL
```

Constraint:

```text
role IN ('CANDIDATE', 'RECRUITER')
```

Email normalized before persistence.

### 8.4 `candidate_profiles`

```text
id UUID PK
user_id UUID NOT NULL UNIQUE FK users(id)
headline VARCHAR(160) NOT NULL
location VARCHAR(120) NOT NULL
bio VARCHAR(1200) NOT NULL
linkedin_url VARCHAR(500)
github_url VARCHAR(500)
portfolio_url VARCHAR(500)
created_at TIMESTAMPTZ NOT NULL
updated_at TIMESTAMPTZ NOT NULL
```

### 8.5 `companies`

```text
id UUID PK
name VARCHAR(160) NOT NULL
description VARCHAR(1500) NOT NULL
website VARCHAR(500)
logo_url VARCHAR(500)
location VARCHAR(120) NOT NULL
created_at TIMESTAMPTZ NOT NULL
updated_at TIMESTAMPTZ NOT NULL
```

No `UNIQUE(name)`.

### 8.6 `recruiter_profiles`

```text
id UUID PK
user_id UUID NOT NULL UNIQUE
company_id UUID NOT NULL
position VARCHAR(120) NOT NULL
created_at TIMESTAMPTZ NOT NULL
updated_at TIMESTAMPTZ NOT NULL
```

### 8.7 `skills`

```text
id UUID PK
name VARCHAR(80) NOT NULL
normalized_name VARCHAR(80) NOT NULL UNIQUE
```

### 8.8 `candidate_skills`

```text
candidate_profile_id UUID NOT NULL
skill_id UUID NOT NULL

PK(candidate_profile_id, skill_id)
```

### 8.9 `jobs`

```text
id UUID PK
title VARCHAR(160) NOT NULL
description VARCHAR(5000) NOT NULL
company_id UUID NOT NULL
created_by_recruiter_id UUID NOT NULL
location VARCHAR(120) NOT NULL
work_mode VARCHAR(20) NOT NULL
salary_min NUMERIC(12,2)
salary_max NUMERIC(12,2)
currency CHAR(3)
experience_level VARCHAR(20) NOT NULL
status VARCHAR(20) NOT NULL
created_at TIMESTAMPTZ NOT NULL
updated_at TIMESTAMPTZ NOT NULL
closed_at TIMESTAMPTZ
```

Checks:

```text
work_mode IN ('REMOTE','HYBRID','ONSITE')

experience_level IN (
    'JUNIOR',
    'MID',
    'SENIOR',
    'LEAD'
)

status IN ('OPEN','CLOSED')

salary_min >= 0
salary_max >= 0
salary_min <= salary_max
```

And:

```text
OPEN
→ closed_at IS NULL

CLOSED
→ closed_at IS NOT NULL
```

### 8.10 `job_skills`

```text
job_id UUID NOT NULL
skill_id UUID NOT NULL

PK(job_id, skill_id)
```

### 8.11 `swipes`

```text
id UUID PK
candidate_id UUID NOT NULL
job_id UUID NOT NULL
decision VARCHAR(20) NOT NULL
created_at TIMESTAMPTZ NOT NULL
```

Critical:

```text
UNIQUE(candidate_id, job_id)
```

```text
decision IN ('LIKE','DISLIKE')
```

### 8.12 `applications`

```text
id UUID PK
candidate_id UUID NOT NULL
job_id UUID NOT NULL
status VARCHAR(20) NOT NULL
created_at TIMESTAMPTZ NOT NULL
```

```text
UNIQUE(candidate_id, job_id)

status = 'APPLIED'
```

Application additionally references the corresponding Swipe pair:

```text
FK(candidate_id, job_id)
→ swipes(candidate_id, job_id)
```

This ensures:

```text
Application
→ requires Swipe
```

The stronger invariant:

```text
Application
→ requires Swipe decision LIKE
```

is enforced by transactional application logic, not a PostgreSQL trigger.

### 8.13 Indexes

```text
users(email)

jobs(created_by_recruiter_id, created_at DESC)

jobs(status, created_at DESC, id)

swipes(candidate_id, job_id) UNIQUE

applications(candidate_id, job_id) UNIQUE

applications(candidate_id, created_at DESC)

applications(job_id, created_at DESC)

candidate_skills(candidate_profile_id)

job_skills(job_id)
```

Feed-specific partial index:

```text
jobs(created_at DESC, id)
WHERE status = 'OPEN'
```

### 8.14 Data lifecycle

No account-deletion API is included in V1.

Closing a Job is a state transition:

```text
OPEN
→ CLOSED
```

not physical deletion.

Existing:

```text
Swipes
Applications
```

are preserved after Job closure.

No destructive cascade from Company/Recruiter/Job should accidentally remove applications.

Default FK behavior for core domain entities should therefore be:

```text
RESTRICT / NO ACTION
```

Join tables can use controlled cascade deletion from their direct aggregate where technically appropriate, but no V1 public endpoint performs destructive profile/job deletion.

### 8.15 ER diagram

```text
USERS
 | 1
 |
 +------ 0..1 CANDIDATE_PROFILES
 |                 |
 |                 +--< CANDIDATE_SKILLS >-- SKILLS
 |
 `------ 0..1 RECRUITER_PROFILES ----> COMPANIES
                     |
                     |
                     `---- creates ----> JOBS
                                         |
                                         +--< JOB_SKILLS >-- SKILLS
                                         |
               CANDIDATE_PROFILE -------+------ SWIPES
                       |                 |
                       `-----------------+------ APPLICATIONS
```

---

## 9. JPA Mapping Strategy

Unchanged from v0.1.

Use explicit IDs across module boundaries rather than building one giant JPA object graph.

Prefer:

```java
UUID userId;
UUID companyId;
UUID candidateId;
UUID jobId;
```

over unnecessary cross-module:

```java
@ManyToOne
private User user;
```

Goals:

```text
avoid circular relationships
avoid accidental loads
avoid N+1
reduce module coupling
avoid serialization problems
```

### Fetching

Mapped associations default to:

```text
LAZY
```

No EAGER collections.

### HTTP boundary

```text
HTTP
 ↓
Request DTO
 ↓
Service
 ↓
Entity / Repository
 ↓
Response DTO
 ↓
HTTP
```

Never return JPA entities directly.

### JPA runtime policy

```text
spring.jpa.open-in-view=false
spring.jpa.hibernate.ddl-auto=validate
```

Flyway owns schema creation and changes.

---

## 10. Flyway Strategy

**LOCKED FROM BLUEPRINT**

Flyway is mandatory.

Initial migration sequence remains:

```text
V1__create_users.sql
V2__create_candidate_and_recruiter_profiles.sql
V3__create_skills.sql
V4__create_jobs.sql
V5__create_swipes.sql
V6__create_applications.sql
V7__create_query_indexes.sql
```

Applied migrations are immutable.

Schema changes require new migrations.

```text
ddl-auto=create
ddl-auto=create-drop
```

are prohibited outside throwaway experiments.

---

## 11. Authentication Design

Unchanged except for environment isolation.

### 11.1 Registration

```text
POST /api/v1/auth/register
```

Flow:

```text
normalize email
↓
validate
↓
verify uniqueness
↓
BCrypt
↓
User
```

Registration does not automatically create a session.

```text
Register
→ Login
```

remains the V1 flow.

### 11.2 Password hashing

```text
BCryptPasswordEncoder
strength = 12
```

No plaintext passwords.

No reversible password encryption.

### 11.3 JWT

Access token only.

No refresh token.

Algorithm:

```text
HS256
```

Claims for DEV:

```json
{
  "sub": "<user UUID>",
  "role": "CANDIDATE",
  "iss": "jobiq-api-dev",
  "iat": "...",
  "exp": "..."
}
```

PROD uses:

```text
iss = jobiq-api-prod
```

Do not store unnecessary profile data inside JWT.

### 11.4 JWT TTL

Default:

```text
PT8H
```

No refresh token.

Expired token:

```text
401
→ SecureStore clear
→ unauthenticated
→ login
```

### 11.5 Environment isolation

**TECHNICAL DECISION PROPOSED**

DEV:

```text
JWT_SECRET = DEV-specific random secret
JWT_ISSUER = jobiq-api-dev
```

PROD:

```text
JWT_SECRET = independent PROD random secret
JWT_ISSUER = jobiq-api-prod
```

Therefore a DEV token cannot be accepted by PROD even if accidentally sent there.

### 11.6 Spring Security

```text
STATELESS
Bearer JWT
CSRF disabled for REST Bearer API
```

Public:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
GET  /actuator/health/liveness
```

All business endpoints otherwise require authentication.

---

## 12. Authorization & Ownership

Unchanged.

| Operation | Candidate | Recruiter |
|---|---|---|
| Own Candidate Profile | R/W | No |
| Applicant profile | Own only | Through application to own Job |
| Recruiter Profile | No | Own R/W |
| Company | No | Own linked company |
| Feed | Yes | No |
| Swipe | Yes | No |
| My Applications | Yes | No |
| Create Job | No | Yes |
| Edit Job | No | Own created Jobs only |
| Close Job | No | Own created Jobs only |
| My Jobs | No | Yes |
| Applicants | No | Own created Jobs only |

Ownership:

```text
job.createdByRecruiterId
==
authenticatedRecruiterProfile.id
```

Same-company membership does not grant ownership over another recruiter's Job.

---

## 13. REST API Contract

API prefix:

```text
/api/v1
```

Content:

```text
application/json
```

IDs:

```text
UUID strings
```

Dates:

```text
ISO-8601
UTC
```

Enums:

```text
UPPERCASE
```

### 13.1 Authentication

#### Register

```text
POST /api/v1/auth/register
```

Role:

```text
PUBLIC
```

Request:

```json
{
  "email": "candidate@example.com",
  "password": "...",
  "name": "Candidate",
  "role": "CANDIDATE"
}
```

Statuses:

```text
201 CREATED
400 VALIDATION_ERROR
409 EMAIL_ALREADY_EXISTS
```

#### Login

```text
POST /api/v1/auth/login
```

Response:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresAt": "..."
}
```

Statuses:

```text
200
400
401 INVALID_CREDENTIALS
```

#### Current User

```text
GET /api/v1/auth/me
```

Response:

```json
{
  "id": "...",
  "email": "...",
  "name": "...",
  "role": "CANDIDATE",
  "profileComplete": true
}
```

### 13.2 Candidate Profile

#### Get

```text
GET /api/v1/candidate/profile
```

Statuses:

```text
200
401
403
404 PROFILE_NOT_FOUND
```

#### Create/update

```text
PUT /api/v1/candidate/profile
```

Request:

```json
{
  "headline": "Java Software Engineer",
  "location": "Murcia",
  "bio": "...",
  "linkedinUrl": null,
  "githubUrl": "...",
  "portfolioUrl": "...",
  "skills": [
    "Java",
    "Spring Boot",
    "PostgreSQL"
  ]
}
```

Statuses:

```text
201 first creation
200 update
400
401
403
```

PUT replaces the complete mutable CandidateProfile representation.

### 13.3 Candidate Feed

```text
GET /api/v1/candidate/feed?limit=20&cursor=...
```

Response:

```json
{
  "items": [
    {
      "id": "...",
      "title": "Senior Java Developer",
      "company": {
        "id": "...",
        "name": "Example"
      },
      "location": "Murcia",
      "workMode": "HYBRID",
      "salaryMin": 40000,
      "salaryMax": 50000,
      "currency": "EUR",
      "experienceLevel": "SENIOR",
      "skills": [
        "Java",
        "Spring Boot"
      ],
      "description": "...",
      "createdAt": "..."
    }
  ],
  "nextCursor": "..."
}
```

Statuses:

```text
200
401
403
409 PROFILE_INCOMPLETE
```

### 13.4 Swipe

```text
POST /api/v1/candidate/jobs/{jobId}/swipes
```

Request:

```json
{
  "decision": "LIKE"
}
```

LIKE:

```json
{
  "swipeId": "...",
  "decision": "LIKE",
  "applicationId": "...",
  "createdAt": "..."
}
```

DISLIKE:

```json
{
  "swipeId": "...",
  "decision": "DISLIKE",
  "applicationId": null,
  "createdAt": "..."
}
```

Statuses:

```text
201
401
403
404 JOB_NOT_FOUND
409 JOB_CLOSED
409 DUPLICATE_SWIPE
409 PROFILE_INCOMPLETE
```

### 13.5 Candidate Applications

```text
GET /api/v1/candidate/applications?limit=20&cursor=...
```

Returns:

```text
Application
+
Job summary
+
Company summary
```

### 13.6 Recruiter Profile

```text
GET /api/v1/recruiter/profile
PUT /api/v1/recruiter/profile
```

Initial PUT:

```json
{
  "position": "Technical Recruiter",
  "company": {
    "name": "Example Company",
    "description": "...",
    "website": "https://...",
    "logoUrl": null,
    "location": "Murcia"
  }
}
```

First creation:

```text
Company
+
RecruiterProfile
```

occurs in a single transaction.

### 13.7 Recruiter Company

```text
GET /api/v1/recruiter/company
PUT /api/v1/recruiter/company
```

No arbitrary company attachment by ID.

### 13.8 Create Job

```text
POST /api/v1/recruiter/jobs
```

Request:

```json
{
  "title": "Backend Developer",
  "description": "...",
  "location": "Murcia",
  "workMode": "HYBRID",
  "salaryMin": 35000,
  "salaryMax": 45000,
  "currency": "EUR",
  "experienceLevel": "MID",
  "skills": [
    "Java",
    "Spring Boot"
  ]
}
```

Derived server-side:

```text
companyId
createdByRecruiterId
status = OPEN
```

Statuses:

```text
201
400
401
403
409 PROFILE_INCOMPLETE
```

### 13.9 My Jobs

```text
GET /api/v1/recruiter/jobs
```

Filter:

```text
createdByRecruiterId = current recruiter
```

### 13.10 Job Detail

```text
GET /api/v1/recruiter/jobs/{jobId}
```

Ownership required.

### 13.11 Update Job

```text
PUT /api/v1/recruiter/jobs/{jobId}
```

Immutable through this request:

```text
id
companyId
createdByRecruiterId
status
createdAt
closedAt
```

CLOSED is terminal for V1.

### 13.12 Close Job

```text
POST /api/v1/recruiter/jobs/{jobId}/close
```

```text
status = CLOSED
closedAt = now
```

Closing an already closed Job returns its closed representation and is functionally idempotent.

### 13.13 Applicants

```text
GET /api/v1/recruiter/jobs/{jobId}/applicants
```

Requires Job ownership.

Example:

```json
{
  "items": [
    {
      "applicationId": "...",
      "status": "APPLIED",
      "createdAt": "...",
      "candidate": {
        "id": "...",
        "name": "...",
        "headline": "...",
        "location": "...",
        "skills": [
          "Java",
          "Spring Boot"
        ]
      }
    }
  ]
}
```

### 13.14 Applicant Profile

```text
GET /api/v1/recruiter/jobs/{jobId}/applicants/{candidateId}
```

Requires:

```text
recruiter owns job
AND
candidate has Application for job
```

No general recruiter endpoint for arbitrary candidate-profile browsing.

---

## 14. API Error Contract

Unchanged.

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "timestamp": "2026-09-10T17:30:00Z",
  "path": "/api/v1/candidate/profile",
  "requestId": "...",
  "fieldErrors": [
    {
      "field": "headline",
      "message": "must not be blank"
    }
  ]
}
```

| HTTP | Code |
|---:|---|
| 400 | `VALIDATION_ERROR` |
| 400 | `MALFORMED_REQUEST` |
| 401 | `UNAUTHENTICATED` |
| 401 | `INVALID_CREDENTIALS` |
| 403 | `FORBIDDEN` |
| 403 | `OWNERSHIP_VIOLATION` |
| 404 | `RESOURCE_NOT_FOUND` |
| 404 | `PROFILE_NOT_FOUND` |
| 409 | `EMAIL_ALREADY_EXISTS` |
| 409 | `PROFILE_INCOMPLETE` |
| 409 | `DUPLICATE_SWIPE` |
| 409 | `JOB_CLOSED` |
| 409 | `CONFLICT` |
| 500 | `INTERNAL_ERROR` |

Never return:

```text
stack traces
SQL
internal exception class names
passwords
JWTs
credentials
```

---

## 15. Swipe Transaction Design

**LOCKED FROM BLUEPRINT**

```text
LIKE
=
Swipe(LIKE)
+
Application(APPLIED)
```

Atomic.

### Transaction

```text
BEGIN
 |
candidate identified
 |
CandidateProfile resolved
 |
Job loaded
 |
OPEN verified
 |
Swipe inserted
 |
 +--------- decision ----------+
 |                             |
DISLIKE                       LIKE
 |                             |
 |                      Application inserted
 |                             |
 +-------------+---------------+
               |
             COMMIT
```

Failure anywhere:

```text
ROLLBACK
```

### Duplicate requests

Database:

```text
UNIQUE(swipes.candidate_id, swipes.job_id)
```

and:

```text
UNIQUE(applications.candidate_id, applications.job_id)
```

Concurrent request:

```text
A -> insert succeeds
B -> unique conflict
```

B becomes:

```text
409 DUPLICATE_SWIPE
```

### Job close concurrency

Swipe and close operations coordinate at database transaction level.

Conceptually:

```text
Swipe
→ protected Job read lock

Close
→ exclusive Job write lock
```

Outcome is deterministic:

```text
Swipe first
→ swipe/application commits
→ job may close afterwards
```

or:

```text
Close first
→ Job CLOSED
→ swipe rejected with 409
```

No distributed lock.

---

## 16. Feed Design

**LOCKED FROM BLUEPRINT**

```text
Job.status = OPEN
AND
NOT EXISTS Swipe(candidate, job)
```

Order:

```text
createdAt DESC
id DESC
```

### Pagination

Use keyset pagination.

Cursor conceptually:

```text
lastCreatedAt
lastJobId
```

Predicate:

```text
created_at < cursor.createdAt

OR

(
  created_at = cursor.createdAt
  AND id < cursor.id
)
```

Default:

```text
20
```

Maximum:

```text
50
```

No offset pagination.

Backend/database remains authoritative if a stale feed card is later swiped.

---

## 17. Mobile Architecture

Unchanged.

```text
mobile/src/
|
|-- app/
|
|-- core/
|   |-- api/
|   |-- auth/
|   |-- config/
|   |-- errors/
|   `-- storage/
|
|-- features/
|   |-- auth/
|   |-- candidate-profile/
|   |-- recruiter-profile/
|   |-- jobs/
|   |-- feed/
|   |-- swipes/
|   `-- applications/
|
|-- components/
|
|-- theme/
|
`-- types/
```

No Redux.

No TanStack Query in V1 unless later evidence demonstrates that manual server-state handling is becoming materially worse.

Likely global state:

```text
authStore
feed state
```

Forms remain local state where appropriate.

---

## 18. Mobile Navigation

Use stable Expo Router APIs.

```text
src/app/
|
|-- _layout.tsx
|-- index.tsx
|
|-- (public)/
|   |-- _layout.tsx
|   |-- login.tsx
|   `-- register.tsx
|
|-- (candidate)/
|   |-- _layout.tsx
|   |
|   `-- (tabs)/
|       |-- _layout.tsx
|       |-- feed.tsx
|       |-- applications.tsx
|       `-- profile.tsx
|
`-- (recruiter)/
    |-- _layout.tsx
    |
    |-- jobs/
    |   |-- index.tsx
    |   |-- new.tsx
    |   `-- [jobId]/
    |       |-- index.tsx
    |       |-- edit.tsx
    |       `-- applicants/
    |           |-- index.tsx
    |           `-- [candidateId].tsx
    |
    `-- profile.tsx
```

Root routing:

```text
checking
→ loading

unauthenticated
→ public

authenticated + CANDIDATE
→ candidate

authenticated + RECRUITER
→ recruiter
```

Role-specific route groups implement UX guards.

Backend role checks remain the real security boundary.

---

## 19. Mobile Authentication

### State

```text
checking
authenticated
unauthenticated
error
```

Store owns:

```text
status
user
accessToken
bootstrapError
login()
logout()
restoreSession()
invalidateSession()
```

JWT is stored through SecureStore.

The entire Zustand state is not persisted through generic storage.

### Boot

```text
App
 |
checking
 |
SecureStore
 |
 +-- no token ------------------> unauthenticated
 |
 `-- token
       |
     /auth/me
       |
       +-- 200 -----------------> authenticated
       |
       +-- 401
       |     |
       |  clear token
       |     |
       |  unauthenticated
       |
       `-- network / 5xx
             |
           error
             |
        KEEP TOKEN
```

A network/backend failure is not equivalent to invalid credentials.

### Environment-scoped SecureStore

**TECHNICAL DECISION PROPOSED — incorporated in FINAL baseline**

Token keys should be environment-scoped:

```text
jobiq.local.accessToken
jobiq.dev.accessToken
jobiq.prod.accessToken
```

This prevents a DEV session token being restored by a PROD build or vice versa.

Environment information is public configuration, not a secret.

### Logout

```text
SecureStore clear
+
memory token clear
+
user clear
+
feature state clear
+
unauthenticated
```

### 401

Protected API 401:

```text
invalidateSession()
```

No routing command inside Axios.

Routing reacts to auth state.

403 does not logout.

---

## 20. Job Card / Swipe Implementation

Unchanged.

Use:

```text
react-native-gesture-handler
+
react-native-reanimated
```

No dedicated Tinder/swipe library.

Gesture:

```text
Pan
 |
translationX
rotation
LIKE/DISLIKE overlay
```

Conceptual activation:

```text
~25% card width
OR
sufficient horizontal velocity
```

Gesture and explicit buttons call exactly the same action:

```text
submitSwipe(LIKE)
submitSwipe(DISLIKE)
```

While request is in flight:

```text
gesture disabled
buttons disabled
```

No offline write queue.

No local Application treated as authoritative.

---

## 21. Mobile API Client

### Structure

```text
core/api/
|
|-- apiClient.ts
|-- apiError.ts
`-- unauthorizedHandler.ts
```

### Base URL

```text
EXPO_PUBLIC_API_URL
```

### Authorization

Request interceptor:

```text
in-memory accessToken
↓
Authorization: Bearer <token>
```

### Error mapping

Backend contract maps into typed:

```text
ApiError
```

### No unsafe retries

Do not transparently replay:

```text
POST swipe
POST job
PUT profile
PUT job
```

because an HTTP timeout does not prove the backend transaction failed.

### Remote timeout policy

**TECHNICAL DECISION PROPOSED — changed from v0.1**

v0.1 proposed:

```text
Axios timeout = 15 seconds
```

That value is too low for the expected cold start of Render Free.

New baseline:

```text
LOCAL:
15 seconds

DEV:
90 seconds

PROD:
90 seconds
```

Configured through:

```text
EXPO_PUBLIC_API_TIMEOUT_MS
```

This value is public and contains no sensitive information.

Reason:

```text
Render wake
potentially tens of seconds

+
possible Neon wake

+
network overhead
```

A 90-second remote timeout provides enough margin for the expected portfolio cold start.

This is not a keep-alive mechanism.

The real user request wakes the API.

No automatic background pings are added.

---

## 22. Configuration & Environments

### 22.1 Environment definitions

#### LOCAL

Purpose:

```text
developer workstation
fast development loop
```

Runtime:

```text
Mobile     -> local Expo
Backend    -> Maven / IDE
Database   -> Docker Compose PostgreSQL
```

Spring profile:

```text
local
```

No remote provider dependency is required.

#### TEST

Purpose:

```text
automated verification
CI
integration tests
```

Runtime:

```text
Spring tests
+
Testcontainers PostgreSQL
```

No persistent remote TEST database.

No Render TEST service.

No Neon TEST project.

Spring profile:

```text
test
```

#### DEV

Purpose:

```text
remote integration
manual QA
mobile/backend integration
portfolio development verification
```

Topology:

```text
Mobile DEV
   |
   v
Render jobiq-api-dev
   |
   v
Neon jobiq-dev
```

Spring profile:

```text
dev
```

DEV data has no relationship with PROD data.

#### PRODUCTION

Purpose:

```text
public portfolio/demo release
V1 production E2E
```

Topology:

```text
Mobile PROD demo build
   |
   v
Render jobiq-api-prod
   |
   v
Neon jobiq-prod
```

Spring profile:

```text
prod
```

Again, this is a portfolio/demo production environment, not a commercial availability commitment.

### 22.2 Backend environment variables

Common names:

```text
SPRING_PROFILES_ACTIVE

DB_URL
DB_USERNAME
DB_PASSWORD

JWT_SECRET
JWT_TTL
JWT_ISSUER

CORS_ALLOWED_ORIGINS

PORT

DB_POOL_MAX_SIZE
DB_POOL_MIN_IDLE

JAVA_TOOL_OPTIONS
```

DEV:

```text
SPRING_PROFILES_ACTIVE=dev

DB_URL=<Neon DEV JDBC URL>
DB_USERNAME=<DEV role>
DB_PASSWORD=<DEV password>

JWT_SECRET=<DEV secret>
JWT_TTL=PT8H
JWT_ISSUER=jobiq-api-dev

CORS_ALLOWED_ORIGINS=<explicit list or empty>
```

PROD:

```text
SPRING_PROFILES_ACTIVE=prod

DB_URL=<Neon PROD JDBC URL>
DB_USERNAME=<PROD role>
DB_PASSWORD=<PROD password>

JWT_SECRET=<different PROD secret>
JWT_TTL=PT8H
JWT_ISSUER=jobiq-api-prod

CORS_ALLOWED_ORIGINS=<explicit list or empty>
```

Mandatory invariant:

```text
DEV JWT_SECRET != PROD JWT_SECRET
DEV DB credentials != PROD DB credentials
DEV DB != PROD DB
```

No secrets in Git.

---

## 23. Spring Profiles

**TECHNICAL DECISION PROPOSED**

### `application.yml`

Contains common non-sensitive configuration and environment references.

Responsibilities:

```text
server port
JPA baseline
Flyway baseline
Hikari configurable values
JWT configuration references
health endpoint baseline
logging baseline
```

Conceptually:

```text
server.port=${PORT:8080}
server.address=0.0.0.0

jpa.open-in-view=false
jpa.hibernate.ddl-auto=validate

flyway.enabled=true
```

No credentials.

### `application-local.yml`

Contains local-development defaults only.

Conceptual database:

```text
jdbc:postgresql://localhost:5432/jobiq
```

Can use convenient local-only credentials matching Docker Compose.

May enable more developer-friendly logging.

### `application-dev.yml`

Remote DEV profile.

No hardcoded secrets.

Contains only non-sensitive DEV behavior:

```text
production-like JPA behavior
resource limits
logging level
Actuator configuration
```

Credentials resolve from environment variables.

### `application-prod.yml`

Remote PROD profile.

Same schema/runtime rules as DEV but production-oriented logging and safety.

No secret values.

### `application-test.yml`

Lives under:

```text
src/test/resources/
```

Testcontainers supplies database properties dynamically.

Uses a test-only JWT secret that has no relationship to DEV/PROD.

---

## 24. Docker / Local Development

### 24.1 Local Docker

**TECHNICAL DECISION PROPOSED — preserved**

`docker-compose.yml` continues to manage only PostgreSQL during normal local development.

```text
docker compose up -d
        |
        v
PostgreSQL 18.x
```

Backend:

```text
./mvnw spring-boot:run
```

or IDE.

Mobile:

```text
npx expo start
```

There is still no need to run the backend through Docker for every edit/run cycle.

### 24.2 Backend Dockerfile

**TECHNICAL DECISION PROPOSED — incorporated in FINAL baseline**

Add:

```text
backend/Dockerfile
backend/.dockerignore
```

The Dockerfile exists primarily for:

```text
reproducible deployment
Render hosting
provider portability
```

It can also be smoke-tested locally.

#### Multi-stage design

```text
STAGE 1
Temurin 25 JDK
     |
Maven Wrapper
     |
Spring Boot package
     |
executable JAR

        ↓

STAGE 2
Temurin 25 JRE
     |
copy JAR only
     |
run application
```

No Maven installation is required outside the project because the Maven Wrapper is committed.

#### Builder responsibility

Builder stage:

```text
copy Maven wrapper
copy pom.xml
copy source
run Maven package
produce executable Spring Boot JAR
```

Tests do not need to run a second time inside the Render Docker build because Render will only deploy after GitHub CI has passed.

Conceptually:

```text
CI
→ tests

Docker build
→ package with tests skipped
```

This reduces duplicate CPU/build-minute consumption.

#### Runtime image

Use a compact Java 25 JRE image, conceptually:

```text
eclipse-temurin:25-jre-ubi10-minimal
```

The exact patch/digest should be pinned when the Dockerfile is implemented.

#### Startup

Docker image owns startup:

```text
java -jar /app/jobiq.jar
```

`JAVA_TOOL_OPTIONS` supplies JVM resource policy automatically.

No custom Render Docker command unless later necessary.

#### Port

Spring:

```text
server.port=${PORT:8080}
```

The application must bind to:

```text
0.0.0.0
```

Therefore:

```text
LOCAL
PORT missing
→ 8080

RENDER
PORT injected by platform
→ platform value
```

No hardcoded Render-specific port in Java.

#### Secrets

Docker image contains no:

```text
DB password
JWT secret
Neon credentials
.env files
```

Secrets are injected at runtime.

---

## 25. Deployment Architecture

### 25.1 Provider topology

**TECHNICAL DECISION PROPOSED**

```text
                         GitHub
                            |
            +---------------+---------------+
            |                               |
         develop                           main
            |                               |
            | GitHub CI                     | GitHub CI
            |                               |
            v                               v
   CI checks successful            CI checks successful
            |                               |
            v                               v
    Render auto-deploy              Render auto-deploy
            |                               |
            v                               v
┌──────────────────────┐        ┌──────────────────────┐
│ jobiq-api-dev        │        │ jobiq-api-prod       │
│ Render Free          │        │ Render Free          │
│ Docker               │        │ Docker               │
│ Java 25              │        │ Java 25              │
└──────────┬───────────┘        └──────────┬───────────┘
           |                               |
           | TLS PostgreSQL                | TLS PostgreSQL
           v                               v
┌──────────────────────┐        ┌──────────────────────┐
│ Neon project         │        │ Neon project         │
│ jobiq-dev            │        │ jobiq-prod           │
│ PostgreSQL 18        │        │ PostgreSQL 18        │
└──────────────────────┘        └──────────────────────┘
```

### 25.2 Region

**TECHNICAL DECISION PROPOSED**

Use Frankfurt for both backend environments and both Neon projects where the provider supports that region.

```text
Render DEV       Frankfurt
Neon DEV         Frankfurt region

Render PROD      Frankfurt
Neon PROD        Frankfurt region
```

Reason:

```text
API and database geographically close
↓
lower DB latency
↓
less time spent on remote DB calls
```

No private network exists between Render and Neon.

Connections traverse the public network over TLS.

### 25.3 Hostnames

Expected initial API URLs:

```text
DEV
https://jobiq-api-dev.onrender.com

PROD
https://jobiq-api-prod.onrender.com
```

If exact service names are unavailable, the generated provider hostname becomes the configured API URL.

No custom domain is required for V1.

---

## 26. Backend Deployment — Render Free

### 26.1 Services

One Render workspace.

Two Free Web Services:

```text
jobiq-api-dev
jobiq-api-prod
```

Do not create additional workspaces merely to obtain additional quota.

### 26.2 DEV service

```text
Name:
jobiq-api-dev

Repository:
jzapataa/jobiq

Branch:
develop

Root directory:
backend

Runtime:
Docker

Plan:
Free

Region:
Frankfurt

Health check:
GET /actuator/health/liveness

Auto deploy:
After CI Checks Pass
```

### 26.3 PROD service

```text
Name:
jobiq-api-prod

Repository:
jzapataa/jobiq

Branch:
main

Root directory:
backend

Runtime:
Docker

Plan:
Free

Region:
Frankfurt

Health check:
GET /actuator/health/liveness

Auto deploy:
After CI Checks Pass
```

### 26.4 Docker versus native runtime

Decision:

```text
DOCKER
```

not native Java.

Reasons:

```text
Java runtime controlled by Jobiq
reproducibility
provider portability
same artifact model DEV/PROD
Render supports Docker
```

### 26.5 Payment policy

No payment method will be added to the Render workspace.

If the Free service cannot be provisioned without adding a card:

```text
DO NOT ENTER CARD
↓
DEPLOYMENT PROVIDER CONFLICT
↓
reopen backend provider selection
```

No exception.

---

## 27. Render Free Resource Baseline

The backend must remain deliberately light because the selected free hosting tier has constrained CPU and memory.

### 27.1 JVM memory

**TECHNICAL DECISION PROPOSED**

Remote DEV/PROD baseline:

```text
JAVA_TOOL_OPTIONS=
-Xms64m
-Xmx256m
-XX:+UseSerialGC
-XX:+ExitOnOutOfMemoryError
```

Rationale:

Keep sufficient headroom outside the heap for:

```text
metaspace
native memory
threads/stacks
JVM itself
network buffers
container overhead
```

`SerialGC` is appropriate to evaluate for this small, CPU-constrained single-instance workload.

This is a baseline, not a permanent tuning constant.

If profiling demonstrates a problem during implementation, tune from evidence.

Do not move to GraalVM Native Image merely to optimize Free hosting.

### 27.2 Tomcat threads

Default large server thread pools are unnecessary for a portfolio API.

Proposed remote baseline:

```text
server.tomcat.threads.max=20
server.tomcat.threads.min-spare=2
```

No need to provision hundreds of request threads.

### 27.3 Hikari

Proposed:

```text
maximumPoolSize = 3
minimumIdle = 0
connectionTimeout ≈ 10 seconds
idleTimeout ≈ 60 seconds
keepaliveTime = disabled
```

Reasons:

```text
one tiny backend instance
very low application concurrency
Neon scale-to-zero
```

A large DB pool would only waste memory and database connections.

### 27.4 Spring initialization

Do **not** globally enable:

```text
spring.main.lazy-initialization=true
```

initially.

Global lazy initialization can:

```text
hide startup failures
move failures to first request
make first interaction even slower
```

Normal eager initialization is preferred.

### 27.5 Dependency policy

Do not introduce unused:

```text
Spring Cloud
messaging starters
Redis clients
metrics exporters
distributed tracing agents
```

Actuator is the only deployment-specific Spring starter justified in the FINAL baseline.

---

## 28. Cold Start Policy

### COLD START EXPECTED

**LOCKED DEPLOYMENT POLICY v0.1 FINAL**

Render Free may spin down an inactive web service.

Neon Free can also scale database compute to zero when inactive.

Therefore:

```text
Jobiq inactive
     |
     +-- Render asleep
     |
     `-- Neon asleep

First user request
     |
     v
Render wake
     |
Spring Boot available
     |
DB request
     |
Neon wake
     |
response
```

The first request can be materially slower than normal.

This is accepted because V1 is:

```text
portfolio
demo
non-critical
zero-cost
```

No:

```text
external ping service
cron keep-alive
uptime bot
fake traffic
scheduled wake request
```

will be introduced.

If it sleeps:

```text
it sleeps
```

This acceptance is a deliberate cost/availability trade-off.

---

## 29. Neon DEV / PROD

### 29.1 Project separation

**TECHNICAL DECISION PROPOSED**

Use two independent Neon projects, not two branches inside one project:

```text
Neon
|
|-- Project: jobiq-dev
|     |
|     `-- PostgreSQL 18
|
`-- Project: jobiq-prod
      |
      `-- PostgreSQL 18
```

Reasons:

```text
strong environment isolation
independent credentials
independent compute quotas
independent storage quotas
lower blast radius
```

DEV and PROD therefore do **not** share database data or credentials.

### 29.2 DEV

```text
Project:
jobiq-dev

Region:
Frankfurt / Europe Central

PostgreSQL:
18

Role:
DEV-specific generated database role

Database:
jobiq

Connection:
direct PostgreSQL endpoint

TLS:
required
```

### 29.3 PROD

```text
Project:
jobiq-prod

Region:
Frankfurt / Europe Central

PostgreSQL:
18

Role:
PROD-specific generated database role

Database:
jobiq

Connection:
direct PostgreSQL endpoint

TLS:
required
```

No credential is reused between projects.

### 29.4 Connection string

Backend-facing `DB_URL` is a JDBC URL:

```text
jdbc:postgresql://<host>/<database>?sslmode=require
```

Username/password live separately:

```text
DB_USERNAME
DB_PASSWORD
```

No complete connection string containing credentials is committed.

### 29.5 Direct versus pooled Neon connection

**TECHNICAL DECISION PROPOSED**

Use the **direct Neon connection** for Jobiq V1.

Reason:

```text
Render instances = 1 per environment
Hikari max connections = 3
very low application concurrency
Flyway migrations work naturally on direct connections
```

Adding another pool layer provides no meaningful V1 value.

Therefore V1 uses:

```text
Spring/JPA/Hikari
      |
direct Neon endpoint
      |
PostgreSQL
```

If future scale makes connection pressure material:

```text
runtime
→ Neon pooled endpoint

Flyway
→ direct endpoint
```

can be introduced then.

Not now.

### 29.6 Hikari and Neon sleep

```text
minimumIdle=0
idle connections expire
keepalive disabled
```

This avoids Jobiq artificially keeping database connections active solely for pool maintenance.

---

## 30. Neon Free-Tier Constraints

The Neon Free plan is selected because it is not a short-lived trial and is compatible with a no-card, zero-cost portfolio architecture.

Relevant free-tier constraints include:

```text
finite compute quota
finite storage quota
finite network transfer
scale-to-zero after inactivity
finite project/branch limits
```

Quota exhaustion or provider limits are accepted as temporary service unavailability.

The architecture must not automatically upgrade to a paid plan.

### PROD data

PROD starts empty.

No implicit:

```text
demo users
demo jobs
seeded profiles
```

are added by this Technical Design FINAL.

Demo seed data can be separately decided later if genuinely needed.

---

## 31. Mobile Environments

### 31.1 Public configuration

Mobile configuration contains:

```text
EXPO_PUBLIC_APP_ENV
EXPO_PUBLIC_API_URL
EXPO_PUBLIC_API_TIMEOUT_MS
```

Examples:

LOCAL:

```text
EXPO_PUBLIC_APP_ENV=local
EXPO_PUBLIC_API_URL=<local backend>
EXPO_PUBLIC_API_TIMEOUT_MS=15000
```

DEV:

```text
EXPO_PUBLIC_APP_ENV=dev
EXPO_PUBLIC_API_URL=https://jobiq-api-dev.onrender.com
EXPO_PUBLIC_API_TIMEOUT_MS=90000
```

PROD:

```text
EXPO_PUBLIC_APP_ENV=prod
EXPO_PUBLIC_API_URL=https://jobiq-api-prod.onrender.com
EXPO_PUBLIC_API_TIMEOUT_MS=90000
```

These values are not secrets.

Never expose:

```text
JWT_SECRET
DB_URL with credentials
DB_PASSWORD
Render credentials
Neon API keys
Expo account credentials
```

through `EXPO_PUBLIC_*`.

Everything under `EXPO_PUBLIC_*` must be treated as client-visible.

---

## 32. Expo / EAS Deployment

### 32.1 Free plan policy

Use Expo/EAS Free only.

The goal is:

```text
develop
test
generate portfolio/demo builds
```

without introducing a payment method.

If free cloud-build quota is exhausted:

```text
new cloud builds unavailable
↓
wait for reset
OR
build locally where appropriate
```

No paid overage activation.

### 32.2 EAS environments

Map standard EAS environment sets to Jobiq:

```text
EAS development
→ Jobiq DEV API

EAS preview
→ Jobiq DEV API

EAS production
→ Jobiq PROD API
```

### 32.3 Build profiles

Conceptually:

```text
development
- development client
- EAS environment: development
- DEV API
```

```text
preview
- internal distribution
- Android APK
- EAS environment: preview
- DEV API
```

```text
production-demo
- internal distribution
- Android APK
- EAS environment: production
- PROD API
```

`production-demo` is a build profile name, not a new provider environment.

### 32.4 Android portfolio artifact

**TECHNICAL DECISION PROPOSED**

The required zero-cost portfolio binary is:

```text
Android APK
```

using EAS internal distribution or an equivalent zero-cost Android build path.

### 32.5 iOS

An internally distributed iOS binary depends on Apple's own developer-account requirements.

Therefore:

```text
iOS distributable binary
```

is **not required** for Jobiq V1 Definition of Done.

This preserves the zero-cost policy.

### 32.6 Deployment versus Store Publication

These are different concerns.

```text
JOBIQ V1 DEPLOYMENT
=
backend online
+
database online
+
installable Android demo build
```

It does **not** mean:

```text
Apple App Store publication
Google Play Store publication
```

Store publication remains outside V1.

---

## 33. CI/CD Strategy

### 33.1 Git model

Simple integration/release flow:

```text
feature/*
    |
    | PR
    v
 develop
    |
    | release PR
    v
  main
```

No complex GitFlow.

### 33.2 Pull requests

```text
feature/*
→ PR develop
→ CI
→ review
→ merge
```

Develop is the integration branch.

Main is the release branch.

### 33.3 DEV deployment

```text
merge to develop
       |
       v
GitHub Actions CI
       |
       | PASS
       v
Render detects CI success
       |
       v
jobiq-api-dev deploy
       |
       v
Flyway against jobiq-dev
```

### 33.4 PROD deployment

```text
develop
   |
release PR
   |
   v
main
   |
GitHub Actions CI
   |
   | PASS
   v
Render detects CI success
   |
   v
jobiq-api-prod deploy
   |
   v
Flyway against jobiq-prod
```

### 33.5 Deployment trigger

**TECHNICAL DECISION PROPOSED**

Prefer Render's native branch-based auto-deploy after successful GitHub checks.

Therefore Jobiq does **not** need:

```text
Render API key in GitHub
deployment webhook scripts
custom deploy GitHub Action
Docker registry
GHCR image push
```

This is simpler and reduces secret/configuration surface.

### 33.6 Backend CI

```text
checkout
setup Java 25
Maven cache
./mvnw -B verify
```

Runs:

```text
compile
unit tests
integration tests
```

### 33.7 Mobile CI

```text
checkout
setup Node 24
npm ci
typecheck
lint
tests
```

### 33.8 Mobile CD

Do not automatically launch an EAS build after every merge.

Reason:

```text
EAS Free build quota
+
native build not needed on every backend/mobile commit
```

Instead:

DEV APK:

```text
manual when integration testing requires a fresh binary
```

PROD demo APK:

```text
manual for release candidate / V1 release
```

This is a deliberate free-quota conservation policy.

---

## 34. PROD Safety

**TECHNICAL DECISION PROPOSED**

```text
feature/*
→ PR
→ develop
→ DEV
```

When release candidate is accepted:

```text
develop
→ PR
→ main
→ PROD
```

Recommended GitHub protection:

```text
develop:
- no direct feature work
- PR required
- CI required

main:
- no normal direct pushes
- PR required
- CI required
```

Render PROD watches only:

```text
main
```

Therefore a merge to `develop` cannot automatically deploy PROD.

No complex release-management platform is required.

---

## 35. Flyway During Deployment

**TECHNICAL DECISION PROPOSED**

Deployment lifecycle:

```text
Render starts new Docker container
        |
        v
Spring Boot starts
        |
        v
Datasource connects to target Neon database
        |
        v
Flyway reads schema history
        |
        +-- no pending migration
        |        |
        |        v
        |    continue
        |
        `-- pending migrations
                 |
                 v
              apply
                 |
                 v
        Flyway validation passes
                 |
                 v
        Hibernate ddl-auto=validate
                 |
                 v
         application ready
                 |
                 v
        health/liveness = UP
```

Failure:

```text
Flyway failure
OR
Hibernate validation failure
       |
       v
startup fails
       |
       v
new deployment is not healthy
```

For Jobiq V1 this is sufficiently safe.

No separate database-migration pipeline is introduced.

Reasons:

```text
single application
single DB per environment
small migrations
single release stream
Flyway already serializes migrations
```

Migration discipline should prefer additive/backward-compatible changes where practical.

---

## 36. Healthcheck

### Decision

Use Spring Boot Actuator, but only the minimum needed.

Dependency:

```text
spring-boot-starter-actuator
```

Render path:

```text
GET /actuator/health/liveness
```

### Why liveness instead of generic `/actuator/health`

For Jobiq V1, Render's periodic healthcheck answers only:

```text
“Is the Spring Boot process alive?”
```

It must not test PostgreSQL/Neon availability on every probe.

Therefore:

```text
Render healthcheck
→ /actuator/health/liveness
→ livenessState only
→ no DB health indicator
→ no PostgreSQL query
→ no Neon wake-up caused by the probe
```

Conceptually, the liveness group is restricted to the application liveness state:

```text
management.endpoint.health.group.liveness.include=livenessState
```

Database validation remains part of application startup:

```text
Spring Boot startup
→ PostgreSQL connection
→ Flyway validates/applies migrations
→ Hibernate ddl-auto=validate
→ application available
```

No additional readiness probe is configured for Render in V1.

### Exposure

The only anonymously/publicly accessible health endpoint is:

```text
GET /actuator/health/liveness
```

The generic health endpoint and other Actuator routes are not part of the public API contract and must not be anonymously exposed.

Health response detail policy:

```text
show-details = never
show-components = never
```

Do not expose:

```text
/env
/beans
/configprops
/mappings
/heapdump
```

No database healthcheck, external ping, keep-alive mechanism, Kubernetes-style probe setup or additional observability platform is introduced.

---

## 37. CORS

### Native mobile behavior

React Native native HTTP requests are not governed by browser CORS enforcement.

Therefore the actual:

```text
Android Jobiq app
→ Render API
```

does not require CORS permission in the same way a browser SPA does.

CORS must not be treated as an authentication/security control.

JWT and backend authorization remain responsible for security.

### Local Expo Web

If Expo Web is used during development, browser CORS can matter.

Therefore retain configurable:

```text
CORS_ALLOWED_ORIGINS
```

Behavior:

```text
LOCAL
→ explicit localhost origins if Expo Web used

DEV
→ explicit web origins if needed, otherwise empty

PROD
→ empty if native-only
```

Never:

```text
*
```

by default in PROD.

---

## 38. Zero-Cost Deployment Policy

### ZERO-COST DEPLOYMENT POLICY

**LOCKED DEPLOYMENT POLICY v0.1 FINAL**

Jobiq V1 is:

```text
PORTFOLIO / DEMO
```

not:

```text
BUSINESS-CRITICAL PRODUCTION SERVICE
```

The following rules are mandatory.

### Payment method

Do not add:

```text
credit card
debit card
other automatic payment method
```

to Jobiq's deployment providers for V1.

### Provider plans

Use only free tiers.

No pay-as-you-go plan.

### Quota exhaustion

Accepted behavior:

```text
service suspension
database suspension
build disabled
wait until quota reset
temporary demo unavailability
```

Preferred over:

```text
automatic billing
```

### Cold starts

Accepted.

### SLA

No paid uptime guarantee.

No V1 SLA.

### Custom domain

Not required.

### Store publication

Not required.

### Keep-alive infrastructure

Forbidden for V1:

```text
external uptime pings
cron wakeups
keep-alive bots
synthetic traffic
```

### Provider upgrade

No provider may automatically be upgraded to a paid tier merely because the Free allowance becomes insufficient.

The decision must return to architecture review.

---

## 39. Billing Provider Validation

### Render

Status:

```text
PASS WITH ELIGIBILITY GUARD
```

Policy:

```text
Attempt to provision Render Free
        |
        v
Does account require payment card?
       / \
     NO   YES
     |     |
     |     v
     |  DO NOT ENTER CARD
     |     |
     |  DEPLOYMENT PROVIDER CONFLICT
     |
     v
Render approved
```

No exception.

### Neon

Status:

```text
PASS
```

Free-tier behavior is compatible with:

```text
no card
hard free allowance
scale to zero
temporary suspension instead of paid upgrade
```

### Expo / EAS

Status:

```text
PASS
```

Free-tier behavior is compatible with:

```text
fixed quotas
no automatic paid overage
manual/local build fallback
```

---

## 40. Failure / Provider Change Policy

**LOCKED DEPLOYMENT POLICY v0.1 FINAL**

If any selected provider later changes its Free plan to require:

```text
payment method
billing account with automatic charge
pay-as-you-go activation
mandatory paid upgrade
```

Jobiq will **not** activate billing automatically.

Required response:

```text
Provider policy changes
        |
        v
Does existing zero-cost policy still hold?
       / \
     YES  NO
     |     |
 continue |
           v
     DO NOT ADD PAYMENT METHOD
           |
           v
     evaluate migration
           |
           v
      alternative free host
```

### Portability objective

Jobiq intentionally avoids lock-in.

Backend:

```text
standard Docker image
standard HTTP
standard environment variables
```

Database:

```text
standard PostgreSQL
standard JDBC
Flyway
no proprietary Neon Data API
```

Mobile:

```text
Expo
API URL configurable at build time
```

Changing provider should therefore primarily involve:

```text
new runtime host
new database host
environment variables
API URL rebuild
database export/import
```

not rewriting the application architecture.

No migration infrastructure is built until a migration is actually needed.

---

## 41. Testing Strategy

Core v0.1 strategy remains.

### Backend unit

Focus:

```text
authentication decisions
job validation
ownership
swipe rules
skill normalization
```

### Repository/integration

Use PostgreSQL 18 through Testcontainers.

Protect:

```text
unique swipe
unique application
FK constraints
feed query
transaction behavior
database locks
```

### API

Protect:

```text
register
login
JWT
role authorization
ownership
job lifecycle
feed
swipe
applications
applicants
```

### Required cases

```text
Candidate registration succeeds
Recruiter registration succeeds
duplicate email rejected

login succeeds
wrong password rejected
expired JWT rejected

Candidate cannot create Job
Recruiter cannot swipe

Candidate only updates own profile

Recruiter creates Job
different Recruiter cannot edit it
different Recruiter cannot close it

CLOSED Job absent from feed
previously swiped Job absent from feed

DISLIKE:
1 Swipe
0 Application

LIKE:
1 Swipe
1 Application(APPLIED)

concurrent LIKE:
maximum 1 Swipe
maximum 1 Application

Recruiter:
can see applicants for own Job
cannot see applicants for another Recruiter's Job
```

### Mobile

```text
Jest
jest-expo
React Native Testing Library
```

Priority:

```text
authStore
SecureStore restoration
environment-scoped token
/auth/me bootstrap
401 invalidation
route guards
JobCard controls
Swipe mapping
feed state
```

### Deployment-specific tests

Add configuration tests/smoke checks for:

```text
application-dev loads without secret defaults
application-prod fails when mandatory secrets missing
server respects PORT
liveness endpoint is reachable, exposes no DB details and does not query PostgreSQL
Docker image starts
```

No complex mobile E2E framework in initial V1.

---

## 42. CI/CD Pipeline

Conceptually:

```text
                  ┌─────────────────┐
                  │ Pull Request    │
                  └────────┬────────┘
                           |
            +--------------+--------------+
            |                             |
            v                             v
       Backend CI                     Mobile CI
            |                             |
        compile                         npm ci
        tests                           typecheck
        verify                          lint
                                        tests
            |                             |
            +--------------+--------------+
                           |
                         PASS
```

On develop:

```text
CI PASS
   |
Render DEV auto-deploy
```

On main:

```text
CI PASS
   |
Render PROD auto-deploy
```

No backend deploy from an unverified commit.

---

## 43. Logging & Security Baseline

Preserved from v0.1.

Use:

```text
SLF4J / Spring logging
requestId
MDC
```

Log:

```text
unexpected server errors
auth failure category
ownership conflict
domain conflict
database constraint translation
deployment/startup errors
Flyway failures
```

Never log:

```text
password
passwordHash
JWT
Authorization
DB_PASSWORD
full Neon connection credentials
candidate bio unnecessarily
private profile links unnecessarily
```

Unexpected stack traces are allowed in server-side ERROR logging.

They are never returned through the REST contract.

HTTPS:

```text
Mobile -> Render
```

uses provider-managed TLS.

Render -> Neon database transport uses PostgreSQL TLS.

---

## 44. Clean Migration Plan

**LOCKED FROM BLUEPRINT**

| Recovered item | Classification | V1 action |
|---|---|---|
| public/protected routing | KEEP CONCEPT | recreate as route groups |
| Expo Router | MIGRATE | clean SDK 57 foundation |
| root layout | MIGRATE | selectively adapt |
| route guard | MIGRATE | preserve state concept, fix semantics |
| login visual | MIGRATE | selective UI migration |
| login logic | REWRITE | real backend/session |
| register | REWRITE | Candidate/Recruiter flow |
| auth actions/interfaces | MIGRATE | adapt |
| Axios concept | KEEP CONCEPT | new API client |
| old Axios config | REWRITE | new environment config |
| Zustand pattern | MIGRATE | new auth store |
| old auth store implementation | REWRITE | deterministic states |
| theme | MIGRATE | selective |
| Colors | MIGRATE | if still valid |
| ThemeTextInput | MIGRATE | if useful |
| fonts | MIGRATE | current Expo API |
| intentional fonts/assets | MIGRATE | only useful assets |
| starter assets | DISCARD | no |
| `.env` | DISCARD | new env strategy |
| package.json | REWRITE | clean SDK 57 |
| lockfile | REWRITE | regenerate |
| app.json | REWRITE | clean foundation |
| node_modules | DISCARD | never migrate |
| `.expo` | DISCARD | never migrate |
| old `.git` | DISCARD FROM NEW REPO | retain local history only |
| deprecated dependencies | DISCARD | no |
| unused starter code | DISCARD | no |

Additionally, the FINAL baseline requires clean mobile configuration for:

```text
LOCAL
DEV
PROD
```

None of the recovered environment configuration is copied literally.

---

## 45. Repository Foundation Plan

The future Foundation PR should establish the approved architecture without implementing business scope prematurely.

### Repository skeleton

Include:

```text
backend/
mobile/
docs/
.github/workflows/
docker-compose.yml
README.md
.gitignore
```

### Backend foundation

Include:

```text
Spring Boot 4.1.1
Java 25
Maven Wrapper
required starters only
PostgreSQL driver
Flyway
Actuator (minimal liveness endpoint only)
Dockerfile
.dockerignore

application.yml
application-local.yml
application-dev.yml
application-prod.yml
```

Backend compiles.

Docker image builds.

### Mobile foundation

Include:

```text
clean Expo SDK 57
Expo Router
TypeScript
Axios
Zustand
SecureStore
Gesture Handler
Reanimated
theme baseline
eas.json
.env.example
```

No recovered files copied wholesale.

### Documentation

Include:

```text
Blueprint FINAL
Technical Design FINAL
recovery.md
deployment.md
```

`deployment.md` documents:

```text
Render service names
branches
root path
region
health path = /actuator/health/liveness
required env variable names
Neon project names
EAS environment mapping
zero-cost policy
```

Never actual secrets.

### CI

Include green:

```text
backend CI
mobile CI
```

### Foundation exclusions

No:

```text
Eureka
Gateway
Kafka
RabbitMQ
Redis
chat
AI
notifications
CV parsing
admin
Kubernetes
Terraform
paid monitoring
```

---

## 46. Implementation Sequence

The FINAL baseline refines the sequence to make DEV useful during actual implementation rather than waiting until the entire application is finished.

```text
0. Repository Foundation
         |
1. DEV Deployment Foundation
         |
2. Clean Mobile Migration
         |
3. Authentication E2E
         |
4. Profiles + Company
         |
5. Recruiter Jobs
         |
6. Candidate Feed
         |
7. Swipe
         |
8. Applications
         |
9. Recruiter Applicants
         |
10. UX / Quality
         |
11. PROD Deployment Foundation
         |
12. Production E2E
         |
13. V1 Release
```

### Slice 0 — Repository Foundation

```text
backend compiles
mobile compiles
PostgreSQL local
Docker build works
CI green
```

### Slice 1 — DEV Deployment Foundation

After repository exists:

```text
Neon jobiq-dev
Render jobiq-api-dev
Docker deployment
liveness healthcheck
DEV configuration
```

No business feature needed yet beyond application startup.

This validates the deployment baseline early.

### Slice 2 — Clean Mobile Migration

Migrate useful recovered concepts only.

### Slice 3 — Authentication E2E

```text
Mobile
→ Render DEV
→ Neon DEV
```

Real remote authentication should become possible here in addition to LOCAL.

### Slice 4 — Profiles + Company

Candidate/recruiter onboarding.

### Slice 5 — Recruiter Jobs

Create/list/detail/edit/close.

### Slice 6 — Feed

OPEN + unseen.

### Slice 7 — Swipe

LEFT and RIGHT.

### Slice 8 — Applications

LIKE → APPLIED.

### Slice 9 — Applicants

Recruiter closes main functional loop.

### Slice 10 — UX / Quality

Errors, cold-start UX, accessibility, empty states and quality gaps.

### Slice 11 — PROD Deployment Foundation

Only near release:

```text
Neon jobiq-prod
Render jobiq-api-prod
PROD secrets
main auto-deployment
EAS production environment
```

This avoids consuming Free resources unnecessarily throughout development.

### Slice 12 — Production E2E

Real production/demo infrastructure validation.

### Slice 13 — V1 Release

Only after Production E2E passes.

---

## 47. Technical Risks

### R1 — Session expiry

**Impact:** Low/Medium

No refresh token means:

```text
expired JWT
→ login required
```

Accepted V1 behavior.

### R2 — Ambiguous Swipe network failure

**Impact:** Medium

Server may commit before response disappears.

Mitigation:

```text
DB uniqueness
409 reconciliation
no blind write retries
```

### R3 — Job close race

**Impact:** Low

Mitigation:

```text
transaction
database locking
status verification
```

### R4 — Concurrent Skill creation

**Impact:** Low

Mitigation:

```text
normalized_name UNIQUE
conflict re-read
```

### R5 — Simple Company model

**Impact:** Low

Two real-world recruiters from the same company can create separate Company records.

Accepted because teams/company administration are not V1.

### R6 — Expo dependency churn

**Impact:** Medium

Mitigation:

```text
SDK 57 stable patch
npx expo install
lockfile
npm ci
Expo Doctor
```

### R7 — Render account-specific card verification

**Impact:** High if triggered

Mitigation:

```text
never enter card
provider conflict if requested
portable Docker architecture
```

### R8 — Render Free policy changes

**Impact:** High

Mitigation:

```text
zero-cost policy
no payment method
standard Docker
standard environment configuration
provider migration instead of upgrade
```

### R9 — Render cold start

**Impact:** Medium

Mitigation:

```text
accepted portfolio behavior
90s remote mobile timeout
clear loading/error UX
no keepalive hacks
```

### R10 — Limited backend RAM

**Impact:** Medium

Mitigation:

```text
-Xmx256m
small Hikari pool
small Tomcat pool
minimal dependencies
smoke test Docker image
```

If the final application cannot reliably start within the free hosting memory limit after reasonable tuning, this becomes a genuine deployment-provider compatibility issue.

Do not blindly downgrade Java as the first reaction.

### R11 — Limited CPU

**Impact:** Medium

Potential consequences:

```text
slow cold start
slow initial class loading
reduced request concurrency
```

Mitigation:

```text
single small app
minimal framework dependencies
no unnecessary background tasks
cold start accepted
```

### R12 — Render DEV + PROD shared quota

**Impact:** Medium

Both services may share workspace-level free quotas.

Mitigation:

```text
no keepalive
services naturally spin down
portfolio-level traffic
PROD provisioned late
quota exhaustion accepted
```

No paid workaround.

### R13 — External-database traffic

**Impact:** Low/Medium

Mitigation:

```text
same geographic region
small payloads
normal database access
no background polling
```

### R14 — Neon scale-to-zero

**Impact:** Low/Medium

Mitigation:

```text
accepted
Hikari minIdle=0
reconnect correctly
no keepalive
```

### R15 — Neon Free quota

**Impact:** Medium if heavily demoed

Mitigation:

```text
separate DEV/PROD projects
small V1 data model
no file storage in DB
manual provider-dashboard awareness
accept suspension
```

### R16 — DB connections after Neon sleep

**Impact:** Low

Mitigation:

```text
Hikari
small pool
minimumIdle=0
connection validation/recreation
```

### R17 — Mobile cloud build quota

**Impact:** Low

Mitigation:

```text
no build on every merge
manual APK builds
wait for quota reset
local Android build fallback
```

No paid upgrade.

### R18 — No SLA

**Impact:** Medium for availability, low for V1 objective

Mitigation:

```text
explicit portfolio/demo positioning
no SLA promise
failure accepted
```

### R19 — Provider outage

**Impact:** Medium

No multi-region/failover infrastructure will be introduced.

That would violate V1 complexity and cost goals.

---

## 48. Open Technical Decisions

### Blocking architecture decisions

```text
NONE
```

The core technical architecture can be implemented without unresolved product or system-design questions.

### Provider provisioning gate

One external operational gate remains:

```text
Can the actual Render account provision Free Web Services
without being asked for a card?
```

This is not currently considered a Technical Design blocker.

However:

```text
IF Render asks for card
THEN DEPLOYMENT PROVIDER CONFLICT
```

and provider selection must be reopened before any deployment.

### Intentionally deferred non-blocking items

```text
final visual design polish
exact swipe animation constants
app icon/splash final assets
future custom domain
store publication
future paid hosting
V2 functionality
```

---

## 49. V1 Technical Definition of Done

V1 is not complete merely because LOCAL works.

It must pass:

```text
LOCAL quality gates
+
TEST quality gates
+
DEV integration
+
real PRODUCTION E2E
```

### 49.1 Local

```text
PostgreSQL Docker starts
backend starts
Flyway migrates empty database
mobile connects
core E2E works
```

### 49.2 Test

```text
backend CI green
mobile CI green
PostgreSQL Testcontainers green
authorization tests green
concurrency tests green
```

### 49.3 DEV

```text
develop
→ GitHub CI
→ Render jobiq-api-dev
→ Neon jobiq-dev
```

Required:

```text
real remote auth
real profiles
real jobs
real feed
real swipe
real applications
real applicants
```

### 49.4 Production infrastructure

Required:

```text
Render jobiq-api-prod
Neon jobiq-prod
PROD secrets isolated
main CI/CD operational
Android PROD demo build
```

No payment method may have been introduced to satisfy this requirement.

### 49.5 Production E2E

**Mandatory.**

```text
Android Jobiq PROD demo build
            |
            v
   Jobiq PROD Render API
            |
            v
     Neon jobiq-prod
```

#### Positive path

```text
Recruiter
   |
Register
   |
Login
   |
Recruiter Profile + Company
   |
Create OPEN Job
   |
   v
Candidate
   |
Register
   |
Login
   |
Candidate Profile
   |
Feed
   |
Job appears
   |
RIGHT
   |
Swipe LIKE
   |
Application APPLIED
   |
   v
Recruiter
   |
Own Job
   |
Applicants
   |
Candidate visible
   |
Candidate basic profile
```

Database verification:

```text
exactly 1 Swipe LIKE
exactly 1 Application APPLIED
```

#### Negative path

```text
Candidate
   |
receives another Job
   |
LEFT
   |
Swipe DISLIKE
   |
Job removed from feed
   |
refresh / reload
   |
Job does not return
```

Database:

```text
1 Swipe DISLIKE
0 Applications
```

### 49.6 Deployment quality

Also required:

```text
Render cold start handled without corrupting mobile auth
PROD API uses PROD database only
DEV API uses DEV database only
DEV tokens rejected by PROD
PROD tokens rejected by DEV
liveness healthcheck exposes no secrets and does not query PostgreSQL
no credentials committed
Docker image runs with Java 25
Flyway runs successfully on remote DB
Hibernate validate succeeds
```

### 49.7 Portfolio artifact

At least:

```text
installable Android APK
```

pointing to PROD.

No App Store or Google Play publication required.

### 49.8 Overall release result

Jobiq V1 must be:

```text
functional
coherent
deployable
testable
demoable
portfolio-ready
zero-cost under the locked policy
```

---

## 50. Explicit Non-Goals

**LOCKED FROM BLUEPRINT**

V1 does not include:

```text
chat
realtime messaging
notifications
email workflows
AI
matching engine
recommendation system
advanced feed scoring
advanced filters
CV parsing
CV upload
CV builder
employment history
education
certifications
languages
LinkedIn import
cover letters
social login
refresh tokens
payments
subscriptions
premium plans
admin
recruiter teams
company invitations
multi-company recruiter management
interview workflow
calendar
maps/geolocation
analytics platform
microservices
Kafka
RabbitMQ
Eureka
Config Server
API Gateway
RAG
distributed transactions
service-to-service REST
separate service databases
Kubernetes
paid observability
high availability
multi-region deployment
custom domain
App Store publication
Google Play publication
```

No infrastructure is prepared merely because one of those items could exist in a future version.

---

# Final Architecture

```text
                            JOBIQ V1

                      ┌───────────────────┐
                      │      GitHub       │
                      │ jzapataa/jobiq    │
                      └─────────┬─────────┘
                                |
                +---------------+---------------+
                |                               |
             develop                           main
                |                               |
              CI PASS                         CI PASS
                |                               |
                v                               v
       ┌──────────────────┐           ┌──────────────────┐
       │ Render Free DEV  │           │ Render Free PROD │
       │                  │           │                  │
       │ jobiq-api-dev    │           │ jobiq-api-prod   │
       │ Docker           │           │ Docker           │
       │ Temurin Java 25  │           │ Temurin Java 25  │
       │ Spring Boot 4.1  │           │ Spring Boot 4.1  │
       └────────┬─────────┘           └────────┬─────────┘
                |                              |
                | TLS                          | TLS
                v                              v
       ┌──────────────────┐           ┌──────────────────┐
       │ Neon Free DEV    │           │ Neon Free PROD   │
       │                  │           │                  │
       │ jobiq-dev        │           │ jobiq-prod       │
       │ PostgreSQL 18    │           │ PostgreSQL 18    │
       └──────────────────┘           └──────────────────┘
                ^                              ^
                |                              |
        ┌───────┴────────┐            ┌────────┴────────┐
        │ Mobile DEV     │            │ Mobile PROD     │
        │ Expo SDK 57    │            │ Expo SDK 57     │
        │ EAS Preview    │            │ EAS Internal    │
        └────────────────┘            │ Android APK     │
                                      └─────────────────┘
```

Cost behavior:

```text
NO CARD
NO PAY-AS-YOU-GO
NO AUTOMATIC BILLING
NO KEEP-ALIVE INFRASTRUCTURE

quota reached
      |
      v
service/build/database may stop
      |
      v
ACCEPTED
```

Portability:

```text
Spring Boot
     |
   Docker
     |
standard PostgreSQL
     |
Expo configurable API
```

The deployment design therefore preserves the original Jobiq architectural principle: **a complete, demonstrable V1 with the minimum infrastructure justified by the product**.

The new DEV/PROD architecture adds environment separation and deployment discipline without turning Jobiq into a DevOps project.

---

# FINAL VERDICT

**JOBIQ TECHNICAL DESIGN v0.1 FINAL — APPROVED BASELINE**

Render Free, Neon Free and Expo/EAS Free provide a viable zero-cost architecture under the deployment policy defined in this document.

Render remains subject to an explicit provisioning gate:

```text
IF account requires payment card
THEN do not add card
AND mark DEPLOYMENT PROVIDER CONFLICT
```

No repository, Dockerfile, environment, database, Render service, Neon project, EAS build, branch, commit, PR or deployment is created by this document.
