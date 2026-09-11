# JOBIQ - PRODUCT BLUEPRINT v0.1 FINAL

**Status:** FINAL  
**Date:** 10 September 2026  
**Product:** Jobiq  
**Repository target:** `jzapataa/jobiq`  
**Document role:** Product and architecture baseline for V1

---

## 0. Purpose of this document

This Blueprint freezes the product scope and the main architecture decisions for Jobiq V1 before Technical Design and implementation begin.

It is intentionally not a low-level technical specification. Exact versions, database DDL, REST payloads, package names, security implementation details and deployment specifics belong to the Technical Design.

The recovered local prototype remains a source for selective migration, not the implementation baseline.

---

## 1. Product vision

**Jobiq** is a mobile application for discovering job opportunities through a fast, visual, swipe-first experience.

The core interaction is simple:

```text
Job offer
   |
 Job Card
  /      \
LEFT    RIGHT
No       Interested
```

The product focuses on the first decision in a job search: "Am I interested in this opportunity?"

Jobiq V1 is not intended to replace a complete job portal or an ATS. Its purpose is to make discovery and the first expression of interest frictionless.

---

## 2. Core product idea

Jobiq connects two roles:

| Role | Primary objective |
|---|---|
| Candidate | Discover offers and express interest quickly |
| Recruiter | Publish offers and review interested candidates |

Candidate interaction:

```text
Candidate
   |
Job Feed
   |
Job Card
   |
 Swipe
```

**Swipe left** means `DISLIKE`: the offer is discarded and does not return to the candidate's feed.

**Swipe right** means `LIKE`: Jobiq creates an `Application` with status `APPLIED`. For V1, **Swipe Right = Application**.

No intermediate concepts such as favorite, saved, pre-application or interest list are introduced in V1.

---

## 3. Product principles

| Principle | V1 interpretation |
|---|---|
| Mobile first | The main product experience is the mobile app |
| Swipe first | Job cards and swipe are the differentiating interaction |
| Fast discovery | Minimize friction before a candidate can decide |
| Simple domain | Avoid reproducing a full job board or ATS |
| Explicit roles | Candidate and Recruiter have distinct capabilities |
| Complete over complex | A complete V1 is more valuable than distributed infrastructure |
| No artificial architecture | Complexity must be justified by product needs |
| Portfolio quality | The final product must be functional, testable, deployable and demoable |

---

## 4. Target users

### 4.1 Candidate

A person looking for job opportunities who wants to perform a fast first selection instead of browsing long result lists.

The V1 profile is deliberately small. It contains enough information for a recruiter to understand who the candidate is, but it is **not a full CV builder**.

### 4.2 Recruiter

A person who publishes jobs for one company and reviews candidates who have swiped right on those jobs.

V1 does not model corporate teams, invitations, departments, granular company permissions or multiple recruiter administration layers.

---

## 5. Candidate V1 flow

```text
Open Jobiq
    |
Register / Login
    |
Candidate Profile
    |
Job Feed
    |
Job Card
  /       \
LEFT     RIGHT
 |          |
Discard   Application(APPLIED)
            |
      My Applications
```

Candidate V1 capabilities:

- register and login;
- create or update a basic profile;
- receive open jobs not previously swiped;
- inspect a job card;
- swipe left to discard;
- swipe right to apply;
- consult their own applications.

---

## 6. Recruiter V1 flow

```text
Open Jobiq
    |
Register / Login
    |
Recruiter Profile + Company
    |
Create Job
    |
My Jobs
    |
Job Detail
    |
Applicants
    |
Candidate Profile
```

Recruiter V1 capabilities:

- register and login;
- create or update a recruiter profile;
- associate with a company within the V1 flow;
- create jobs;
- edit own jobs;
- close own jobs;
- see applicants for own jobs;
- inspect the basic Jobiq profile of an applicant.

The recruiter **does not change application status in V1**.

---

## 7. Authentication and roles

V1 has exactly two roles:

```text
CANDIDATE
RECRUITER
```

Base identity:

```text
User
--------------------
id
email
passwordHash
name
role
createdAt
updatedAt
```

Role-specific data is modeled separately:

