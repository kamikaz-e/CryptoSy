package dev.kamikaze.cryptosy.ui

import dev.kamikaze.cryptosy.domain.model.ChatItem
import dev.kamikaze.cryptosy.domain.model.ToolItem

data class ChatUiState(
    val items: List<ChatItem> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
    val tools: List<ToolItem> = emptyList(),
    val showToolsDialog: Boolean = false,
    val isServiceRunning: Boolean = false
)