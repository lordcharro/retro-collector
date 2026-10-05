package com.retrocollector.app.core.presentation.util

import com.retrocollector.app.settings.domain.model.AppLanguage

actual fun setAppLocale(language: AppLanguage) {
    // In web / Wasm environment, language preferences are managed via document/browser
}
