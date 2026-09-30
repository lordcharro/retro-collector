package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.retrocollector.app.core.presentation.theme.AccentBlue
import com.retrocollector.app.core.presentation.theme.BodySm
import com.retrocollector.app.core.presentation.theme.BorderSubtle
import com.retrocollector.app.core.presentation.theme.HeadlineSm
import com.retrocollector.app.core.presentation.theme.LabelFilterStyle
import com.retrocollector.app.core.presentation.theme.RetroTactileTheme
import com.retrocollector.app.core.presentation.theme.StatusRiskFg
import com.retrocollector.app.core.presentation.theme.SurfaceCard
import com.retrocollector.app.core.presentation.theme.TextPrimary
import com.retrocollector.app.core.presentation.theme.TextSecondary
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TactileConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = "Cancelar",
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = HeadlineSm,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = message,
                style = BodySm,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) StatusRiskFg else AccentBlue
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = confirmLabel,
                    style = LabelFilterStyle,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, BorderSubtle),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = dismissLabel,
                    style = LabelFilterStyle,
                    color = TextPrimary
                )
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    )
}

@Preview
@Composable
fun TactileConfirmDialogPreview() {
    RetroTactileTheme {
        TactileConfirmDialog(
            title = "Eliminar Jogo",
            message = "Tem a certeza de que deseja remover este jogo?",
            confirmLabel = "Eliminar",
            isDestructive = true,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
