package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.SwissMarketRadar
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.core.presentation.util.PriceFormatter

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.text.style.TextOverflow

import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SwissMarketRadarView(
    radar: SwissMarketRadar,
    modifier: Modifier = Modifier,
    askingPriceChf: Double? = null,
    paidPriceChf: Double? = null,
    isOwned: Boolean = false,
    onPriceSubmitted: ((Double?) -> Unit)? = null,
    currency: String = "CHF"
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${TextKeys.Radar.ASKING_PRICE}:",
                    style = BodySm,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = PriceFormatter.format(effectiveAsking, currency),
                    style = CodePriceStyle,
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${TextKeys.Radar.MEDIAN_90D}:",
                    style = BodySm,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = PriceFormatter.format(median, currency),
                    style = CodePriceStyle,
                    color = AccentBlue,
                    maxLines = 1,
                    softWrap = false
                )
            }

            if (isOwned && onPriceSubmitted != null) {
                PaidPriceInput(
                    paidPrice = paidPriceChf,
                    currency = currency,
                    label = "${TextKeys.Dossier.PAID_PRICE_LABEL}:",
                    onPriceSubmitted = onPriceSubmitted
                )
            }
        }

        // Deal evaluation pill if both available
        val priceToCompare = if (isOwned && paidPriceChf != null) paidPriceChf else effectiveAsking
        if (priceToCompare != null && median != null && median > 0) {
            val delta = ((priceToCompare - median) / median) * 100
            val (dealText, dealColor) = when {
                delta <= -20 -> Pair(if (isOwned && paidPriceChf != null) "Great Acquisition" else TextKeys.Radar.DEAL_BARGAIN, StatusEnglishFg)
                delta <= 0 -> Pair(if (isOwned && paidPriceChf != null) "Good Value" else TextKeys.Radar.DEAL_GOOD, StatusEnglishFg)
                delta <= 15 -> Pair(if (isOwned && paidPriceChf != null) "Fair Price" else TextKeys.Radar.DEAL_FAIR, StatusEditionFg)
                else -> Pair(if (isOwned && paidPriceChf != null) "Premium Paid" else TextKeys.Radar.DEAL_OVERPRICED, StatusRiskFg)
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

        // Visual price scale bar
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
fun SwissMarketRadarViewPreview() {
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
