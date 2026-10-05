package com.retrocollector.app.core.presentation.util

import com.retrocollector.app.settings.domain.model.AppLanguage
import java.util.Locale

actual fun setAppLocale(language: AppLanguage) {
    val locale = Locale.forLanguageTag(language.code)
    Locale.setDefault(locale)
}
