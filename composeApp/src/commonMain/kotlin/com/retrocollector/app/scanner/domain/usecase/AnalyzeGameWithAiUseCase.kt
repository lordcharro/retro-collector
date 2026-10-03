package com.retrocollector.app.scanner.domain.usecase

import com.retrocollector.app.core.domain.model.ChatMessage
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.repository.IGameRepository

class AnalyzeGameWithAiUseCase(private val repository: IGameRepository) {
    suspend operator fun invoke(
        query: String,
        imageBase64: String? = null,
        spottedLocation: String = "",
        askingPriceChf: Double? = null
    ): Result<Pair<ChatMessage, GameItem?>> {
        return repository.inspectGameWithAi(query, imageBase64, spottedLocation, askingPriceChf)
    }
}
