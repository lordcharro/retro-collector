package com.retrocollector.app.dossier.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.CollectionStatus
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.presentation.components.*
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardUiState
import com.retrocollector.app.dashboard.presentation.viewmodel.DashboardViewModel

@Composable
fun MobileGameDetailScreen(
    game: GameItem,
    state: DashboardUiState,
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var followUpQuestion by remember { mutableStateOf("") }
    var isChatFocused by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceBase,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(SurfaceCard)
                    .border(BorderStroke(1.dp, BorderSubtle))
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onBack) {
                    Text("← " + TextKeys.Navigation.BACK, color = AccentBlue, style = LabelFilterStyle)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = game.title, style = HeadlineSm, color = TextPrimary, maxLines = 1)
                    PlatformBadge(platform = game.platform)
                }

                Spacer(modifier = Modifier.width(20.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                // Estado da Coleção (Hunting, Owned, Avoid)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceCard, RoundedCornerShape(6.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CollectionStatus.entries.forEach { status ->
                            val isCurrent = game.collectionStatus == status
                            val bg = if (isCurrent) {
                                when (status) {
                                    CollectionStatus.HUNTING -> StatusEditionBg
                                    CollectionStatus.OWNED -> StatusEnglishBg
                                    CollectionStatus.PASS -> StatusRiskBg
                                }
                            } else Color.Transparent

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(bg, RoundedCornerShape(4.dp))
                                    .clickable { viewModel.updateGameStatus(game, status) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val label = when (status) {
                                    CollectionStatus.OWNED -> TextKeys.Status.OWNED
                                    CollectionStatus.HUNTING -> TextKeys.Status.HUNTING
                                    CollectionStatus.PASS -> TextKeys.Status.AVOID
                                }
                                Text(
                                    text = label,
                                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                                    color = if (isCurrent) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }
                }

                // Banner de Status de Idioma
                item {
                    LanguageRiskBadge(status = game.languageStatus, showDescription = true)
                }

                // Swiss Market Radar
                if (game.marketRadar != null) {
                    item {
                        SwissMarketRadarView(
                            radar = game.marketRadar,
                            askingPriceChf = game.askingPriceChf
                        )
                    }
                }

                // Matriz de SKUs Seguros vs de Risco
                item {
                    SafeSkuMatrixView(
                        safeSkus = game.safeSkus,
                        riskySkus = game.riskySkus
                    )
                }

                // Ficha Técnica do Jogo
                item {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SurfaceCard,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = TextKeys.Dossier.TITLE, style = HeadlineSm, color = TextPrimary)

                            if (game.spottedLocation.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = TextKeys.Dossier.SPOTTED_LOCATION, style = BodySm, color = TextSecondary)
                                    Text(text = game.spottedLocation, style = BodySm, color = TextPrimary)
                                }
                            }

                            if (!game.productCode.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = TextKeys.Dossier.PRODUCT_CODE, style = BodySm, color = TextSecondary)
                                    Text(text = game.productCode, style = CodeSkuStyle, color = TextPrimary)
                                }
                            }

                            if (!game.barcode.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = TextKeys.Dossier.BARCODE, style = BodySm, color = TextSecondary)
                                    Text(text = game.barcode, style = CodeSkuStyle, color = TextSecondary)
                                }
                            }

                            if (!game.censorshipWarning.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(StatusRiskBg, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "⚠️ " + game.censorshipWarning,
                                        style = BodySm,
                                        color = StatusRiskFg
                                    )
                                }
                            }

                            if (game.collectorVerdict.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SurfaceElevated, RoundedCornerShape(4.dp))
                                        .padding(10.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(text = "VEREDICTO DE MERCADO", style = LabelFilterStyle, color = AccentBlue)
                                        Text(text = game.collectorVerdict, style = BodySm, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Chat com Gemini Flash
                item {
                    Text(text = "💬 " + TextKeys.Dossier.CHAT_TITLE, style = HeadlineSm, color = TextPrimary)
                }

                items(state.activeChatMessages, key = { it.id }) { msg ->
                    GeminiChatBubble(message = msg)
                }
            }

            // Input inferior de mensagem
            Surface(
                color = SurfaceCard,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = followUpQuestion,
                        onValueChange = { followUpQuestion = it },
                        textStyle = BodyMd.copy(color = TextPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .background(SurfaceBase, RoundedCornerShape(4.dp))
                            .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                            .onFocusChanged { isChatFocused = it.isFocused },
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (followUpQuestion.isEmpty() && !isChatFocused) {
                                    Text(
                                        text = TextKeys.Dossier.CHAT_PLACEHOLDER,
                                        style = BodySm.copy(color = StatusUnverifiedFg)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Button(
                        onClick = {
                            val q = followUpQuestion.trim()
                            if (q.isNotEmpty()) {
                                viewModel.sendFollowUpMessage(q)
                                followUpQuestion = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(text = TextKeys.Dossier.CHAT_SEND, style = LabelFilterStyle, color = Color.White)
                    }
                }
            }
        }
    }
}
