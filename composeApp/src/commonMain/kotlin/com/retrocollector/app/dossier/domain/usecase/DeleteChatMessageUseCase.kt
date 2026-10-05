package com.retrocollector.app.dossier.domain.usecase

import com.retrocollector.app.core.domain.repository.IGameRepository

class DeleteChatMessageUseCase(private val repository: IGameRepository) {
    operator fun invoke(messageId: String) {
        repository.deleteChatMessage(messageId)
    }
}
