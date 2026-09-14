# Jobiq recovery history

An older local Jobiq mobile prototype existed before the V1 repository was created.

The original audited source archive was used only as **RECOVERED SOURCE** for the Slice 2 clean migration. The authoritative implementation baseline remains the clean Expo SDK 57 application in `jzapataa/jobiq/mobile`.

## Slice 2 clean migration

The recovered project was not upgraded in place and no directory was copied wholesale.

Migrated or adapted selectively:

- public routing concept using Expo Router route groups;
- root-layout font loading and status-bar behavior, adapted to the current SDK 57 foundation;
- recovered login visual structure without authentication behavior;
- the useful focus/theme behavior of the recovered text input;
- the useful themed text concept;
- the intentional Jobiq primary color and selected light/dark theme values;
- Montserrat and Poppins font assets used by the migrated UI;
- the Zustand authentication-state concept as a deterministic local shape only.

The Slice 2 authentication state contains only structural fields for future integration:

```text
status
user
accessToken
bootstrapError
```

It boots deterministically to `unauthenticated`. Slice 2 does not perform remote session restoration, login, registration, logout, token persistence or authenticated API calls.

Explicitly not migrated:

- old `.git` history;
- old `.expo` state;
- old `node_modules`;
- recovered `.env` / `.env.template`;
- recovered `package.json` and lockfile;
- recovered `app.json`;
- old Axios environment configuration and platform-specific API URLs;
- recovered login/check-status actions;
- the buggy recovered auth store implementation;
- starter/demo assets and React logos;
- starter helpers without a current Jobiq use case;
- recovered home placeholder and any candidate/recruiter/business screens.

Environment configuration was not reused from the prototype. The clean V1 foundation remains authoritative and uses only the approved public variables:

```text
EXPO_PUBLIC_APP_ENV
EXPO_PUBLIC_API_URL
EXPO_PUBLIC_API_TIMEOUT_MS
```

No secrets, recovered credentials or legacy Git history were imported.
