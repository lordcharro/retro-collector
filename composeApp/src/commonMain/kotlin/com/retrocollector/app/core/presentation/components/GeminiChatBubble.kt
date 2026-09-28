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
import com.retrocollector.app.core.domain.model.MessageSender
import com.retrocollector.app.core.presentation.theme.*

@Composable
fun GeminiChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    if (message.sender == MessageSender.USER) {
        // Balão de Mensagem do Utilizador
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
                        Text(text = "Foto de Lombada Anexada", style = CodeSkuStyle.copy(fontSize = 11.sp), color = TextSecondary)
                    }
                }
            }
        }
    } else {
        // Cartão de Resposta do Gemini
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
                // Barra de Topo do Gemini
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

                    Text(
                        text = "0.42s latency",
                        style = CodeSkuStyle.copy(fontSize = 11.sp),
                        color = StatusUnverifiedFg
                    )
                }

                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Caixa de Alerta de Censura / Risco se existir
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
                                    text = "⚠️ Alerta de Censura ou Bloqueio Regional",
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

                    // Veredito de Colecionador em Destaque
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

                    // Texto da Resposta
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
