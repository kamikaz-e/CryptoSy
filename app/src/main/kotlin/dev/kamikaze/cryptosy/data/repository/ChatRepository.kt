package dev.kamikaze.cryptosy.data.repository

import dev.kamikaze.cryptosy.domain.model.ChatItem
import dev.kamikaze.cryptosy.domain.model.ToolItem
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(message: String): Result<List<ChatItem>>
    suspend fun getSummary(): Result<List<ChatItem>>
    suspend fun getTools(): Result<List<ToolItem>>
    suspend fun saveMessages(messages: List<ChatItem>): Result<Unit>
    suspend fun loadMessages(): Result<List<ChatItem>>
    fun observeMessages(): Flow<List<ChatItem>>
}