# Jobiq recovery history

An older local Jobiq mobile prototype existed before the V1 repository was created.

The prototype was audited and contained a partially recoverable Expo / React Native foundation, including useful concepts around routing, theming and early authentication structure.

The approved V1 decision is **CLEAN MIGRATION**:

- the old project is not the implementation baseline;
- the old `.git` history is not imported into `jzapataa/jobiq`;
- old `node_modules`, `.expo`, `.env`, lockfiles and starter assets are not migrated;
- useful pieces will be migrated selectively in a later slice;
- no old source code is copied during Slice 0.

The historical empty `jzapataa/user-service` repository is not part of the new Jobiq implementation.
