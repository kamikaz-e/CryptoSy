package dev.kamikaze.cryptosy.data.dto

import dev.kamikaze.cryptosy.domain.model.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatResponseDto(
    val items: List<ChatResponseItemDto>
)

@Serializable
data class ChatResponseItemDto(
    val type: String,
    val ts: Long = System.currentTimeMillis(),
    val text: String? = null,
    val base: String? = null,
    val coins: List<CoinPriceDto>? = null,
    val name: String? = null,
    val now: FearGreedValueDto? = null,
    val yesterday: FearGreedValueDto? = null,
    val lastWeek: FearGreedValueDto? = null,
    val value: Int? = null,
    val label: String? = null,
    val items: List<NewsItemDto>? = null,
    val symbol: String? = null,
    val description: String? = null,
    val marketCap: Double? = null,
    val price: Double? = null,
    val change24h: Double? = null,
    val id: String? = null,
    val icon: String? = null,
    val rank: Int? = null,
    val priceBtc: Double? = null,
    val volume: Double? = null,
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
    val contractAddresses: List<ContractAddressDto>? = null,
    val decimals: Int? = null,
    val explorers: List<String>? = null,
    val liquidityScore: Double? = null,
    val volatilityScore: Double? = null,
    val marketCapScore: Double? = null,
    val riskScore: Double? = null,
    val avgChange: Double? = null,
    val moonPhase: MoonPhaseDto? = null
)

@Serializable
data class FearGreedValueDto(
    val value: Int,
    @SerialName("value_classification") val valueClassification: String,
    val timestamp: Long,
    @SerialName("update_time") val updateTime: String? = null
)

@Serializable
data class CoinPriceDto(
    val symbol: String,
    val name: String?,
    val price: Double,
    val change1hPct: Double?,
    val change1hAbs: Double?
)

@Serializable
data class NewsItemDto(
    val title: String,
    val source: String?,
    val time: Long,
    val url: String?
)

@Serializable
data class ContractAddressDto(
    val blockchain: String,
    @SerialName("contractAddress") val contractAddress: String
)

@Serializable
data class ToolsResponseDto(
    val tools: List<ToolItemDto>
)

@Serializable
data class ToolItemDto(
    val id: String,
    val title: String,
    val description: String,
    val sampleQuery: String
)

@Serializable
data class MoonPhaseDto(
    val phase: String,
    val phaseEmoji: String,
    val waxing: Boolean,
    val waning: Boolean,
    val lunarAge: Double,
    val lunarAgePercent: Double,
    val lunationNumber: Int,
    val lunarDistance: Double,
    val nextFullMoon: String,
    val lastFullMoon: String,
    val cryptoPrediction: CryptoPredictionDto? = null
)

@Serializable
data class CryptoPredictionDto(
    val trend: String,
    val confidence: String,
    val reasoning: String,
    val recommendation: String
)

fun ChatResponseItemDto.toDomain(): ChatItem {
    val payload = when (type) {
        "text" -> ChatPayload.Text(text ?: "")
        "prices" -> ChatPayload.Prices(
            base = base ?: "USD",
            coins = coins?.map { it.toDomain() } ?: emptyList()
        )
        "fearGreed" -> {
            if (now != null) {
                ChatPayload.FearGreed(
                    name = name ?: "Fear and Greed Index",
                    now = now.toDomain(),
                    yesterday = yesterday?.toDomain(),
                    lastWeek = lastWeek?.toDomain()
                )
            } else {
                ChatPayload.FearGreed(
                    now = FearGreedValue(
                        value = value ?: 50,
                        valueClassification = label ?: "Neutral",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
        "news" -> ChatPayload.News(
            items = items?.map { it.toDomain() } ?: emptyList()
        )
        "moonPhase" -> ChatPayload.MoonPhase(
            phase = moonPhase?.phase ?: "Unknown",
            phaseEmoji = moonPhase?.phaseEmoji ?: "🌙",
            waxing = moonPhase?.waxing ?: false,
            waning = moonPhase?.waning ?: false,
            lunarAge = moonPhase?.lunarAge ?: 0.0,
            lunarAgePercent = moonPhase?.lunarAgePercent ?: 0.0,
            lunationNumber = moonPhase?.lunationNumber ?: 0,
            lunarDistance = moonPhase?.lunarDistance ?: 0.0,
            nextFullMoon = moonPhase?.nextFullMoon,
            lastFullMoon = moonPhase?.lastFullMoon,
            cryptoPrediction = moonPhase?.cryptoPrediction?.toDomain()
        )
        "coin" -> {
            if (id != null || rank != null || icon != null) {
                ChatPayload.Coin(
                    id = id ?: "",
                    icon = icon,
                    symbol = symbol ?: "",
                    name = name,
                    rank = rank,
                    price = price,
                    priceBtc = priceBtc,
                    volume = volume,
                    marketCap = marketCap,
                    availableSupply = availableSupply,
                    totalSupply = totalSupply,
                    fullyDilutedValuation = fullyDilutedValuation,
                    priceChange1h = priceChange1h,
                    priceChange1d = priceChange1d,
                    priceChange1w = priceChange1w,
                    websiteUrl = websiteUrl,
                    redditUrl = redditUrl,
                    twitterUrl = twitterUrl,
                    contractAddress = contractAddress,
                    contractAddresses = contractAddresses?.map { it.toDomain() },
                    decimals = decimals,
                    explorers = explorers,
                    liquidityScore = liquidityScore,
                    volatilityScore = volatilityScore,
                    marketCapScore = marketCapScore,
                    riskScore = riskScore,
                    avgChange = avgChange,
                    description = description,
                    change24h = priceChange1d ?: change24h
                )
            } else {
                ChatPayload.Coin(
                    id = symbol ?: "",
                    symbol = symbol ?: "",
                    name = name,
                    description = description,
                    marketCap = marketCap,
                    price = price,
                    change24h = change24h
                )
            }
        }
        else -> ChatPayload.Text("Unknown type: $type")
    }

    return ChatItem(
        ts = ts,
        role = ChatRole.ASSISTANT,
        payload = payload
    )
}

fun FearGreedValueDto.toDomain() = FearGreedValue(
    value = value,
    valueClassification = valueClassification,
    timestamp = timestamp,
    updateTime = updateTime
)

fun CoinPriceDto.toDomain() = CoinPrice(
    symbol = symbol,
    name = name,
    price = price,
    change1hPct = change1hPct,
    change1hAbs = change1hAbs
)

fun NewsItemDto.toDomain() = NewsItem(
    title = title,
    source = source,
    time = time,
    url = url
)

fun ToolItemDto.toDomain(source: String = "unknown") = ToolItem(
    id = id,
    title = title,
    description = description,
    sampleQuery = sampleQuery,
    source = source
)

fun ContractAddressDto.toDomain() = ContractAddress(
    blockchain = blockchain,
    contractAddress = contractAddress
)

fun MoonPhaseDto.toDomain() = ChatPayload.MoonPhase(
    phase = phase,
    phaseEmoji = phaseEmoji,
    waxing = waxing,
    waning = waning,
    lunarAge = lunarAge,
    lunarAgePercent = lunarAgePercent,
    lunationNumber = lunationNumber,
    lunarDistance = lunarDistance,
    nextFullMoon = nextFullMoon,
    lastFullMoon = lastFullMoon,
    cryptoPrediction = cryptoPrediction?.toDomain()
)

fun CryptoPredictionDto.toDomain() = CryptoPrediction(
    trend = trend,
    confidence = confidence,
    reasoning = reasoning,
    recommendation = recommendation
)