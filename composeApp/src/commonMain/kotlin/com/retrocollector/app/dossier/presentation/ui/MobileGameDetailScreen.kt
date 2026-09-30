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
import com.retrocollector.app.core.domain.model.ChatMessage
import com.retrocollector.app.core.domain.model.DiscoveredGameItem
import com.retrocollector.app.discovery.presentation.components.SimilarGamesShelf

@Composable
fun MobileGameDetailScreen(
    game: GameItem,
    chatMessages: List<ChatMessage>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isAnalyzing: Boolean = false,
    similarGames: List<DiscoveredGameItem> = emptyList(),
    isSimilarGamesLoading: Boolean = false,
    onUpdateGameStatus: (GameItem, CollectionStatus) -> Unit = { _, _ -> },
    onSendFollowUpMessage: (String) -> Unit = {},
    onSelectSimilarGame: (DiscoveredGameItem) -> Unit = {},
    onAddSimilarGameToWishlist: (DiscoveredGameItem) -> Unit = {}
) {
    var followUpQuestion by remember { mutableStateOf("") }

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
                    CollectionStatusSelector(
                        currentStatus = game.collectionStatus,
                        onStatusSelect = { onUpdateGameStatus(game, it) },
                        fillMaxWidth = true
                    )
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

                // Prateleira de Jogos Semelhantes
                item {
                    SimilarGamesShelf(
                        similarGames = similarGames,
                        isLoading = isSimilarGamesLoading,
                        onSelectGame = onSelectSimilarGame,
                        onAddToWishlist = onAddSimilarGameToWishlist
                    )
                }

                // Chat com Gemini Flash
                item {
                    Text(text = "💬 " + TextKeys.Dossier.CHAT_TITLE, style = HeadlineSm, color = TextPrimary)
                }

                items(chatMessages, key = { it.id }) { msg ->
                    GeminiChatBubble(message = msg)
                }

                if (isAnalyzing) {
                    item(key = "mobile_typing_bubble") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = StatusEnglishFg,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Gemini a analisar verificação regional...",
                                style = BodySm.copy(fontSize = 12.sp),
                                color = StatusEnglishFg
                            )
                        }
                    }
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
                    TactileTextField(
                        value = followUpQuestion,
                        onValueChange = { followUpQuestion = it },
                        placeholder = TextKeys.Dossier.CHAT_PLACEHOLDER,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val q = followUpQuestion.trim()
                            if (q.isNotEmpty()) {
                                onSendFollowUpMessage(q)
                                followUpQuestion = ""
                            }
                        },
                        enabled = !isAnalyzing && followUpQuestion.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = TextKeys.Dossier.CHAT_SEND, style = LabelFilterStyle, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
