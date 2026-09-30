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
import com.retrocollector.app.core.domain.model.EnrichmentStatus
import com.retrocollector.app.core.presentation.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Badge visual para o estado de enriquecimento de um jogo da Wishlist.
 * Mostra ícone e label com cor correspondente ao estado.
 */
@Composable
fun EnrichmentBadge(
    status: EnrichmentStatus,
    modifier: Modifier = Modifier
) {
    val style = when (status) {
        EnrichmentStatus.PENDING -> EnrichmentStyle("⏳", "Pending", EnrichmentPendingBg, EnrichmentPendingFg)
        EnrichmentStatus.ENRICHING -> EnrichmentStyle("🔄", "Enriching", EnrichmentRunningBg, EnrichmentRunningFg)
        EnrichmentStatus.COMPLETE -> EnrichmentStyle("✅", "Ready", StatusEnglishBg, StatusEnglishFg)
        EnrichmentStatus.FAILED -> EnrichmentStyle("❌", "Failed", StatusRiskBg, StatusRiskFg)
    }

    Row(
        modifier = modifier
            .background(style.bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(text = style.icon, fontSize = 10.sp)
        Text(
            text = style.label,
            style = LabelBadgeStyle.copy(fontSize = 10.sp),
            color = style.fgColor,
            maxLines = 1
        )
    }
}

private data class EnrichmentStyle(
    val icon: String,
    val label: String,
    val bgColor: Color,
    val fgColor: Color
)

@Preview
@Composable
fun EnrichmentBadgePreview() {
    RetroTactileTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EnrichmentBadge(status = EnrichmentStatus.PENDING)
            EnrichmentBadge(status = EnrichmentStatus.ENRICHING)
            EnrichmentBadge(status = EnrichmentStatus.COMPLETE)
            EnrichmentBadge(status = EnrichmentStatus.FAILED)
        }
    }
}
