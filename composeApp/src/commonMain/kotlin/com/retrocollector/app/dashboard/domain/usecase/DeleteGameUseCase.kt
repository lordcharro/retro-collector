package com.retrocollector.app.dashboard.domain.usecase

import com.retrocollector.app.core.domain.repository.IGameRepository

class DeleteGameUseCase(private val repository: IGameRepository) {
    operator fun invoke(gameId: String) {
        repository.deleteGame(gameId)
    }
}
