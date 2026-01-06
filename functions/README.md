# Firebase Cloud Functions

This folder contains the Firebase callable function used by the Android app.

## Deploy

- Install Firebase CLI and login.
- From repo root:
  - `cd functions`
  - `npm install`
  - `npm run deploy`

## Callable functions

- `createWorkerUser` (callable)
  - Requires the caller to be authenticated and have `role=admin` in `users/{uid}`.
  - Creates a Firebase Auth user and a Firestore `users/{newUid}` document with `role=worker`.
