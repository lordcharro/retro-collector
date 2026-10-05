package com.retrocollector.app.core.presentation.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * Encapsulates UI text that can be either a raw dynamic string or a localized Compose StringResource with arguments.
 * Allows ViewModels and business logic to emit localized messages without direct UI context.
 */
@Suppress("SpreadOperator")
sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class Resource(val res: StringResource, val args: List<Any> = emptyList()) : UiText

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is Resource -> if (args.isEmpty()) stringResource(res) else stringResource(res, *args.toTypedArray())
    }

    suspend fun asStringAsync(): String = when (this) {
        is DynamicString -> value
        is Resource -> if (args.isEmpty()) getString(res) else getString(res, *args.toTypedArray())
    }

    companion object {
        operator fun invoke(res: StringResource, vararg args: Any): UiText = Resource(res, args.toList())
        operator fun invoke(value: String): UiText = DynamicString(value)
    }
}
