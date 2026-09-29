package com.retrocollector.app.discovery.domain.usecase

import com.retrocollector.app.core.domain.model.DiscoveredGameItem
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.repository.IGameRepository

class GetSimilarGamesUseCase(private val repository: IGameRepository) {
    suspend operator fun invoke(
        game: GameItem,
        forceRefresh: Boolean = false
    ): Result<List<DiscoveredGameItem>> {
        return repository.getSimilarGames(
            game = game,
            forceRefresh = forceRefresh
        )
    }
}
