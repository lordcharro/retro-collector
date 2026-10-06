package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.presentation.theme.*

import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PlatformBadge(
    platform: ConsolePlatform,
    modifier: Modifier = Modifier
) {
    val bgColor = Color(platform.brandColorHex)

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .semantics {
                contentDescription = "Plataforma: ${platform.displayName}"
            }
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = platform.displayName,
            color = Color.White,
            style = LabelBadgeStyle
        )
    }
}

@Preview
@Composable
fun PlatformBadgePreview() {
    RetroTactileTheme {
        PlatformBadge(platform = ConsolePlatform.GAMECUBE)
    }
}
