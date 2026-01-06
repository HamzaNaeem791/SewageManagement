# Firebase Cloud Functions

This folder contains the Firebase callable function used by the Android app.

The Android client calls the callable function `createWorkerUser` from Firebase Functions.
If the app shows `NOT_FOUND`, it means the callable endpoint is not reachable (most commonly: functions not deployed, deployed to the wrong Firebase project, or deployed in a different region).

## Deploy

- Install Firebase CLI and login.
- From repo root:
  - `cd functions`
  - `npm install`
  - `npm run deploy`

### Windows / PowerShell quick start

1) Install Firebase CLI:

`npm install -g firebase-tools`

2) Login:

`firebase login`

3) Deploy to the same Firebase project as the Android app (`sewagemanagement-d1aef`):

`cd functions`

`npm install`

`firebase deploy --only functions --project sewagemanagement-d1aef`

## Callable functions

- `createWorkerUser` (callable)
  - Requires the caller to be authenticated and have `role=admin` in `users/{uid}`.
  - Creates a Firebase Auth user and a Firestore `users/{newUid}` document with `role=worker`.
