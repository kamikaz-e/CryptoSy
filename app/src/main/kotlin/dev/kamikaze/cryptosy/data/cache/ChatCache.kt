package dev.kamikaze.cryptosy.data.cache

import android.content.Context
import dev.kamikaze.cryptosy.data.dto.MoonPhaseDto
import dev.kamikaze.cryptosy.domain.model.ChatItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File

@Serializable
data class ChatCacheData(
    val version: Int = 1,
    val savedAt: Long,
    val items: List<ChatItem>
)

@Serializable
data class MoonPhaseCache(
    val moonPhase: MoonPhaseDto,
    val cachedAt: Long
)

class CacheManager(context: Context) {

    private val cacheFile = File(context.filesDir, "chat_cache.json")
    private val moonPhaseCacheFile = File(context.filesDir, "moon_phase_cache.json")
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    companion object {
        private const val ONE_DAY_MS = 24 * 60 * 60 * 1000L
    }

    suspend fun saveCache(items: List<ChatItem>): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cache = ChatCacheData(
                savedAt = System.currentTimeMillis(),
                items = items.takeLast(500)
            )
            val jsonString = json.encodeToString(ChatCacheData.serializer(), cache)
            cacheFile.writeText(jsonString)
            Timber.d("Cache saved: ${items.size} items")
        }.onFailure {
            Timber.e(it, "Failed to save cache")
        }
    }

    suspend fun loadCache(): Result<ChatCacheData> = withContext(Dispatchers.IO) {
        runCatching {
            if (!cacheFile.exists()) {
                Timber.d("Cache file doesn't exist")
                return@runCatching ChatCacheData(savedAt = 0, items = emptyList())
            }

            val jsonString = cacheFile.readText()
            val cache = json.decodeFromString<ChatCacheData>(jsonString)
            Timber.d("Cache loaded: ${cache.items.size} items")
            cache
        }.onFailure {
            Timber.e(it, "Failed to load cache")
        }
    }

    suspend fun clearCache(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (cacheFile.exists()) {
                cacheFile.delete()
                Timber.d("Cache cleared")
            }
        }
    }

    suspend fun saveMoonPhase(moonPhase: MoonPhaseDto): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cache = MoonPhaseCache(
                moonPhase = moonPhase,
                cachedAt = System.currentTimeMillis()
            )
            val jsonString = json.encodeToString(MoonPhaseCache.serializer(), cache)
            moonPhaseCacheFile.writeText(jsonString)
            Timber.d("Moon phase cached at ${cache.cachedAt}")
        }.onFailure {
            Timber.e(it, "Failed to save moon phase cache")
        }
    }

    suspend fun loadMoonPhase(): Result<MoonPhaseDto?> = withContext(Dispatchers.IO) {
        runCatching {
            if (!moonPhaseCacheFile.exists()) {
                Timber.d("Moon phase cache file doesn't exist")
                return@runCatching null
            }

            val jsonString = moonPhaseCacheFile.readText()
            val cache = json.decodeFromString<MoonPhaseCache>(jsonString)
            Timber.d("Moon phase cache loaded, cached at: ${cache.cachedAt}")
            cache.moonPhase
        }.onFailure {
            Timber.e(it, "Failed to load moon phase cache")
            null
        }
    }

    suspend fun isMoonPhaseCacheValid(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            if (!moonPhaseCacheFile.exists()) {
                return@runCatching false
            }

            val jsonString = moonPhaseCacheFile.readText()
            val cache = json.decodeFromString<MoonPhaseCache>(jsonString)
            val now = System.currentTimeMillis()
            val age = now - cache.cachedAt

            val isValid = age < ONE_DAY_MS
            Timber.d("Moon phase cache age: ${age / (60 * 60 * 1000)} hours, valid: $isValid")
            isValid
        }.getOrElse {
            Timber.e(it, "Failed to check moon phase cache validity")
            false
        }
    }
}