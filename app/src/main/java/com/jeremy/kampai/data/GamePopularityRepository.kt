package com.jeremy.kampai.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeremy.kampai.BuildConfig
import com.jeremy.kampai.domain.models.FeaturedGameSnapshot
import com.jeremy.kampai.domain.models.FeaturedGameSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamePopularityRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val gameRepository: GameRepository
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val apiBaseUrl = BuildConfig.KAMPAI_POPULARITY_API_BASE_URL.trim().trimEnd('/')

    fun cachedFeatured(): FeaturedGameSnapshot {
        val gameId = prefs.getString(KEY_FEATURED_GAME_ID, null)
            ?.takeIf { gameRepository.getGameById(it) != null }
            ?: DEFAULT_FEATURED_GAME_ID
        val source = runCatching {
            FeaturedGameSource.valueOf(
                prefs.getString(KEY_FEATURED_SOURCE, FeaturedGameSource.DEFAULT.name)
                    ?: FeaturedGameSource.DEFAULT.name
            )
        }.getOrDefault(FeaturedGameSource.DEFAULT)

        return FeaturedGameSnapshot(
            gameId = gameId,
            source = source,
            totalSessions7d = prefs.getInt(KEY_FEATURED_TOTAL_7D, 0)
        )
    }

    suspend fun refreshFeatured(): FeaturedGameSnapshot = withContext(Dispatchers.IO) {
        if (apiBaseUrl.isBlank()) return@withContext cachedFeatured()

        flushPendingSessionsInternal()
        val remote = fetchFeaturedInternal() ?: return@withContext cachedFeatured()
        val safeSnapshot = if (gameRepository.getGameById(remote.gameId) != null) {
            remote
        } else {
            FeaturedGameSnapshot(DEFAULT_FEATURED_GAME_ID, FeaturedGameSource.DEFAULT, 0)
        }
        cacheFeatured(safeSnapshot)
        safeSnapshot
    }

    suspend fun recordGameSession(gameId: String) = withContext(Dispatchers.IO) {
        if (gameRepository.getGameById(gameId) == null) return@withContext

        incrementLocalCount(gameId)
        val pending = PendingGameSession(
            sessionId = UUID.randomUUID().toString(),
            gameId = gameId,
            startedAtEpochMs = System.currentTimeMillis()
        )

        val queue = loadPendingSessions().toMutableList().apply { add(pending) }
        if (apiBaseUrl.isBlank()) {
            savePendingSessions(queue.takeLast(MAX_PENDING_SESSIONS))
            return@withContext
        }

        val remaining = mutableListOf<PendingGameSession>()
        queue.takeLast(MAX_PENDING_SESSIONS).forEach { session ->
            if (!postSessionInternal(session)) remaining += session
        }
        savePendingSessions(remaining)
    }

    private fun fetchFeaturedInternal(): FeaturedGameSnapshot? {
        val connection = openConnection("$apiBaseUrl/featured", "GET")
        return try {
            val code = connection.responseCode
            if (code !in 200..299) return null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val response = gson.fromJson(body, FeaturedApiResponse::class.java) ?: return null
            FeaturedGameSnapshot(
                gameId = response.gameId,
                source = if (response.source.equals("community", ignoreCase = true)) {
                    FeaturedGameSource.COMMUNITY
                } else {
                    FeaturedGameSource.DEFAULT
                },
                totalSessions7d = response.totalSessions7d.coerceAtLeast(0)
            )
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun postSessionInternal(session: PendingGameSession): Boolean {
        val connection = openConnection("$apiBaseUrl/session", "POST")
        return try {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(gson.toJson(session))
            }
            connection.responseCode in 200..299
        } catch (_: Exception) {
            false
        } finally {
            connection.disconnect()
        }
    }

    private fun flushPendingSessionsInternal() {
        if (apiBaseUrl.isBlank()) return
        val pending = loadPendingSessions()
        if (pending.isEmpty()) return

        val remaining = pending.filterNot { postSessionInternal(it) }
        savePendingSessions(remaining.takeLast(MAX_PENDING_SESSIONS))
    }

    private fun openConnection(url: String, method: String): HttpURLConnection {
        return (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = NETWORK_TIMEOUT_MS
            readTimeout = NETWORK_TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            useCaches = false
        }
    }

    private fun cacheFeatured(snapshot: FeaturedGameSnapshot) {
        prefs.edit()
            .putString(KEY_FEATURED_GAME_ID, snapshot.gameId)
            .putString(KEY_FEATURED_SOURCE, snapshot.source.name)
            .putInt(KEY_FEATURED_TOTAL_7D, snapshot.totalSessions7d)
            .apply()
    }

    private fun incrementLocalCount(gameId: String) {
        val key = "$KEY_LOCAL_COUNT_PREFIX$gameId"
        prefs.edit().putLong(key, prefs.getLong(key, 0L) + 1L).apply()
    }

    private fun loadPendingSessions(): List<PendingGameSession> {
        val raw = prefs.getString(KEY_PENDING_SESSIONS, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<PendingGameSession>>() {}.type
            gson.fromJson<List<PendingGameSession>>(raw, type) ?: emptyList()
        }.getOrDefault(emptyList())
    }

    private fun savePendingSessions(sessions: List<PendingGameSession>) {
        prefs.edit().putString(KEY_PENDING_SESSIONS, gson.toJson(sessions)).apply()
    }

    private data class PendingGameSession(
        val sessionId: String,
        val gameId: String,
        val startedAtEpochMs: Long
    )

    private data class FeaturedApiResponse(
        val gameId: String = DEFAULT_FEATURED_GAME_ID,
        val source: String = "default",
        val totalSessions7d: Int = 0
    )

    companion object {
        const val DEFAULT_FEATURED_GAME_ID = "warmup"
        private const val PREFS_NAME = "kampai_game_popularity"
        private const val KEY_FEATURED_GAME_ID = "featured_game_id"
        private const val KEY_FEATURED_SOURCE = "featured_source"
        private const val KEY_FEATURED_TOTAL_7D = "featured_total_7d"
        private const val KEY_PENDING_SESSIONS = "pending_sessions"
        private const val KEY_LOCAL_COUNT_PREFIX = "local_count_"
        private const val MAX_PENDING_SESSIONS = 100
        private const val NETWORK_TIMEOUT_MS = 3_000
    }
}
