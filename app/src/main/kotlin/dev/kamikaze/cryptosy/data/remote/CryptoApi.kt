package dev.kamikaze.cryptosy.data.remote

import dev.kamikaze.cryptosy.data.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import timber.log.Timber

interface McpApi {
    suspend fun getTools(): Result<ToolsResponseDto>
    suspend fun sendMessage(message: String): Result<ChatResponseDto>
    suspend fun getSummary(): Result<ChatResponseDto>
}

class McpApiImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : McpApi {

    override suspend fun getTools(): Result<ToolsResponseDto> = runCatching {
        Timber.d("Fetching tools from $baseUrl/tools")
        val response: ToolsResponseDto = client.get("$baseUrl/tools").body()
        Timber.d("Crypto tools received: ${response.tools.size} tools")
        response
    }.onFailure {
        Timber.e(it, "Failed to fetch tools")
    }

    override suspend fun sendMessage(message: String): Result<ChatResponseDto> = runCatching {
        Timber.d("Sending message: $message")
        val request = ChatRequestDto(
            message = message,
            context = ChatContextDto()
        )
        val response: ChatResponseDto = client.post("$baseUrl/chat") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        Timber.d("Received ${response.items.size} response items")
        response
    }.onFailure {
        Timber.e(it, "Failed to send message")
    }

    override suspend fun getSummary(): Result<ChatResponseDto> = runCatching {
        Timber.d("Fetching summary from $baseUrl/summary")
        val response: ChatResponseDto = client.get("$baseUrl/summary").body()
        Timber.d("Received ${response.items.size} summary items")
        response
    }.onFailure {
        Timber.e(it, "Failed to fetch summary")
    }
}