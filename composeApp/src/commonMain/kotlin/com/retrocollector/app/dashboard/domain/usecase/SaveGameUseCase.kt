package com.retrocollector.app.dashboard.domain.usecase

import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.repository.IGameRepository

class SaveGameUseCase(private val repository: IGameRepository) {
    operator fun invoke(game: GameItem) {
        repository.upsertGame(game)
    }
}
