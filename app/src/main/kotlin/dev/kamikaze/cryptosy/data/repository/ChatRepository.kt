package dev.kamikaze.cryptosy.data.repository

import dev.kamikaze.cryptosy.domain.model.ChatItem
import dev.kamikaze.cryptosy.domain.model.ChatPayload
import dev.kamikaze.cryptosy.domain.model.ToolItem

interface ChatRepository {
    suspend fun sendMessage(message: String): Result<List<ChatItem>>
    suspend fun getSummary(): Result<List<ChatItem>>
    suspend fun getCryptoTools(): Result<List<ToolItem>>
    suspend fun getMoonTools(): Result<List<ToolItem>>
    suspend fun getAllTools(): Result<List<ToolItem>>
    suspend fun getCurrentMoonPhase(): Result<ChatPayload.MoonPhase>
    suspend fun saveMessages(messages: List<ChatItem>): Result<Unit>
    suspend fun loadMessages(): Result<List<ChatItem>>
}