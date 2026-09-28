package com.retrocollector.app.settings.domain.usecase

import com.retrocollector.app.core.domain.repository.IGameRepository

class TestGeminiConnectionUseCase(private val repository: IGameRepository) {
    suspend operator fun invoke(apiKey: String): Result<String> {
        return repository.testGeminiConnection(apiKey)
    }
}
