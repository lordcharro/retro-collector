package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.ChatMessage
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.model.MessageSender
import com.retrocollector.app.core.presentation.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun GeminiChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
    headerAction: (@Composable () -> Unit)? = null
) {
    if (message.sender == MessageSender.USER) {
        // User Message Bubble
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .background(UserChatBg, RoundedCornerShape(8.dp))
                    .border(1.dp, UserChatBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                if (headerAction != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "YOU",
                            style = CodeSkuStyle.copy(fontSize = 10.sp),
                            color = TextSecondary
                        )
                        headerAction()
                    }
                }

                Text(
                    text = message.text,
                    style = BodyMd,
                    color = TextPrimary
                )

                if (!message.imageBase64.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "📷", fontSize = 12.sp)
                        Text(text = "Attached Spine Photo", style = CodeSkuStyle.copy(fontSize = 11.sp), color = TextSecondary)
                    }
                }
            }
        }
    } else {
        // Gemini Response Card
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .background(SurfaceCard, RoundedCornerShape(8.dp))
                    .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
            ) {
                // Gemini Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(AccentGreen, RoundedCornerShape(3.dp))
                        )
                        Text(
                            text = "Gemini Flash Regional Verification",
                            style = LabelFilterStyle,
                            color = AccentGreen
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "0.42s latency",
                            style = CodeSkuStyle.copy(fontSize = 11.sp),
                            color = StatusUnverifiedFg
                        )

                        if (headerAction != null) {
                            headerAction()
                        }
                    }
                }

                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Censorship / Risk Alert Box if present
                    message.suggestedGameUpdate?.censorshipWarning?.let { warning ->
                        if (warning.isNotBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(StatusRiskBg.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .border(1.dp, StatusRiskFg.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "⚠️ Censorship or Regional Lock Warning",
                                    style = HeadlineSm,
                                    color = StatusRiskFg
                                )
                                Text(
                                    text = warning,
                                    style = BodyMd,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    // Featured Collector Verdict
                    message.suggestedGameUpdate?.collectorVerdict?.let { verdict ->
                        if (verdict.isNotBlank()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SurfaceElevated.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .border(BorderStroke(2.dp, StatusEditionFg))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "FIELD COLLECTOR VERDICT",
                                    style = LabelBadgeStyle,
                                    color = StatusEditionFg
                                )
                                Text(
                                    text = verdict,
                                    style = BodyMd,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    // Response Text
                    Text(
                        text = message.text,
                        style = BodyMd,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun GeminiChatBubblePreview() {
    RetroTactileTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GeminiChatBubble(
                message = ChatMessage(
                    id = "1",
                    contextId = "gc_re4",
                    sender = MessageSender.USER,
                    text = "Is there any difference between UK and NOE editions for this game?",
                    imageBase64 = "mock_base64_data"
                )
            )
            GeminiChatBubble(
                message = ChatMessage(
                    id = "2",
                    contextId = "gc_re4",
                    sender = MessageSender.GEMINI,
                    text = "The UK edition has SKU DOL-P-G4BE with full English audio and text. The NOE edition is German language only.",
                    suggestedGameUpdate = GameItem(
                        id = "gc_re4",
                        title = "Resident Evil 4",
                        platform = ConsolePlatform.GAMECUBE,
                        collectorVerdict = "UKV/EUR edition recommended with full multilingual subtitles. Avoid German (NOE) version with censored bonus modes.",
                        censorshipWarning = "The German (NOE) release removed Assignment Ada and The Mercenaries."
                    )
                )
            )
        }
    }
}
