package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.retrocollector.app.core.domain.model.LanguageStatus
import com.retrocollector.app.core.presentation.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LanguageRiskBadge(
    status: LanguageStatus,
    modifier: Modifier = Modifier,
    showDescription: Boolean = false
) {
    val (bgColor, fgColor, icon) = when (status) {
        LanguageStatus.FULL_ENGLISH -> Triple(StatusEnglishBg, StatusEnglishFg, "✔")
        LanguageStatus.SUBS_ONLY -> Triple(StatusEditionBg, StatusEditionFg, "⚠️")
        LanguageStatus.GERMAN_ONLY -> Triple(StatusRiskBg, StatusRiskFg, "⊘")
        LanguageStatus.EDITION_NOTICE -> Triple(StatusEditionBg, StatusEditionFg, "ℹ")
        LanguageStatus.UNVERIFIED -> Triple(StatusUnverifiedBg, StatusUnverifiedFg, "❓")
    }

    Column(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "Estado de idioma: ${status.label}${if (showDescription && status.subLabel.isNotBlank()) ". ${status.subLabel}" else ""}"
        }
    ) {
        Row(
            modifier = Modifier
                .background(bgColor, RoundedCornerShape(4.dp))
                .border(1.dp, fgColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                .padding(horizontal = 7.dp, vertical = 2.5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$icon ${status.label}",
                color = fgColor,
                style = LabelBadgeStyle,
                maxLines = 1,
                softWrap = false
            )
        }

        if (showDescription && status.subLabel.isNotBlank()) {
            Text(
                text = status.subLabel,
                style = BodySm,
                color = fgColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview
@Composable
fun LanguageRiskBadgePreview() {
    RetroTactileTheme {
        LanguageRiskBadge(
            status = LanguageStatus.FULL_ENGLISH,
            showDescription = true
        )
    }
}

