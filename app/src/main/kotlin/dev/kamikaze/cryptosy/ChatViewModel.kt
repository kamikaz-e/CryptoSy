package dev.kamikaze.cryptosy

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.kamikaze.cryptosy.data.repository.ChatRepository
import dev.kamikaze.cryptosy.domain.model.ChatItem
import dev.kamikaze.cryptosy.domain.model.ChatPayload
import dev.kamikaze.cryptosy.domain.model.ChatRole
import dev.kamikaze.cryptosy.domain.usecase.GetSummaryUseCase
import dev.kamikaze.cryptosy.domain.usecase.LoadToolsUseCase
import dev.kamikaze.cryptosy.domain.usecase.SendMessageUseCase
import dev.kamikaze.cryptosy.service.CryptoUpdateService
import dev.kamikaze.cryptosy.ui.ChatEvent
import dev.kamikaze.cryptosy.ui.ChatUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class ChatViewModel(
    private val app: Application,
    private val repository: ChatRepository,
    private val sendMessageUseCase: SendMessageUseCase,
    private val loadToolsUseCase: LoadToolsUseCase,
    private val getSummaryUseCase: GetSummaryUseCase
) : AndroidViewModel(app) {

    companion object {
        fun provideFactory(app: Application): ViewModelProvider.Factory {
            val container = (app as CryptoSy).container

            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return ChatViewModel(
                        app = app,
                        repository =  container.chatRepository,
                        sendMessageUseCase = container.sendMessageUseCase,
                        loadToolsUseCase = container.loadToolsUseCase,
                        getSummaryUseCase = container.getSummaryUseCase
                    ) as T
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadCache()
        loadTools()
        observeServiceUpdates()
    }

    fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InputChanged -> {
                _uiState.update { it.copy(input = event.text) }
            }

            is ChatEvent.SendPressed -> sendMessage()
            is ChatEvent.ToolsPressed -> {
                _uiState.update { it.copy(showToolsDialog = true) }
            }

            is ChatEvent.ToolPicked -> {
                _uiState.update {
                    it.copy(
                        input = event.tool.sampleQuery,
                        showToolsDialog = false
                    )
                }
            }

            is ChatEvent.ToolsDialogDismissed -> {
                _uiState.update { it.copy(showToolsDialog = false) }
            }

            is ChatEvent.Scrolled -> { /* Handled in UI */
            }

            is ChatEvent.StartService -> startForegroundService()
            is ChatEvent.StopService -> stopForegroundService()
        }
    }

    private fun sendMessage() {
        val message = _uiState.value.input.trim()
        if (message.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, input = "") }

            // Add user message
            val userMessage = ChatItem(
                role = ChatRole.USER,
                payload = ChatPayload.Text(message)
            )
            addMessage(userMessage)

            // Send to MCP
            sendMessageUseCase(message)
                .onSuccess { items ->
                    items.forEach { addMessage(it) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                    addMessage(
                        ChatItem(
                            role = ChatRole.SYSTEM,
                            payload = ChatPayload.Text("❌ Ошибка: ${error.message}")
                        )
                    )
                }

            _uiState.update { it.copy(isSending = false) }
        }
    }

    private fun addMessage(item: ChatItem) {
        _uiState.update { state ->
            val newItems = state.items + item
            state.copy(items = newItems)
        }
        saveCache()
    }

    private fun addMessages(items: List<ChatItem>) {
        _uiState.update { state ->
            val newItems = state.items + items
            state.copy(items = newItems)
        }
        saveCache()
    }

    private fun saveCache() {
        viewModelScope.launch {
            repository.saveMessages(_uiState.value.items)
        }
    }

    private fun loadCache() {
        viewModelScope.launch {
            repository.loadMessages()
                .onSuccess { items ->
                    _uiState.update { it.copy(items = items) }
                    Timber.d("Loaded ${items.size} messages from cache")
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to load cache")
                }
        }
    }

    private fun loadTools() {
        viewModelScope.launch {
            loadToolsUseCase()
                .onSuccess { tools ->
                    _uiState.update { it.copy(tools = tools) }
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to load tools")
                }
        }
    }

    private fun observeServiceUpdates() {
        viewModelScope.launch {
            CryptoUpdateService.summaryFlow.collect { items ->
                Timber.d("Received ${items.size} items from service")
                addMessages(items)
            }
        }
    }

    private fun startForegroundService() {
        val intent = Intent(app, CryptoUpdateService::class.java)
        app.startForegroundService(intent)
        _uiState.update { it.copy(isServiceRunning = true) }
        Timber.d("Foreground service started")
    }

    private fun stopForegroundService() {
        val intent = Intent(app, CryptoUpdateService::class.java)
        app.stopService(intent)
        _uiState.update { it.copy(isServiceRunning = false) }
        Timber.d("Foreground service stopped")
    }

    override fun onCleared() {
        super.onCleared()
        // Optionally stop service when ViewModel is cleared
        // stopForegroundService()
    }
}