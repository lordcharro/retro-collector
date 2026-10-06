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
import com.retrocollector.app.generated.resources.*
import com.retrocollector.app.core.domain.model.SwissMarketRadar
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.core.presentation.util.PriceFormatter
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SwissMarketRadarView(
    radar: SwissMarketRadar,
    modifier: Modifier = Modifier,
    paidPriceChf: Double? = null,
    isOwned: Boolean = false,
    onPriceSubmitted: ((Double?) -> Unit)? = null,
    currency: String = "CHF"
) {
    val median = radar.medianPriceChf
    val minPrice = radar.historicalMinChf
    val maxPrice = radar.historicalMaxChf

    val trendColor = when {
        radar.trend.contains("Rising", ignoreCase = true) || radar.trend.contains("Hot", ignoreCase = true) -> StatusEditionFg
        radar.trend.contains("Falling", ignoreCase = true) -> AccentBlue
        else -> StatusEnglishFg
    }

    Column(modifier = modifier) {
        // Header: Title & Market Trend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.radar_title),
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
                        .background(trendColor, RoundedCornerShape(3.dp))
                )
                Text(
                    text = "${radar.trend.uppercase()} ${stringResource(Res.string.radar_trend).uppercase()}",
                    color = trendColor,
                    style = CodeSkuStyle.copy(fontSize = 10.sp),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        // Metrics Row: Median & CIB Range
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 90d Median
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(Res.string.radar_median_90d)}:",
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

            // CIB / Historical Price Range
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stringResource(Res.string.radar_historical_range)}:",
                    style = BodySm,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
                val rangeText = when {
                    minPrice != null && maxPrice != null ->
                        "${PriceFormatter.formatAmount(minPrice)} – ${PriceFormatter.format(maxPrice, currency)}"
                    minPrice != null ->
                        "From ${PriceFormatter.format(minPrice, currency)}"
                    maxPrice != null ->
                        "Up to ${PriceFormatter.format(maxPrice, currency)}"
                    else -> "—"
                }
                Text(
                    text = rangeText,
                    style = CodePriceStyle.copy(fontSize = 12.sp),
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Acquisition Paid Price (if owned)
            if (isOwned && onPriceSubmitted != null) {
                PaidPriceInput(
                    paidPrice = paidPriceChf,
                    currency = currency,
                    label = "${stringResource(Res.string.dossier_paid_price_label)}:",
                    onPriceSubmitted = onPriceSubmitted
                )
            }
        }

        // Deal evaluation vs Median (if owned)
        if (isOwned && paidPriceChf != null && median != null && median > 0) {
            val delta = ((paidPriceChf - median) / median) * 100
            val (dealRes, dealColor) = when {
                delta <= -20 -> Pair(Res.string.radar_deal_bargain, StatusEnglishFg)
                delta <= 0 -> Pair(Res.string.radar_deal_good, StatusEnglishFg)
                delta <= 15 -> Pair(Res.string.radar_deal_fair, StatusEditionFg)
                else -> Pair(Res.string.radar_deal_overpriced, StatusRiskFg)
            }

            Text(
                text = "${stringResource(dealRes)} (${if (delta > 0) "+" else ""}${delta.toInt()}% ${stringResource(Res.string.radar_vs_ricardo)})",
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
                    .fillMaxWidth(0.65f)
                    .padding(start = 16.dp)
                    .background(AccentBlue.copy(alpha = 0.35f), RoundedCornerShape(3.dp))
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .padding(start = 36.dp)
                    .background(StatusEnglishFg, RoundedCornerShape(2.dp))
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
            modifier = Modifier.padding(16.dp)
        )
    }
}
