package dev.kamikaze.cryptosy.data.remote

import dev.kamikaze.cryptosy.data.dto.ChatRequestDto
import dev.kamikaze.cryptosy.data.dto.ChatContextDto
import dev.kamikaze.cryptosy.data.dto.ChatResponseDto
import dev.kamikaze.cryptosy.data.dto.ChatResponseItemDto
import dev.kamikaze.cryptosy.data.dto.MoonPhaseDto
import dev.kamikaze.cryptosy.data.dto.ToolsResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import timber.log.Timber

interface MoonPhasesApi {
    suspend fun getCurrentMoonPhase(): Result<MoonPhaseDto>
    suspend fun getTools(): Result<ToolsResponseDto>
    suspend fun sendMessage(message: String): Result<ChatResponseDto>
}

class MoonPhasesApiImpl(
    private val client: HttpClient,
    private val baseUrl: String
) : MoonPhasesApi {

    override suspend fun getCurrentMoonPhase(): Result<MoonPhaseDto> = runCatching {
        Timber.d("Fetching current moon phase from $baseUrl/moonphase/current")
        val response: MoonPhaseDto = client.get("$baseUrl/moonphase/current").body()
        Timber.d("Moon phase received: ${response.phase} ${response.phaseEmoji}")
        response
    }.onFailure {
        Timber.e(it, "Failed to fetch moon phase")
    }

    override suspend fun getTools(): Result<ToolsResponseDto> = runCatching {
        Timber.d("Fetching moon tools from $baseUrl/tools")
        val response: ToolsResponseDto = client.get("$baseUrl/tools").body()
        Timber.d("Moon tools received: ${response.tools.size} tools")
        response
    }.onFailure {
        Timber.e(it, "Failed to fetch moon tools")
    }

    override suspend fun sendMessage(message: String): Result<ChatResponseDto> = runCatching {
        Timber.d("Sending message to Moon API: $message")
        val request = ChatRequestDto(
            message = message,
            context = ChatContextDto()
        )

        // Moon API возвращает MoonPhaseDto напрямую
        val moonPhaseResponse: MoonPhaseDto = client.post("$baseUrl/chat") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

        Timber.d("Moon phase received from chat: ${moonPhaseResponse.phase}")

        // Конвертируем MoonPhaseDto в ChatResponseDto
        val chatResponse = ChatResponseDto(
            items = listOf(
                ChatResponseItemDto(
                    type = "moonPhase",
                    ts = System.currentTimeMillis(),
                    moonPhase = moonPhaseResponse
                )
            )
        )

        chatResponse
    }.onFailure {
        Timber.e(it, "Failed to send message to Moon API")
    }
}