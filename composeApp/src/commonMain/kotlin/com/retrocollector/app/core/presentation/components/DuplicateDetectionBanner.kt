package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*

/**
 * Banner de deteção de duplicados.
 * Aparece quando um Quick Scan encontra um jogo que já existe na Wishlist.
 */
@Composable
fun DuplicateDetectionBanner(
    onMoveToHunting: () -> Unit,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(6.dp))
            .border(
                BorderStroke(1.dp, Color(0xFFDB2777)),
                RoundedCornerShape(6.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Conteúdo textual
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = TextKeys.Duplicate.BANNER_TITLE,
                style = BodyMd,
                color = TextPrimary
            )
            Text(
                text = TextKeys.Duplicate.BANNER_SUBTITLE,
                style = BodySm.copy(fontSize = 12.sp),
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Botões de ação
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.End
        ) {
            Button(
                onClick = onMoveToHunting,
                colors = ButtonDefaults.buttonColors(containerColor = ConsoleGamecube),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(28.dp).defaultMinSize(minHeight = 28.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = TextKeys.Duplicate.ACTION_MOVE,
                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = onCreateNew,
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.height(28.dp).defaultMinSize(minHeight = 28.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = TextKeys.Duplicate.ACTION_CREATE_NEW,
                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                    color = TextSecondary
                )
            }
        }

        // Botão dismiss
        Text(
            text = "✕",
            style = BodyMd,
            color = StatusUnverifiedFg,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable { onDismiss() }
        )
    }
}
