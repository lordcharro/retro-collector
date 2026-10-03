package com.retrocollector.app.settings.domain.usecase

import com.retrocollector.app.core.domain.repository.IGameRepository
import com.retrocollector.app.settings.domain.model.AiProvider

class TestAiConnectionUseCase(private val repository: IGameRepository) {
    suspend operator fun invoke(
        provider: AiProvider,
        apiKey: String,
        model: String,
        baseUrl: String? = null
    ): Result<String> {
        return repository.testAiConnection(provider, apiKey, model, baseUrl)
    }
}
