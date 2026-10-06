package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.presentation.theme.BodySm
import com.retrocollector.app.core.presentation.theme.BorderStrong
import com.retrocollector.app.core.presentation.theme.HeadlineSm
import com.retrocollector.app.core.presentation.theme.LabelFilterStyle
import com.retrocollector.app.core.presentation.theme.RetroTactileTheme
import com.retrocollector.app.core.presentation.theme.TextPrimary
import com.retrocollector.app.core.presentation.theme.TextSecondary
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TacticalEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    icon: String? = null,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            if (!icon.isNullOrBlank()) {
                Text(text = icon, fontSize = 28.sp)
                Spacer(modifier = Modifier.height(10.dp))
            }

            Text(
                text = title,
                style = HeadlineSm,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = BodySm,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            if (!actionLabel.isNullOrBlank() && onActionClick != null) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onActionClick,
                    border = BorderStroke(1.dp, BorderStrong),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = LabelFilterStyle,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun TacticalEmptyStatePreview() {
    RetroTactileTheme {
        TacticalEmptyState(
            icon = "🔍",
            title = "No games found",
            subtitle = "Try adjusting your filters or search query.",
            actionLabel = "Clear Filters",
            onActionClick = {}
        )
    }
}