```text
User
 |-- CandidateProfile
 `-- RecruiterProfile
```

No JPA inheritance hierarchy is required between Candidate and Recruiter. `User` owns authentication and identity; role-specific profiles own domain data.

---

## 8. Candidate profile V1

The candidate profile must stay intentionally basic.

```text
CandidateProfile
--------------------
id
userId
headline
location
bio
linkedinUrl? 
githubUrl?
portfolioUrl?
createdAt
updatedAt
```

Skills are modeled separately:

```text
CandidateProfile
      |
CandidateSkill
      |
     Skill
```

The following are explicitly outside V1:

- employment history;
- education;
- certifications;
- languages;
- CV upload;
- PDF CV generation;
- years-of-experience scoring;
- remote preference/matching rules.

LinkedIn, GitHub and portfolio URLs are optional and exist only to make the recruiter-facing profile more useful without turning Jobiq into a CV platform.

---

## 9. Recruiter profile V1

```text
RecruiterProfile
--------------------
id
userId
companyId
position
createdAt
updatedAt
```

A recruiter belongs to one company in V1.

No multi-company recruiter management is required.

---

## 10. Company

```text
Company
--------------------
id
name
description
website
logoUrl
location
createdAt
updatedAt
```

V1 does not include advanced company administration, invitations or team management.

---

## 11. Job model

`Job` is one of the central V1 entities.

```text
Job
--------------------
id
title
description
companyId
createdByRecruiterId
location
workMode
salaryMin
salaryMax
currency
experienceLevel
status
createdAt
updatedAt
closedAt
```

### WorkMode

```text
REMOTE
HYBRID
ONSITE
```

### ExperienceLevel

```text
JUNIOR
MID
SENIOR
LEAD
```

### JobStatus

```text
OPEN
CLOSED
```

Required skills are normalized:

```text
Job
 |
JobSkill
 |
Skill
```

The exact optional/required field policy will be finalized in Technical Design and API validation rules.

---

## 12. Job Card

The card is designed for a first decision, not for displaying every field of the offer.

Conceptual content:

```text
+--------------------------------+
| Senior Java Developer          |
| Example Company                |
|                                |
| Murcia                         |
| Hybrid                         |
| 40k - 50k EUR                  |
|                                |
| Java - Spring - PostgreSQL     |
|                                |
| Short description...           |
|                                |
|        [X]          [LIKE]      |
+--------------------------------+
```

In addition to the swipe gesture, V1 should offer explicit dislike/like controls for accessibility and demo reliability.

---

## 13. Swipe model

```text
Swipe
--------------------
id
candidateId
jobId
decision
createdAt
```

`decision`:

```text
LIKE
DISLIKE
```

Invariant:

```text
UNIQUE(candidateId, jobId)
```

A candidate can make only one initial swipe decision for a given job.

---

## 14. Application model

A `LIKE` creates an application.

```text
Application
--------------------
id
candidateId
jobId
status
createdAt
```

V1 contains a single application state:

```text
APPLIED
```

The enum is retained even with a single value so that future states can be added without changing the meaning of the entity.

The V1 recruiter cannot update application state.

**Atomic rule:** creating `Swipe(LIKE)` and `Application(APPLIED)` must succeed or fail together.

---

## 15. Future chat behavior - V2 candidate

Chat is **out of scope for V1**.

The intended future direction is:

```text
Candidate swipes RIGHT
        |
Application = APPLIED
        |
Recruiter sees candidate
        |
[V2]
Recruiter starts conversation
        |
Candidate <-> Recruiter chat
```

The recruiter, not the swipe itself, should initiate the future conversation. This avoids creating empty chat threads for every application.

No V1 architecture should unnecessarily block this evolution, but V1 will not implement chat entities, realtime messaging, notifications or conversation state.

---

## 16. Feed rules V1

V1 has no AI matching and no recommendation engine.

A candidate receives jobs satisfying:

```text
Job.status = OPEN
AND
no Swipe(candidate, job) exists
```

Initial ordering:

```text
createdAt DESC
```

Therefore the V1 feed is deterministic: open, unseen jobs, newest first.

Advanced matching, scoring and filters are future capabilities.

---

## 17. Target repository strategy

Target repository:

```text
jzapataa/jobiq
```

Repository model: **monorepo**.

```text
jobiq/
|
|-- mobile/
|-- backend/
|-- docs/
|-- docker-compose.yml
|-- .gitignore
`-- README.md
```

