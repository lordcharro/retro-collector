package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.SwissMarketRadar
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.text.style.TextOverflow

import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SwissMarketRadarView(
    radar: SwissMarketRadar,
    modifier: Modifier = Modifier,
    askingPriceChf: Double? = null
) {
    val effectiveAsking = askingPriceChf ?: radar.spottedPriceChf
    val median = radar.medianPriceChf

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = TextKeys.Radar.TITLE,
                style = LabelFilterStyle.copy(fontSize = 11.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(StatusEnglishFg, RoundedCornerShape(3.dp))
                )
                Text(
                    text = radar.trend,
                    color = StatusEnglishFg,
                    style = CodeSkuStyle.copy(fontSize = 11.sp),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${TextKeys.Radar.ASKING_PRICE}:",
                    style = BodySm,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = if (effectiveAsking != null) "CHF $effectiveAsking" else "—",
                    style = CodePriceStyle,
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${TextKeys.Radar.MEDIAN_90D}:",
                    style = BodySm,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = if (median != null) "CHF $median" else "—",
                    style = CodePriceStyle,
                    color = AccentBlue,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        // Deal evaluation pill if both available
        if (effectiveAsking != null && median != null && median > 0) {
            val delta = ((effectiveAsking - median) / median) * 100
            val (dealText, dealColor) = when {
                delta <= -20 -> Pair(TextKeys.Radar.DEAL_BARGAIN, StatusEnglishFg)
                delta <= 0 -> Pair(TextKeys.Radar.DEAL_GOOD, StatusEnglishFg)
                delta <= 15 -> Pair(TextKeys.Radar.DEAL_FAIR, StatusEditionFg)
                else -> Pair(TextKeys.Radar.DEAL_OVERPRICED, StatusRiskFg)
            }

            Text(
                text = "$dealText (${if (delta > 0) "+" else ""}${delta.toInt()}% ${TextKeys.Radar.VS_RICARDO})",
                style = LabelFilterStyle.copy(fontSize = 11.sp),
                color = dealColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Barra visual de escala de preço
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(top = 4.dp)
                .background(SurfaceElevated, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.6f)
                    .padding(start = 24.dp)
                    .background(AccentBlue.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .padding(start = 48.dp)
                    .background(StatusEditionFg, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Preview
@Composable
internal fun SwissMarketRadarViewPreview() {
    RetroTactileTheme {
        SwissMarketRadarView(
            radar = SwissMarketRadar(
                spottedPriceChf = 35.0,
                medianPriceChf = 31.50,
                historicalMinChf = 28.0,
                historicalMaxChf = 36.0,
                trend = "Stable"
            ),
            askingPriceChf = 35.0,
            modifier = Modifier.padding(16.dp)
        )
    }
}
