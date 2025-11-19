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

    // Text
    val text: String? = null,

    // Prices
    val base: String? = null,
    val coins: List<CoinPriceDto>? = null,

    val name: String? = null,
    val now: FearGreedValueDto? = null,
    val yesterday: FearGreedValueDto? = null,
    val lastWeek: FearGreedValueDto? = null,

    // Старый формат для обратной совместимости
    val value: Int? = null,
    val label: String? = null,

    // News
    val items: List<NewsItemDto>? = null,

    // Coin
    val symbol: String? = null,
    val description: String? = null,
    val marketCap: Double? = null,
    val price: Double? = null,
    val change24h: Double? = null
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

// Mappers
fun ChatResponseItemDto.toDomain(): ChatItem {
    val payload = when (type) {
        "text" -> ChatPayload.Text(text ?: "")
        "prices" -> ChatPayload.Prices(
            base = base ?: "USD",
            coins = coins?.map { it.toDomain() } ?: emptyList()
        )
        "fearGreed" -> {
            // Новый формат с историей
            if (now != null) {
                ChatPayload.FearGreed(
                    name = name ?: "Fear and Greed Index",
                    now = now.toDomain(),
                    yesterday = yesterday?.toDomain(),
                    lastWeek = lastWeek?.toDomain()
                )
            } else {
                // Старый формат для обратной совместимости
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
        "coin" -> ChatPayload.Coin(
            symbol = symbol ?: "",
            name = name,
            description = description,
            marketCap = marketCap,
            price = price,
            change24h = change24h
        )
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

fun ToolItemDto.toDomain() = ToolItem(
    id = id,
    title = title,
    description = description,
    sampleQuery = sampleQuery
)