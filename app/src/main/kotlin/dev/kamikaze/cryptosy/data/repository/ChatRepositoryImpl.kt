package dev.kamikaze.cryptosy.data.repository

import dev.kamikaze.cryptosy.data.cache.CacheManager
import dev.kamikaze.cryptosy.data.dto.toDomain
import dev.kamikaze.cryptosy.data.remote.McpApi
import dev.kamikaze.cryptosy.data.remote.MoonPhasesApi
import dev.kamikaze.cryptosy.domain.model.ChatItem
import dev.kamikaze.cryptosy.domain.model.ChatPayload
import dev.kamikaze.cryptosy.domain.model.ChatRole
import dev.kamikaze.cryptosy.domain.model.ToolItem
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

class ChatRepositoryImpl(
    private val mcpApi: McpApi,
    private val moonPhasesApi: MoonPhasesApi,
    private val cache: CacheManager
) : ChatRepository {

    private val moonKeywords = listOf(
        "moon", "lunar", "луна", "лунн", "фаз", "phase",
        "полнолуние", "новолуние", "full moon", "new moon",
        "waxing", "waning", "растущая", "убывающая",
        "lunation", "лунация", "moon phase"
    )

    private fun detectMessageContext(message: String): String {
        val lowerMessage = message.lowercase()

        val containsMoonKeyword = moonKeywords.any { keyword ->
            lowerMessage.contains(keyword)
        }

        return if (containsMoonKeyword) {
            Timber.d("Message context detected: MOON - '$message'")
            "moon"
        } else {
            Timber.d("Message context detected: CRYPTO - '$message'")
            "crypto"
        }
    }

    override suspend fun sendMessage(message: String): Result<List<ChatItem>> {
        val context = detectMessageContext(message)

        return when (context) {
            "moon" -> {
                Timber.d("Routing to Moon API: $message")
                moonPhasesApi.sendMessage(message).map { dto ->
                    dto.items.map { it.toDomain() }
                }
            }
            else -> {
                Timber.d("Routing to Crypto API: $message")
                mcpApi.sendMessage(message).map { dto ->
                    dto.items.map { it.toDomain() }
                }
            }
        }
    }

    override suspend fun getSummary(): Result<List<ChatItem>> = coroutineScope {
        try {
            Timber.d("Starting combined summary fetch (crypto + moon)...")

            val cryptoSummaryDeferred = async {
                mcpApi.getSummary().map { dto ->
                    dto.items.map { it.toDomain() }
                }
            }

            val moonPhaseDeferred = async {
                getMoonPhaseWithCache()
            }

            val cryptoResult = cryptoSummaryDeferred.await()
            val moonResult = moonPhaseDeferred.await()

            val resultItems = mutableListOf<ChatItem>()

            moonResult.onSuccess { moonPhase ->
                Timber.d("Moon phase received: ${moonPhase.phase}")
                resultItems.add(
                    ChatItem(
                        role = ChatRole.ASSISTANT,
                        payload = moonPhase
                    )
                )
            }.onFailure { error ->
                Timber.e(error, "Failed to fetch moon phase")
                resultItems.add(
                    ChatItem(
                        role = ChatRole.SYSTEM,
                        payload = ChatPayload.Text("⚠️ Failed to load moon phase: ${error.message}")
                    )
                )
            }

            cryptoResult.onSuccess { items ->
                Timber.d("Crypto summary received: ${items.size} items")
                resultItems.addAll(items)
            }.onFailure { error ->
                Timber.e(error, "Failed to fetch crypto summary")
                resultItems.add(
                    ChatItem(
                        role = ChatRole.SYSTEM,
                        payload = ChatPayload.Text("⚠️ Failed to load crypto data: ${error.message}")
                    )
                )
            }

            if (cryptoResult.isSuccess && moonResult.isSuccess) {
                val moonPhase = moonResult.getOrNull()!!
                val prediction = moonPhase.cryptoPrediction

                if (prediction != null) {
                    val summaryText = buildString {
                        appendLine("📊 **MARKET ANALYSIS COMPLETE**")
                        appendLine()
                        appendLine("🌙 **Lunar Phase**: ${moonPhase.phase} ${moonPhase.phaseEmoji}")
                        appendLine("📈 **Trend Prediction**: ${prediction.trend.uppercase()}")
                        appendLine("🎯 **Confidence**: ${prediction.confidence.uppercase()}")
                        appendLine()
                        appendLine("**Analysis**: ${prediction.reasoning}")
                        appendLine()
                        appendLine("**Recommendation**: ${prediction.recommendation}")
                        appendLine()
                        appendLine("_Based on historical moon phase correlation with crypto markets and current market data._")
                    }

                    resultItems.add(
                        ChatItem(
                            role = ChatRole.ASSISTANT,
                            payload = ChatPayload.Text(summaryText)
                        )
                    )
                }
            }

            Timber.d("Combined summary completed with ${resultItems.size} items")
            Result.success(resultItems)

        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch combined summary")
            Result.failure(e)
        }
    }

    private suspend fun getMoonPhaseWithCache(): Result<ChatPayload.MoonPhase> {
        val isCacheValid = cache.isMoonPhaseCacheValid()

        return if (isCacheValid) {
            Timber.d("Using cached moon phase")
            cache.loadMoonPhase().fold(
                onSuccess = { dto ->
                    if (dto != null) {
                        Result.success(dto.toDomain())
                    } else {
                        Timber.d("Cache is empty, fetching from API")
                        fetchAndCacheMoonPhase()
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to load cache, fetching from API")
                    fetchAndCacheMoonPhase()
                }
            )
        } else {
            Timber.d("Cache invalid or missing, fetching from API")
            fetchAndCacheMoonPhase()
        }
    }

    private suspend fun fetchAndCacheMoonPhase(): Result<ChatPayload.MoonPhase> {
        return moonPhasesApi.getCurrentMoonPhase()
            .onSuccess { dto ->
                cache.saveMoonPhase(dto)
                    .onFailure { error ->
                        Timber.e(error, "Failed to save moon phase to cache")
                    }
            }
            .map { dto -> dto.toDomain() }
    }

    override suspend fun getCryptoTools(): Result<List<ToolItem>> {
        return mcpApi.getTools().map { dto ->
            dto.tools.map { it.toDomain("crypto") }
        }
    }

    override suspend fun getMoonTools(): Result<List<ToolItem>> {
        return moonPhasesApi.getTools().map { dto ->
            dto.tools.map { it.toDomain("moon") }
        }
    }

    override suspend fun getAllTools(): Result<List<ToolItem>> {
        val cryptoTools = getCryptoTools().getOrElse { emptyList() }
        val moonTools = getMoonTools().getOrElse { emptyList() }

        val allTools = cryptoTools + moonTools
        Timber.d("Loaded ${cryptoTools.size} crypto tools and ${moonTools.size} moon tools")

        return Result.success(allTools)
    }

    override suspend fun getCurrentMoonPhase(): Result<ChatPayload.MoonPhase> {
        return getMoonPhaseWithCache()
    }

    override suspend fun saveMessages(messages: List<ChatItem>): Result<Unit> {
        return cache.saveCache(messages)
    }

    override suspend fun loadMessages(): Result<List<ChatItem>> {
        return cache.loadCache().map { it.items }
    }
}