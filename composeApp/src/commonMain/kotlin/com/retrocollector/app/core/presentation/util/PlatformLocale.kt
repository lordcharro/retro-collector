package com.retrocollector.app.core.presentation.util

import com.retrocollector.app.settings.domain.model.AppLanguage

/**
 * Platform-specific locale mutator to synchronize Compose Multiplatform
 * resource loading with the user-selected AppLanguage at runtime.
 */
expect fun setAppLocale(language: AppLanguage)
