package dev.kamikaze.cryptosy.di

import android.content.Context
import dev.kamikaze.cryptosy.BuildConfig.MCP_BASE_URL
import dev.kamikaze.cryptosy.BuildConfig.MOON_PHASES_BASE_URL
import dev.kamikaze.cryptosy.data.cache.CacheManager
import dev.kamikaze.cryptosy.data.remote.McpApi
import dev.kamikaze.cryptosy.data.remote.McpApiImpl
import dev.kamikaze.cryptosy.data.remote.MoonPhasesApi
import dev.kamikaze.cryptosy.data.remote.MoonPhasesApiImpl
import dev.kamikaze.cryptosy.data.repository.ChatRepository
import dev.kamikaze.cryptosy.data.repository.ChatRepositoryImpl
import dev.kamikaze.cryptosy.domain.usecase.GetMoonPhaseUseCase
import dev.kamikaze.cryptosy.domain.usecase.GetSummaryUseCase
import dev.kamikaze.cryptosy.domain.usecase.LoadToolsUseCase
import dev.kamikaze.cryptosy.domain.usecase.SendMessageUseCase
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import timber.log.Timber

class AppContainer(context: Context) {

    private val httpClient: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Timber.tag("HTTP Client").d(message)
                }
            }
            level = LogLevel.INFO
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
        }
    }
    private val mcpApi: McpApi = McpApiImpl(httpClient, MCP_BASE_URL)
    private val moonPhasesApi: MoonPhasesApi = MoonPhasesApiImpl(httpClient, MOON_PHASES_BASE_URL)

    private val cacheManager: CacheManager = CacheManager(context)

    val chatRepository: ChatRepository = ChatRepositoryImpl(
        mcpApi = mcpApi,
        moonPhasesApi = moonPhasesApi,
        cache = cacheManager
    )

    val sendMessageUseCase: SendMessageUseCase = SendMessageUseCase(chatRepository)
    val getSummaryUseCase: GetSummaryUseCase = GetSummaryUseCase(chatRepository)
    val loadToolsUseCase: LoadToolsUseCase = LoadToolsUseCase(chatRepository)
    val getMoonPhaseUseCase: GetMoonPhaseUseCase = GetMoonPhaseUseCase(moonPhasesApi, cacheManager)
}