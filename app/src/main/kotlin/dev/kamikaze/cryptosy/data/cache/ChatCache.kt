package dev.kamikaze.cryptosy.data.cache

import android.content.Context
import dev.kamikaze.cryptosy.domain.model.ChatItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File

@Serializable
data class ChatCache(
    val version: Int = 1,
    val savedAt: Long,
    val items: List<ChatItem>
)

class CacheManager(context: Context) {

    private val cacheFile = File(context.filesDir, "chat_cache.json")
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun saveCache(items: List<ChatItem>): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val cache = ChatCache(
                savedAt = System.currentTimeMillis(),
                items = items.takeLast(500) // Limit to last 500 messages
            )
            val jsonString = json.encodeToString(cache)
            cacheFile.writeText(jsonString)
            Timber.d("Cache saved: ${items.size} items")
        }.onFailure {
            Timber.e(it, "Failed to save cache")
        }
    }

    suspend fun loadCache(): Result<ChatCache> = withContext(Dispatchers.IO) {
        runCatching {
            if (!cacheFile.exists()) {
                Timber.d("Cache file doesn't exist")
                return@runCatching ChatCache(savedAt = 0, items = emptyList())
            }
            
            val jsonString = cacheFile.readText()
            val cache = json.decodeFromString<ChatCache>(jsonString)
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
}