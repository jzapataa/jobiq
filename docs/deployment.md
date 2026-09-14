# Jobiq deployment baseline

This document records the approved remote infrastructure configuration only. Slice 0 does **not** provision or deploy any remote service.

## DEV

```text
Render service: jobiq-api-dev
Branch: develop
Root path: backend
Runtime: Docker
Region: Frankfurt
Health path: /actuator/health/liveness

Neon project: jobiq-dev
PostgreSQL major: 18
Region: Frankfurt / Europe Central
Database: jobiq
Connection: direct read/write endpoint
TLS: required
Pooler: not used
```

## PROD

```text
Render service: jobiq-api-prod
Branch: main
Root path: backend
Runtime: Docker
Region: Frankfurt
Health path: /actuator/health/liveness

Neon project: jobiq-prod
PostgreSQL major: 18
Region: Frankfurt / Europe Central
Database: jobiq
```

## Backend environment variable names

The following names are expected by the approved Technical Design:

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

No real secret values belong in Git.

Recommended remote JVM baseline through `JAVA_TOOL_OPTIONS`:

```text
-Xms64m -Xmx256m -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError
```

Remote Tomcat baseline:

```text
max threads: 20
min spare threads: 2
```

Remote Hikari baseline:

```text
maximumPoolSize: 3
minimumIdle: 0
connectionTimeout: ~10s
idleTimeout: ~60s
keepalive: disabled
```

## Mobile public configuration

Only public client configuration is exposed through `EXPO_PUBLIC_*` variables:

```text
EXPO_PUBLIC_APP_ENV
EXPO_PUBLIC_API_URL
EXPO_PUBLIC_API_TIMEOUT_MS
```

Expected remote API URLs initially:

```text
DEV  https://jobiq-api-dev.onrender.com
PROD https://jobiq-api-prod.onrender.com
```

EAS environment mapping:

```text
development -> DEV API
preview -> DEV API
production -> PROD API
```

No EAS build is executed during Slice 0.

## Liveness policy

Render must probe only:

```text
GET /actuator/health/liveness
```

The liveness group contains `livenessState` only. It does not query PostgreSQL and must not wake Neon.

No readiness probe, external ping, uptime bot or keep-alive traffic is configured for V1.

## Zero-cost policy

Jobiq V1 uses free tiers only. No payment card, pay-as-you-go activation or automatic billing is introduced. If a selected provider requires a payment method, provider selection must be reopened rather than adding a card.

## Accepted deviations

### DEV-DEPLOY-01 — Neon provisioning deviation

`jobiq-dev` is provisioned inside a Neon organization managed by Vercel because the current Neon access does not allow creating a standalone Neon project directly.

```text
Architecture impact: NONE
Runtime impact: NONE
Product impact: NONE
Portability impact: LOW
Accepted for V1: YES
```

Vercel is not part of the Jobiq runtime. Render connects directly to Neon through standard PostgreSQL JDBC/TLS.

## Observations

### DEV-OBS-01 — Render Free cold start

The observed DEV startup took approximately 102 seconds from Spring startup to `Started JobiqApplication` on Render Free.

This does not block Slice 1. `EXPO_PUBLIC_API_TIMEOUT_MS` is not changed here; the remote mobile timeout should be reevaluated later in the appropriate mobile/UX slice using this evidence.
