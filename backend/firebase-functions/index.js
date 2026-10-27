const { onRequest } = require("firebase-functions/v2/https");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue, Timestamp } = require("firebase-admin/firestore");

initializeApp();
const db = getFirestore();

const GAME_IDS = new Set([
  "warmup",
  "culture",
  "kingscup",
  "impostor",
  "never_have_i_ever",
  "truth_or_dare",
  "most_likely",
]);

const DEFAULT_GAME_ID = "warmup";
const WINDOW_DAYS = 7;
const MIN_SESSIONS = 30;
const LEAD_REQUIRED = 0.10;
const CACHE_MS = 6 * 60 * 60 * 1000;

exports.popularityApi = onRequest({ region: "us-central1", cors: false }, async (req, res) => {
  try {
    if (req.method === "POST" && req.path.endsWith("/session")) {
      await recordSession(req, res);
      return;
    }
    if (req.method === "GET" && req.path.endsWith("/featured")) {
      await getFeatured(res);
      return;
    }
    res.status(404).json({ error: "not_found" });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: "internal_error" });
  }
});

async function recordSession(req, res) {
  const { sessionId, gameId, startedAtEpochMs } = req.body || {};
  if (!GAME_IDS.has(gameId)) {
    res.status(400).json({ error: "invalid_game" });
    return;
  }
  if (typeof sessionId !== "string" || !/^[a-zA-Z0-9-]{16,64}$/.test(sessionId)) {
    res.status(400).json({ error: "invalid_session" });
    return;
  }

  const now = new Date();
  const dateKey = now.toISOString().slice(0, 10);
  const receiptRef = db.collection("sessionReceipts").doc(sessionId);
  const aggregateRef = db.collection("gamePopularityDaily").doc(`${dateKey}_${gameId}`);

  const wasCreated = await db.runTransaction(async (tx) => {
    const existing = await tx.get(receiptRef);
    if (existing.exists) return false;

    tx.create(receiptRef, {
      gameId,
      clientStartedAtEpochMs: Number.isFinite(startedAtEpochMs) ? startedAtEpochMs : null,
      createdAt: FieldValue.serverTimestamp(),
      expireAt: Timestamp.fromMillis(Date.now() + 30 * 24 * 60 * 60 * 1000),
    });
    tx.set(aggregateRef, {
      dateKey,
      gameId,
      count: FieldValue.increment(1),
      updatedAt: FieldValue.serverTimestamp(),
    }, { merge: true });
    return true;
  });

  res.status(200).json({ accepted: true, duplicate: !wasCreated });
}

async function getFeatured(res) {
  const configRef = db.collection("publicConfig").doc("featuredGame");
  const configSnap = await configRef.get();
  const cached = configSnap.exists ? configSnap.data() : null;
  const updatedMs = cached?.updatedAt?.toMillis?.() || 0;

  if (cached && Date.now() - updatedMs < CACHE_MS && GAME_IDS.has(cached.gameId)) {
    res.status(200).json(toPublicResponse(cached));
    return;
  }

  const start = new Date();
  start.setUTCDate(start.getUTCDate() - (WINDOW_DAYS - 1));
  const startKey = start.toISOString().slice(0, 10);

  const snapshots = await db.collection("gamePopularityDaily")
    .where("dateKey", ">=", startKey)
    .get();

  const totals = Object.fromEntries([...GAME_IDS].map((id) => [id, 0]));
  let totalSessions7d = 0;
  snapshots.forEach((doc) => {
    const data = doc.data();
    if (!GAME_IDS.has(data.gameId)) return;
    const count = Math.max(0, Number(data.count) || 0);
    totals[data.gameId] += count;
    totalSessions7d += count;
  });

  let gameId = DEFAULT_GAME_ID;
  let source = "default";

  if (totalSessions7d >= MIN_SESSIONS) {
    const winner = Object.entries(totals).sort((a, b) => b[1] - a[1])[0];
    const currentId = GAME_IDS.has(cached?.gameId) ? cached.gameId : DEFAULT_GAME_ID;
    const currentCount = totals[currentId] || 0;
    const [winnerId, winnerCount] = winner;

    if (
      winnerId === currentId ||
      currentCount === 0 ||
      winnerCount >= currentCount * (1 + LEAD_REQUIRED)
    ) {
      gameId = winnerId;
    } else {
      gameId = currentId;
    }
    source = "community";
  }

  const next = {
    gameId,
    source,
    totalSessions7d,
    windowDays: WINDOW_DAYS,
    minSessions: MIN_SESSIONS,
    leadRequired: LEAD_REQUIRED,
    updatedAt: FieldValue.serverTimestamp(),
  };
  await configRef.set(next, { merge: true });
  res.status(200).json({ gameId, source, totalSessions7d });
}

function toPublicResponse(data) {
  return {
    gameId: GAME_IDS.has(data.gameId) ? data.gameId : DEFAULT_GAME_ID,
    source: data.source === "community" ? "community" : "default",
    totalSessions7d: Math.max(0, Number(data.totalSessions7d) || 0),
  };
}
