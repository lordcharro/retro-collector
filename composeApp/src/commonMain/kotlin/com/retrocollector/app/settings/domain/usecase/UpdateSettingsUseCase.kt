package com.retrocollector.app.settings.domain.usecase

import com.retrocollector.app.core.domain.repository.IGameRepository
import com.retrocollector.app.settings.domain.model.AppSettings

class UpdateSettingsUseCase(private val repository: IGameRepository) {
    operator fun invoke(settings: AppSettings) {
        repository.updateSettings(settings)
    }
}
