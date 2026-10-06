package com.retrocollector.app.core.presentation.util

import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.retrocollector.app.RetroCollectorApplication
import com.retrocollector.app.settings.domain.model.AppLanguage
import java.util.Locale

actual fun setAppLocale(language: AppLanguage) {
    val locale = Locale.forLanguageTag(language.code)
    Locale.setDefault(locale)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocaleList.setDefault(LocaleList(locale))
    }
    try {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.code))
    } catch (_: Exception) {
        // Fallback for non-AppCompat or headless environments
    }
    val context = RetroCollectorApplication.appContext
    if (context != null) {
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        }
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
