# Kampai · Community Featured backend

Backend for the Home “featured game” ranking. The Android app never talks to Firestore directly: it only calls this HTTPS function anonymously.

## Behavior

- `POST /session`: accepts one anonymous game-session receipt and increments that game's daily counter. `sessionId` prevents duplicate uploads after retries.
- `GET /featured`: returns the featured game. It uses the last 7 UTC days, requires at least 30 valid sessions, and only changes the current winner when the challenger leads by at least 10%.
- Before the threshold is met, the response is `warmup` / `default`, so PartyMix is always available at launch.
- The computed winner is cached for 6 hours in `publicConfig/featuredGame`.
- No account, player name, location, advertising ID, or party contents are stored.

## Deploy

1. Create/choose a Firebase project and enable Firestore.
2. Initialize Cloud Functions (Node 20) and copy `index.js` + `package.json` into the functions folder.
3. Deploy `popularityApi` and Firestore rules.
4. Configure Firestore TTL for the `sessionReceipts.expireAt` field (optional but recommended).
5. Build Android with the deployed function URL:

```bash
./gradlew assembleRelease -PKAMPAI_POPULARITY_API_BASE_URL=https://<region>-<project>.cloudfunctions.net/popularityApi
```

The app has an empty URL by default. With no endpoint configured it remains fully functional, queues a bounded number of anonymous session receipts locally, and keeps PartyMix/cached featured as fallback.

## Hardening before large scale

Enable Firebase App Check for the HTTPS endpoint or place an App Check-aware callable/API layer in front of it. The session receipt deduplication prevents accidental retry inflation, but App Check is the proper anti-abuse layer for public production traffic.
