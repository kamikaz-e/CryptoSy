package dev.kamikaze.cryptosy.data.repository

import dev.kamikaze.cryptosy.data.cache.CacheManager
import dev.kamikaze.cryptosy.data.dto.toDomain
import dev.kamikaze.cryptosy.data.remote.McpApi
import dev.kamikaze.cryptosy.domain.model.ChatItem
import dev.kamikaze.cryptosy.domain.model.ToolItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRepositoryImpl(
    private val mcpApi: McpApi,
    private val cacheManager: CacheManager
) : ChatRepository {

    private val _messagesFlow = MutableStateFlow<List<ChatItem>>(emptyList())
    private val messagesFlow: Flow<List<ChatItem>> = _messagesFlow.asStateFlow()

    override suspend fun sendMessage(message: String): Result<List<ChatItem>> {
        return mcpApi.sendMessage(message).map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun getSummary(): Result<List<ChatItem>> {
        return mcpApi.getSummary().map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun getTools(): Result<List<ToolItem>> {
        return mcpApi.getTools().map { dtos ->
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun saveMessages(messages: List<ChatItem>): Result<Unit> {
        _messagesFlow.value = messages
        return cacheManager.saveCache(messages)
    }

    override suspend fun loadMessages(): Result<List<ChatItem>> {
        return cacheManager.loadCache().map { cache ->
            val items = cache.items
            _messagesFlow.value = items
            items
        }
    }

    override fun observeMessages(): Flow<List<ChatItem>> = messagesFlow
}