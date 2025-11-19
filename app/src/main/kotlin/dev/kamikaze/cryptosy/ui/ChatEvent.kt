package dev.kamikaze.cryptosy.ui

import dev.kamikaze.cryptosy.domain.model.ToolItem

sealed interface ChatEvent {
    data class InputChanged(val text: String) : ChatEvent
    data object SendPressed : ChatEvent
    data class ToolPicked(val tool: ToolItem) : ChatEvent
    data object ToolsPressed : ChatEvent
    data object ToolsDialogDismissed : ChatEvent
    data object Scrolled : ChatEvent
    data object StartService : ChatEvent
    data object StopService : ChatEvent
}