The monorepo keeps the product easy to clone, understand, run and present in a portfolio.

---

## 18. Backend architecture

Target: **modular monolith**.

```text
              +----------------------+
              |     Jobiq Mobile     |
              | React Native / Expo  |
              +----------+-----------+
                         |
                        REST
                         |
              +----------v-----------+
              |     Jobiq Backend    |
              |     Spring Boot      |
              |----------------------|
              | auth                 |
              | users                |
              | candidates           |
              | recruiters           |
              | companies            |
              | jobs                 |
              | swipes               |
              | applications         |
              +----------+-----------+
                         |
                         v
                    PostgreSQL
```

V1 will **not** use:

- Eureka;
- Config Server;
- API Gateway;
- Kafka;
- RabbitMQ;
- separate databases per module;
- distributed transactions;
- service-to-service REST.

The recovered sources contain no backend implementation that justifies rebuilding the previously imagined microservice architecture.

---

## 19. Backend stack baseline

The intended backend stack is:

- Java;
- Spring Boot;
- Spring Web;
- Spring Security;
- Spring Data JPA;
- PostgreSQL;
- Flyway;
- JWT;
- Bean Validation;
- Maven;
- Docker;
- JUnit;
- Testcontainers.

Exact versions are intentionally deferred to Technical Design.

---

## 20. Mobile architecture baseline

The mobile application will use:

- React Native;
- Expo;
- Expo Router;
- TypeScript;
- Axios;
- Zustand;
- Expo SecureStore.

The recovered prototype is useful as a migration source because it already contains a working Expo project structure, routing, theming and an early authentication foundation. It will **not** be upgraded in place as the final V1 base.

---

## 21. Mobile recovery strategy

### 21.1 Migrate selectively

Preserve or adapt the useful concepts/code for:

- public vs protected route organization;
- root layout;
- Expo Router navigation approach;
- theme foundation;
- colors;
- Montserrat/Poppins font setup where still desired;
- themed text/input components that remain useful;
- login layout;
- auth action/API separation;
- minimal user/auth interfaces;
- Zustand auth-store pattern;
- route-guard state model (`checking`, `authenticated`, `unauthenticated`).

### 21.2 Recreate

Recreate on a clean current Expo foundation:

- `package.json`;
- lockfile;
- `app.json` / app configuration;
- environment configuration;
- README;
- final dependency graph.

### 21.3 Do not migrate

Do not migrate:

- `node_modules/`;
- `.expo/`;
- `.env`;
- old `.git/` directory;
- old lockfile verbatim;
- deprecated dependencies;
- unused starter components;
- starter React images;
- broken reset-project tooling.

---

## 22. Authentication V1

Conceptual flow:

```text
Register
   |
 Login
   |
  JWT
   |
SecureStore
   |
Authorization: Bearer <token>
   |
GET /auth/me
```

V1 uses an access JWT without refresh-token flow.

On an invalid/expired token:

```text
401
 |
clear local secure token
 |
return to login
```

Conceptual endpoints:

```text
POST /auth/register
POST /auth/login
GET  /auth/me
```

The recovered prototype's `/auth/check-status` concept is not a locked API contract. The final contract belongs to Technical Design.

Passwords are never stored in plaintext.

---

## 23. Ownership and authorization

### Candidate can

- update own profile;
- read own feed;
- swipe jobs;
- read own applications.

### Candidate cannot

- create or manage jobs;
- access recruiter-only endpoints;
- read another candidate's applications.

### Recruiter can

- update own recruiter profile;
- operate within the V1 company flow;
- create jobs;
- edit own jobs;
- close own jobs;
- inspect applicants for own jobs.

### Recruiter cannot

- modify jobs created by another recruiter;
- browse private candidate/application data unrelated to own jobs.

---

## 24. Conceptual V1 API surface

These endpoints describe required capabilities, not the final REST contract.

