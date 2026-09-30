package com.retrocollector.app.core.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.retrocollector.app.core.presentation.theme.CodePriceStyle
import com.retrocollector.app.core.presentation.theme.TextPrimary
import com.retrocollector.app.core.presentation.util.PriceFormatter

@Composable
fun PriceTag(
    amount: Double?,
    currency: String = "CHF",
    modifier: Modifier = Modifier,
    includeCurrency: Boolean = true,
    style: TextStyle = CodePriceStyle,
    color: Color = TextPrimary
) {
    Text(
        text = PriceFormatter.format(
            amount = amount,
            currency = currency,
            includeCurrency = includeCurrency
        ),
        style = style,
        color = color,
        maxLines = 1,
        softWrap = false,
        modifier = modifier
    )
}
