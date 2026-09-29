package com.retrocollector.app.collection.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*

/**
 * Barra de estatísticas da coleção pessoal.
 * Mostra contagem de jogos e valor total em CHF.
 */
@Composable
fun CollectionStatsBar(
    games: List<GameItem>,
    currency: String = "CHF",
    modifier: Modifier = Modifier
) {
    val totalValue = remember(games) {
        games.mapNotNull { it.paidPriceChf }.sum()
    }
    val hasAnyPrice = remember(games) {
        games.any { it.paidPriceChf != null }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .border(BorderStroke(1.dp, BorderSubtle))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Contagem de jogos
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "📦", fontSize = 14.sp)
            Text(
                text = "${games.size} ${TextKeys.Collection.STATS_GAMES}",
                style = HeadlineSm,
                color = TextPrimary
            )
        }

        // Valor total
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "💰", fontSize = 14.sp)
            if (hasAnyPrice) {
                Text(
                    text = "$currency ${formatPrice(totalValue)}",
                    style = CodePriceStyle.copy(fontSize = 14.sp),
                    color = StatusEnglishFg
                )
            } else {
                Text(
                    text = "$currency ${TextKeys.Collection.STATS_NO_PRICES}",
                    style = CodePriceStyle.copy(fontSize = 14.sp),
                    color = StatusUnverifiedFg
                )
            }
            Text(
                text = TextKeys.Collection.STATS_TOTAL_VALUE,
                style = LabelBadgeStyle.copy(fontSize = 10.sp),
                color = StatusUnverifiedFg
            )
        }
    }
}

private fun formatPrice(price: Double): String {
    val rounded = (price * 100).toLong()
    val whole = rounded / 100
    val fraction = rounded % 100
    return "$whole.${fraction.toString().padStart(2, '0')}"
}

