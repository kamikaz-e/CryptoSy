package dev.kamikaze.cryptosy.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ChatItem(
    val id: String = UUID.randomUUID().toString(),
    val ts: Long = System.currentTimeMillis(),
    val role: ChatRole,
    val payload: ChatPayload
)

@Serializable
enum class ChatRole {
    USER,
    ASSISTANT,
    SYSTEM,
    TOOL
}

@Serializable
data class ToolItem(
    val id: String,
    val title: String,
    val description: String,
    val sampleQuery: String
)
@Serializable
sealed class ChatPayload {

    @Serializable
    data class Text(val text: String) : ChatPayload()

    @Serializable
    data class Prices(
        val base: String,
        val coins: List<CoinPrice>
    ) : ChatPayload()

    @Serializable
    data class FearGreed(
        val name: String = "Fear and Greed Index",
        val now: FearGreedValue,
        val yesterday: FearGreedValue? = null,
        val lastWeek: FearGreedValue? = null
    ) : ChatPayload()

    @Serializable
    data class News(
        val items: List<NewsItem>
    ) : ChatPayload()

    @Serializable
    data class Coin(
        val symbol: String,
        val name: String?,
        val description: String?,
        val marketCap: Double?,
        val price: Double?,
        val change24h: Double?
    ) : ChatPayload()
}

@Serializable
data class FearGreedValue(
    val value: Int,
    val valueClassification: String,
    val timestamp: Long,
    val updateTime: String? = null
)

@Serializable
data class CoinPrice(
    val symbol: String,
    val name: String?,
    val price: Double,
    val change1hPct: Double?,
    val change1hAbs: Double?
) {
    val displaySymbol: String
        get() = when (symbol.uppercase()) {
            "ASTER" -> "ASTR"
            else -> symbol.uppercase()
        }

    val displayName: String
        get() = when (symbol.uppercase()) {
            "ASTR", "ASTER" -> "Astar"
            else -> name ?: symbol
        }
}

@Serializable
data class NewsItem(
    val title: String,
    val source: String?,
    val time: Long,
    val url: String?
)