### Auth

```text
POST /auth/register
POST /auth/login
GET  /auth/me
```

### Candidate

```text
GET /candidate/profile
PUT /candidate/profile
GET /jobs/feed
POST /jobs/{jobId}/swipes
GET /applications
```

### Recruiter

```text
GET  /recruiter/profile
PUT  /recruiter/profile
POST /jobs
GET  /jobs/mine
GET  /jobs/{id}
PUT  /jobs/{id}
POST /jobs/{id}/close
GET  /jobs/{id}/applications
```

Exact naming, request/response payloads and error contracts will be defined in Technical Design.

---

## 25. Persistence

V1 uses a single PostgreSQL database.

Schema evolution is managed through Flyway migrations.

A deployed V1 must not depend on destructive auto-generation such as `ddl-auto=create` as its persistence strategy.

---

## 26. Local development

Target developer experience:

```text
docker compose up
```

At minimum, Docker Compose provisions PostgreSQL for local development.

The backend may run through the IDE or Maven during development. Backend containerization can be included when it provides clear value for reproducibility or deployment.

---

## 27. Testing strategy

V1 testing should protect behavior rather than chase arbitrary coverage percentages.

### Backend

- unit tests for domain/application rules;
- repository/integration tests;
- API tests for critical flows;
- Testcontainers where useful for PostgreSQL behavior.

### Mobile

- auth/store logic tests;
- critical component/interaction tests;
- targeted tests for feed/swipe state where practical.

Critical rules to protect:

```text
authentication
authorization
job ownership
feed exclusion after swipe
one swipe per candidate/job
LIKE -> exactly one Application
DISLIKE -> no Application
closed jobs excluded from feed
```

---

## 28. Critical V1 invariants

```text
1 candidate + 1 job -> maximum 1 swipe

Swipe LIKE -> exactly 1 Application(APPLIED)

Swipe DISLIKE -> 0 applications

CLOSED job -> never appears in feed

Previously swiped job -> never appears again in feed

Recruiter -> only manages own jobs

Candidate -> cannot perform recruiter operations
```

These invariants should be enforced at the appropriate combination of domain logic, database constraints and automated tests.

---

## 29. V1 scope

| Capability | V1 |
|---|---|
| Candidate registration | Included |
| Recruiter registration | Included |
| Login | Included |
| JWT auth | Included |
| Candidate basic profile | Included |
| Optional LinkedIn/GitHub/portfolio links | Included |
| Recruiter profile | Included |
| Company | Included |
| Job create/edit/close | Included |
| Job feed | Included |
| Job Card | Included |
| Swipe left/right | Included |
| Application on LIKE | Included |
| Application status APPLIED only | Included |
| Candidate application list | Included |
| Recruiter applicant list | Included |
| PostgreSQL | Included |
| Flyway migrations | Included |
| Local Docker infrastructure | Included |
| CI | Included |
| README/documentation | Included |
| Chat | Not V1 |

---

## 30. Explicitly out of scope for V1

The following must not be added unless the Blueprint is deliberately revised:

- chat or realtime messaging;
- notifications;
- email workflows;
- application lifecycle beyond `APPLIED`;
- AI matching;
- recommendation engine/service;
- advanced feed scoring;
- complex filters/preferences;
- employment-history CV builder;
- education/certifications/languages;
- CV parsing;
- CV upload;
- LinkedIn import;
- cover letters;
- payments or subscriptions;
- premium plans;
- admin panel;
- company teams;
- recruiter invitations;
- multi-company recruiter management;
- interview workflow;
- calendar integration;
- maps/geolocation;
- social login;
- refresh tokens;
- microservices;
- Kafka/RabbitMQ;
- Eureka/Config Server/Gateway;
- RAG;
- analytics platform.

---

## 31. V2 candidates - not committed scope

Potential V2 work includes:

- recruiter-initiated Candidate <-> Recruiter chat after an application;
- application lifecycle such as `CONTACTED` / `REJECTED`;
- richer candidate profiles;
- candidate job preferences;
- job filters;
- notifications;
- improved feed/matching.

