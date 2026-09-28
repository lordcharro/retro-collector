package com.retrocollector.app.dossier.domain.usecase

import com.retrocollector.app.core.domain.model.ChatMessage
import com.retrocollector.app.core.domain.repository.IGameRepository

class SendFollowUpChatUseCase(private val repository: IGameRepository) {
    suspend operator fun invoke(
        contextId: String,
        userMessage: String,
        imageBase64: String? = null
    ): Result<ChatMessage> {
        return repository.sendFollowUpChat(contextId, userMessage, imageBase64)
    }
}
