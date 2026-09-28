package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.presentation.theme.*

@Composable
fun PlatformBadge(
    platform: ConsolePlatform,
    modifier: Modifier = Modifier
) {
    val bgColor = when (platform) {
        ConsolePlatform.N64 -> ConsoleN64
        ConsolePlatform.GAMECUBE -> ConsoleGamecube
        ConsolePlatform.PS3 -> ConsolePS3
        ConsolePlatform.SWITCH -> ConsoleSwitch
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = platform.displayName,
            color = Color.White,
            style = LabelBadgeStyle
        )
    }
}
