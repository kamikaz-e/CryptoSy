package dev.kamikaze.cryptosy.domain.usecase

import dev.kamikaze.cryptosy.data.cache.CacheManager
import dev.kamikaze.cryptosy.data.remote.MoonPhasesApi
import dev.kamikaze.cryptosy.domain.model.ChatItem
import dev.kamikaze.cryptosy.domain.model.ChatPayload
import dev.kamikaze.cryptosy.domain.model.ChatRole
import timber.log.Timber

class GetMoonPhaseUseCase(
    private val moonPhasesApi: MoonPhasesApi,
    private val cacheManager: CacheManager
) {
    suspend operator fun invoke(): Result<List<ChatItem>> {
        // Проверяем, есть ли валидный кеш (менее суток)
        val isCacheValid = cacheManager.isMoonPhaseCacheValid()

        return if (isCacheValid) {
            // Используем кеш
            Timber.d("Using cached moon phase")
            cacheManager.loadMoonPhase().fold(
                onSuccess = { dto ->
                    if (dto != null) {
                        Result.success(listOf(createChatItem(dto)))
                    } else {
                        // Если кеш пустой, делаем запрос к API
                        Timber.d("Cache is empty, fetching from API")
                        fetchAndCacheMoonPhase()
                    }
                },
                onFailure = { error ->
                    // Если кеш не загрузился, делаем запрос к API
                    Timber.e(error, "Failed to load cache, fetching from API")
                    fetchAndCacheMoonPhase()
                }
            )
        } else {
            // Кеш невалиден или отсутствует, делаем запрос к API
            Timber.d("Cache invalid or missing, fetching from API")
            fetchAndCacheMoonPhase()
        }
    }

    private suspend fun fetchAndCacheMoonPhase(): Result<List<ChatItem>> {
        return moonPhasesApi.getCurrentMoonPhase()
            .onSuccess { dto ->
                // Сохраняем в кеш
                cacheManager.saveMoonPhase(dto)
                    .onFailure { error ->
                        Timber.e(error, "Failed to save moon phase to cache")
                    }
            }
            .map { dto ->
                listOf(createChatItem(dto))
            }
    }

    private fun createChatItem(dto: dev.kamikaze.cryptosy.data.dto.MoonPhaseDto): ChatItem {
        return ChatItem(
            role = ChatRole.ASSISTANT,
            payload = ChatPayload.MoonPhase(
                phase = dto.phase,
                phaseEmoji = dto.phaseEmoji,
                waxing = dto.waxing,
                waning = dto.waning,
                lunarAge = dto.lunarAge,
                lunarAgePercent = dto.lunarAgePercent,
                lunationNumber = dto.lunationNumber,
                lunarDistance = dto.lunarDistance,
                nextFullMoon = dto.nextFullMoon,
                lastFullMoon = dto.lastFullMoon
            )
        )
    }
}