package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.retrocollector.app.core.presentation.theme.LabelFilterStyle
import com.retrocollector.app.core.presentation.theme.RetroTactileTheme
import com.retrocollector.app.core.presentation.theme.StatusEnglishFg
import com.retrocollector.app.core.presentation.util.PriceFormatter
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PaidPriceInput(
    paidPrice: Double?,
    onPriceSubmitted: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    currency: String = "CHF",
    label: String? = null
) {
    var priceText by remember(paidPrice) {
        mutableStateOf(paidPrice?.let { PriceFormatter.formatAmount(it) } ?: "")
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = LabelFilterStyle,
                color = StatusEnglishFg
            )
        }

        val curr = currency.ifBlank { "CHF" }
        TactileTextField(
            value = priceText,
            onValueChange = { newText ->
                priceText = newText
                val sanitized = newText.replace(',', '.').trim()
                if (sanitized.isBlank()) {
                    onPriceSubmitted(null)
                } else {
                    val parsed = sanitized.toDoubleOrNull()
                    if (parsed != null) {
                        onPriceSubmitted(parsed)
                    }
                }
            },
            placeholder = "$curr 0.00",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(90.dp)
        )
    }
}

@Preview
@Composable
fun PaidPriceInputPreview() {
    RetroTactileTheme {
        PaidPriceInput(
            paidPrice = 35.0,
            currency = "CHF",
            label = "PAID:",
            onPriceSubmitted = {}
        )
    }
}
