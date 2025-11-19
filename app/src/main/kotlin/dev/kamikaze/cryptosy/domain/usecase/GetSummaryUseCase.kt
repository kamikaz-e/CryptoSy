package dev.kamikaze.cryptosy.domain.usecase

import dev.kamikaze.cryptosy.data.repository.ChatRepository
import dev.kamikaze.cryptosy.domain.model.ChatItem

class GetSummaryUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(): Result<List<ChatItem>> {
        return repository.getSummary()
    }
}