These are candidates only. They are not requirements for V1 and should not influence V1 complexity beyond avoiding obviously irreversible design choices.

---

## 32. Repository and recovery history

The recovered local mobile prototype has its own small Git history and no configured remote. It is preserved as historical evidence but will not become the Git history of the new product repository.

Decision:

```text
Recovered local repository -> preserve locally as source evidence

New jzapataa/jobiq -> clean Git history
```

The new repository should contain a short `docs/recovery.md` documenting that V1 was created through a clean migration of selected pieces from the recovered prototype.

The historical empty GitHub `user-service` repository is not a backend foundation for V1. It should remain untouched until the new Jobiq repository is established; archiving/deletion can be decided later.

---

## 33. Definition of V1 Done

V1 is complete only when this end-to-end flow is demonstrable:

```text
Recruiter registers
        |
creates profile/company
        |
creates OPEN job
        |
        v
Candidate registers
        |
creates profile
        |
receives that job in feed
        |
swipes RIGHT
        |
Application(APPLIED) persisted
        |
        v
Recruiter opens own job
        |
sees candidate applicant
        |
opens candidate basic profile
```

And the negative path is also verified:

```text
Candidate receives another job
        |
swipes LEFT
        |
job disappears from feed
        |
job does not return
        |
no Application is created
```

The V1 release is not complete merely because the individual screens or endpoints exist.

---

## 34. Product success criterion

This is a portfolio/product-engineering V1. It does not need real commercial traction or product-market-fit metrics.

The release is successful when Jobiq is:

```text
functional
coherent
deployable
testable
demoable
portfolio-ready
```

---

## 35. Delivery sequence

```text
Blueprint v0.1 FINAL
        |
Technical Design v0.1
        |
Repository Foundation
        |
Clean Mobile Migration
        |
Authentication
        |
Profiles + Company
        |
Jobs
        |
Feed
        |
Swipe
        |
Applications
        |
Recruiter Applicants Flow
        |
UX Polish
        |
Quality / CI
        |
Deployment
        |
V1 Release
        |
jorgezapata.es portfolio entry
```

---

## 36. Decisions locked in v0.1

| Decision | Status |
|---|---|
| Clean migration from recovered local prototype | LOCKED |
| Preserve useful mobile pieces selectively | LOCKED |
| New repository `jzapataa/jobiq` | LOCKED |
| Monorepo | LOCKED |
| Modular monolith backend | LOCKED |
| Spring Boot backend | LOCKED |
| PostgreSQL | LOCKED |
| React Native + Expo mobile | LOCKED |
| Candidate + Recruiter roles only | LOCKED |
| Candidate profile deliberately basic | LOCKED |
| LinkedIn/GitHub/portfolio URLs optional | LOCKED |
| Swipe Right = Application | LOCKED |
| Application status V1 = `APPLIED` only | LOCKED |
| Recruiter does not update application status in V1 | LOCKED |
| Chat excluded from V1 | LOCKED |
| Recruiter-initiated chat is a V2 candidate | LOCKED FOR V1 |
| No recommendation engine / AI matching | LOCKED |
| No microservices | LOCKED |
| JWT without refresh flow for V1 | LOCKED |
| Expo SecureStore for mobile token storage | LOCKED |
| Clean Git history for new repository | LOCKED |

---

## 37. Items intentionally deferred to Technical Design

The following are **not open product decisions**. They are implementation details to be resolved in the next document:

- exact supported Java and Spring Boot versions;
- exact current Expo/React Native versions;
- package/module layout;
- SQL schema and constraints;
- Flyway migration naming;
- exact REST resource naming;
- request/response DTOs;
- validation constraints;
- JWT claims, TTL and signing configuration;
- error contract;
- CORS/environment strategy;
- Axios interceptor behavior;
- mobile secure-session bootstrap;
- Docker Compose details;
- CI workflow;
- deployment targets;
- test pyramid and exact tooling.

---

# FINAL BLUEPRINT VERDICT

**JOBIQ PRODUCT BLUEPRINT v0.1 - FINAL**

The V1 product boundary, recovery strategy and target architecture are frozen.

Next document: **JOBIQ - TECHNICAL DESIGN v0.1**.
