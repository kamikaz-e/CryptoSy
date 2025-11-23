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
    val sampleQuery: String,
    val source: String = "unknown"
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
        val id: String,
        val icon: String? = null,
        val symbol: String,
        val name: String? = null,
        val rank: Int? = null,
        val price: Double? = null,
        val priceBtc: Double? = null,
        val volume: Double? = null,
        val marketCap: Double? = null,
        val availableSupply: Double? = null,
        val totalSupply: Double? = null,
        val fullyDilutedValuation: Double? = null,
        val priceChange1h: Double? = null,
        val priceChange1d: Double? = null,
        val priceChange1w: Double? = null,
        val websiteUrl: String? = null,
        val redditUrl: String? = null,
        val twitterUrl: String? = null,
        val contractAddress: String? = null,
        val contractAddresses: List<ContractAddress>? = null,
        val decimals: Int? = null,
        val explorers: List<String>? = null,
        val liquidityScore: Double? = null,
        val volatilityScore: Double? = null,
        val marketCapScore: Double? = null,
        val riskScore: Double? = null,
        val avgChange: Double? = null,
        val description: String? = null,
        val change24h: Double? = null
    ) : ChatPayload() {
        val displaySymbol: String
            get() = symbol.uppercase()

        val displayName: String
            get() = name ?: symbol
    }

    @Serializable
    data class MoonPhase(
        val phase: String,
        val phaseEmoji: String,
        val waxing: Boolean,
        val waning: Boolean,
        val lunarAge: Double,
        val lunarAgePercent: Double,
        val lunationNumber: Int,
        val lunarDistance: Double,
        val nextFullMoon: String?,
        val lastFullMoon: String?,
        val cryptoPrediction: CryptoPrediction? = null
    ) : ChatPayload()

    @Serializable
    data class CombinedReport(
        val moonPhase: MoonPhase,
        val marketSummary: String,
        val prediction: CryptoPrediction
    ) : ChatPayload()
}

@Serializable
data class CryptoPrediction(
    val trend: String,
    val confidence: String,
    val reasoning: String,
    val recommendation: String
)

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
        get() = symbol.uppercase()

    val displayName: String
        get() = name ?: symbol
}

@Serializable
data class NewsItem(
    val title: String,
    val source: String?,
    val time: Long,
    val url: String?
)

@Serializable
data class ContractAddress(
    val blockchain: String,
    val contractAddress: String
)