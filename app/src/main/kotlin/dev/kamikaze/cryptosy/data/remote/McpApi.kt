package dev.kamikaze.cryptosy.data.remote

import dev.kamikaze.cryptosy.data.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber

interface McpApi {
    suspend fun getTools(): Result<List<ToolItemDto>>
    suspend fun sendMessage(message: String): Result<List<ChatResponseItemDto>>
    suspend fun getSummary(): Result<List<ChatResponseItemDto>>
}

class McpApiImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : McpApi {

    override suspend fun getTools(): Result<List<ToolItemDto>> = runCatching {
        Timber.d("Fetching tools from $baseUrl/tools")
        val response: ToolsResponseDto = client.get("$baseUrl/tools").body()
        response.tools
    }.onFailure { 
        Timber.e(it, "Failed to fetch tools")
    }

    override suspend fun sendMessage(message: String): Result<List<ChatResponseItemDto>> = runCatching {
        Timber.d("Sending message: $message")
        val request = ChatRequestDto(
            message = message,
            context = ChatContextDto()
        )
        val response: ChatResponseDto = client.post("$baseUrl/chat") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        response.items
    }.onFailure { 
        Timber.e(it, "Failed to send message")
    }

    override suspend fun getSummary(): Result<List<ChatResponseItemDto>> = runCatching {
        Timber.d("Fetching summary")
        val response: ChatResponseDto = client.get("$baseUrl/summary").body()
        response.items
    }.onFailure { 
        Timber.e(it, "Failed to fetch summary")
    }
}