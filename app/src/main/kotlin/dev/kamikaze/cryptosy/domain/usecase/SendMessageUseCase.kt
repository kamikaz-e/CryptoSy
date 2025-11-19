package dev.kamikaze.cryptosy.domain.usecase

import dev.kamikaze.cryptosy.data.repository.ChatRepository
import dev.kamikaze.cryptosy.domain.model.ChatItem

class SendMessageUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(message: String): Result<List<ChatItem>> {
        return repository.sendMessage(message)
    }
}