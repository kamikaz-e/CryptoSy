package dev.kamikaze.cryptosy.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequestDto(
    val message: String,
    val context: ChatContextDto? = null
)

@Serializable
data class ChatContextDto(
    val locale: String = "ru-RU",
    val timezone: String = "Europe/Moscow"
)