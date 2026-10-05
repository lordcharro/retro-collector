package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.ChatMessage
import com.retrocollector.app.core.domain.model.MessageSender
import com.retrocollector.app.core.presentation.theme.LabelFilterStyle
import com.retrocollector.app.core.presentation.theme.RetroTactileTheme
import com.retrocollector.app.core.presentation.theme.StatusRiskFg
import com.retrocollector.app.core.presentation.theme.TextSecondary
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Wraps [GeminiChatBubble] with deletion interaction capabilities:
 * long-press gesture, dropdown action trigger, and tactile confirmation dialog.
 */
@Composable
fun DeletableChatBubble(
    message: ChatMessage,
    onDeleteMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        TactileConfirmDialog(
            title = "Delete Message",
            message = "Are you sure you want to delete this message from the dossier?",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                showConfirmDialog = false
                onDeleteMessage(message.id)
            },
            onDismiss = {
                showConfirmDialog = false
            }
        )
    }

    val actionSlot: @Composable () -> Unit = {
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(22.dp)
            ) {
                Text("⋮", style = LabelFilterStyle, color = TextSecondary)
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Delete Message",
                            style = LabelFilterStyle,
                            color = StatusRiskFg
                        )
                    },
                    leadingIcon = {
                        Text("🗑️", fontSize = 14.sp)
                    },
                    onClick = {
                        showMenu = false
                        showConfirmDialog = true
                    }
                )
            }
        }
    }

    GeminiChatBubble(
        message = message,
        headerAction = actionSlot,
        modifier = modifier.pointerInput(message.id) {
            detectTapGestures(onLongPress = { showMenu = true })
        }
    )
}

@Preview
@Composable
fun DeletableChatBubblePreview() {
    RetroTactileTheme {
        DeletableChatBubble(
            message = ChatMessage(
                id = "preview_1",
                contextId = "test",
                sender = MessageSender.USER,
                text = "Is this European edition compatible with Swiss consoles?"
            ),
            onDeleteMessage = {}
        )
    }
}
