package dev.kamikaze.cryptosy.domain.usecase

import dev.kamikaze.cryptosy.data.repository.ChatRepository
import dev.kamikaze.cryptosy.domain.model.ToolItem

class LoadToolsUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(): Result<List<ToolItem>> {
        return repository.getTools()
    }
}