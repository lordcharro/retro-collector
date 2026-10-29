package com.retrocollector.app.settings.domain.usecase

import com.retrocollector.app.core.domain.repository.IGameRepository

class VerifyFirestoreConnectionUseCase(private val repository: IGameRepository) {
    suspend operator fun invoke(projectId: String, apiKey: String = ""): Result<String> {
        return repository.testFirestoreConnection(projectId, apiKey)
    }
}
