package dev.kamikaze.cryptosy.di

import android.content.Context
import dev.kamikaze.cryptosy.BuildConfig
import dev.kamikaze.cryptosy.data.cache.CacheManager
import dev.kamikaze.cryptosy.data.remote.McpApi
import dev.kamikaze.cryptosy.data.remote.McpApiImpl
import dev.kamikaze.cryptosy.data.repository.ChatRepository
import dev.kamikaze.cryptosy.data.repository.ChatRepositoryImpl
import dev.kamikaze.cryptosy.domain.usecase.GetSummaryUseCase
import dev.kamikaze.cryptosy.domain.usecase.LoadToolsUseCase
import dev.kamikaze.cryptosy.domain.usecase.SendMessageUseCase
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class AppContainer(private val context: Context) {

    // Network
    private val httpClient: HttpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }
            
            install(Logging) {
                logger = Logger.DEFAULT
                level = if (BuildConfig.DEBUG) LogLevel.INFO else LogLevel.NONE
            }
        }
    }

    // API
    private val mcpApi: McpApi by lazy {
        McpApiImpl(
            client = httpClient,
            baseUrl = BuildConfig.MCP_BASE_URL
        )
    }

    // Cache
    private val cacheManager: CacheManager by lazy {
        CacheManager(context)
    }

    // Repository
    val chatRepository: ChatRepository by lazy {
        ChatRepositoryImpl(
            mcpApi = mcpApi,
            cacheManager = cacheManager
        )
    }

    // Use Cases
    val sendMessageUseCase: SendMessageUseCase by lazy {
        SendMessageUseCase(chatRepository)
    }

    val loadToolsUseCase: LoadToolsUseCase by lazy {
        LoadToolsUseCase(chatRepository)
    }

    val getSummaryUseCase: GetSummaryUseCase by lazy {
        GetSummaryUseCase(chatRepository)
    }
}