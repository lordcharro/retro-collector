package com.retrocollector.app.dossier.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.generated.resources.*
import com.retrocollector.app.core.domain.model.CollectionStatus
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.presentation.components.*
import com.retrocollector.app.core.presentation.theme.*
import org.jetbrains.compose.resources.stringResource
import com.retrocollector.app.core.domain.model.ChatMessage
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.DiscoveredGameItem
import com.retrocollector.app.core.domain.model.LanguageStatus
import com.retrocollector.app.core.domain.model.MessageSender
import com.retrocollector.app.core.domain.model.SkuInfo
import com.retrocollector.app.core.domain.model.SwissMarketRadar
import com.retrocollector.app.discovery.presentation.components.SimilarGamesShelf
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MobileGameDetailScreen(
    game: GameItem,
    chatMessages: List<ChatMessage>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isAnalyzing: Boolean = false,
    currency: String = "CHF",
    similarGames: List<DiscoveredGameItem> = emptyList(),
    isSimilarGamesLoading: Boolean = false,
    onUpdateGameStatus: (GameItem, CollectionStatus) -> Unit = { _, _ -> },
    onUpdatePaidPrice: (GameItem, Double?) -> Unit = { _, _ -> },
    onUpdateProductCode: (GameItem, String?) -> Unit = { _, _ -> },
    onDeleteGame: (String) -> Unit = {},
    onSendFollowUpMessage: (String) -> Unit = {},
    onDeleteChatMessage: (String) -> Unit = {},
    onSelectSimilarGame: (DiscoveredGameItem) -> Unit = {},
    onAddSimilarGameToWishlist: (DiscoveredGameItem) -> Unit = {},
    onRefreshSimilarGames: () -> Unit = {},
    onAddOrUpdateOffer: (GameItem, com.retrocollector.app.core.domain.model.GameOffer) -> Unit = { _, _ -> },
    onDeleteOffer: (GameItem, String) -> Unit = { _, _ -> },
    onConvertOfferToOwned: (GameItem, String?, Double, com.retrocollector.app.core.domain.model.GameCondition) -> Unit = { _, _, _, _ -> }
) {
    var followUpQuestion by remember { mutableStateOf("") }
    var editingOffer by remember { mutableStateOf<com.retrocollector.app.core.domain.model.GameOffer?>(null) }
    var isAddOfferOpen by remember { mutableStateOf(false) }
    var acquisitionOffer by remember { mutableStateOf<com.retrocollector.app.core.domain.model.GameOffer?>(null) }
    var isAcquisitionOpen by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceBase,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(BorderStroke(1.dp, BorderSubtle))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onBack) {
                        Text("← " + stringResource(Res.string.nav_back), color = AccentBlue, style = LabelFilterStyle)
                    }

                var showDeleteConfirm by remember(game.id) { mutableStateOf(false) }

                IconButton(
                    onClick = { showDeleteConfirm = true }
                ) {
                    Text("🗑️", fontSize = 16.sp)
                }

                if (showDeleteConfirm) {
                    TactileConfirmDialog(
                        title = stringResource(Res.string.dossier_delete_confirm_title),
                        message = stringResource(Res.string.dossier_delete_confirm_message),
                        confirmLabel = stringResource(Res.string.dossier_delete_confirm_button),
                        dismissLabel = stringResource(Res.string.dossier_delete_cancel_button),
                        isDestructive = true,
                        onConfirm = {
                            showDeleteConfirm = false
                            onDeleteGame(game.id)
                            onBack()
                        },
                        onDismiss = { showDeleteConfirm = false }
                    )
                }
            }
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
                // Game Title & Platform Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = game.title,
                            style = HeadlineMd,
                            color = TextPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PlatformBadge(platform = game.platform)
                            if (game.releaseYear.isNotBlank()) {
                                Text(
                                    text = "•  ${game.releaseYear}",
                                    style = BodySm,
                                    color = TextSecondary
                                )
                            }
                            if (game.spottedLocation.isNotBlank()) {
                                Text(
                                    text = "•  ${game.spottedLocation}",
                                    style = CodeSkuStyle.copy(fontSize = 11.sp),
                                    color = StatusUnverifiedFg,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Collection Status (Wishlist, Owned, Pass)
                item {
                    CollectionStatusSelector(
                        currentStatus = game.collectionStatus,
                        onStatusSelect = { newStatus ->
                            if (newStatus == CollectionStatus.OWNED && game.collectionStatus == CollectionStatus.WISHLIST) {
                                acquisitionOffer = game.bestOffer
                                isAcquisitionOpen = true
                            } else {
                                onUpdateGameStatus(game, newStatus)
                            }
                        },
                        fillMaxWidth = true
                    )
                }

                // Purchase Price Field (when Owned)
                if (game.collectionStatus == CollectionStatus.OWNED) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SurfaceCard,
                            border = BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = stringResource(Res.string.dossier_paid_price_label),
                                        style = LabelFilterStyle,
                                        color = StatusEnglishFg
                                    )
                                    Text(
                                        text = stringResource(Res.string.dossier_paid_price_desc),
                                        style = BodySm.copy(fontSize = 11.sp),
                                        color = TextSecondary
                                    )
                                }
                                PaidPriceInput(
                                    paidPrice = game.paidPriceChf,
                                    currency = currency,
                                    onPriceSubmitted = { onUpdatePaidPrice(game, it) }
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
                            currency = currency
                        )
                    }
                }

                // Stores & Live Offers Section
                item {
                    com.retrocollector.app.offers.presentation.components.StoreOffersSection(
                        offers = game.offers,
                        currency = currency,
                        isCompactLayout = true,
                        onAddOfferClick = { isAddOfferOpen = true },
                        onEditOfferClick = { offer -> editingOffer = offer },
                        onDeleteOfferClick = { offer -> onDeleteOffer(game, offer.id) },
                        onMarkAsBoughtClick = { offer ->
                            acquisitionOffer = offer
                            isAcquisitionOpen = true
                        }
                    )
                }

                // Safe vs Risky SKU Matrix
                item {
                    SafeSkuMatrixView(
                        safeSkus = game.safeSkus,
                        riskySkus = game.riskySkus,
                        activeSkuCode = game.productCode,
                        onSelectSku = { skuCode -> onUpdateProductCode(game, skuCode) }
                    )
                }

                // Game Technical Details
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
                            Text(text = stringResource(Res.string.dossier_title), style = HeadlineSm, color = TextPrimary)

                            if (game.spottedLocation.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = stringResource(Res.string.dossier_spotted_location), style = BodySm, color = TextSecondary)
                                    Text(text = game.spottedLocation, style = BodySm, color = TextPrimary)
                                }
                            }

                            if (!game.productCode.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = stringResource(Res.string.dossier_product_code), style = BodySm, color = TextSecondary)
                                    Text(text = game.productCode, style = CodeSkuStyle, color = TextPrimary)
                                }
                            }

                            if (!game.barcode.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = stringResource(Res.string.dossier_barcode), style = BodySm, color = TextSecondary)
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
                                        Text(text = stringResource(Res.string.dossier_market_verdict), style = LabelFilterStyle, color = AccentBlue)
                                        Text(text = game.collectorVerdict, style = BodySm, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Shelf of Similar Games
                item {
                    SimilarGamesShelf(
                        similarGames = similarGames,
                        isLoading = isSimilarGamesLoading,
                        currency = currency,
                        onSelectGame = onSelectSimilarGame,
                        onAddToWishlist = onAddSimilarGameToWishlist,
                        onRefreshSimilarGames = onRefreshSimilarGames
                    )
                }

                // Chat with Gemini Flash
                item {
                    Text(text = "💬 " + stringResource(Res.string.dossier_chat_title), style = HeadlineSm, color = TextPrimary)
                }

                items(chatMessages, key = { it.id }) { msg ->
                    DeletableChatBubble(
                        message = msg,
                        onDeleteMessage = onDeleteChatMessage
                    )
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
                                text = stringResource(Res.string.dossier_analyzing_verification),
                                style = BodySm.copy(fontSize = 12.sp),
                                color = StatusEnglishFg
                            )
                        }
                    }
                }
            }

            // Bottom message input
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
                        placeholder = stringResource(Res.string.dossier_chat_placeholder),
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
                            Text(text = stringResource(Res.string.dossier_chat_send), style = LabelFilterStyle, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (isAddOfferOpen || editingOffer != null) {
        com.retrocollector.app.offers.presentation.dialog.AddEditOfferDialog(
            initialOffer = editingOffer,
            currency = currency,
            onSave = { offer ->
                onAddOrUpdateOffer(game, offer)
                isAddOfferOpen = false
                editingOffer = null
            },
            onDismiss = {
                isAddOfferOpen = false
                editingOffer = null
            }
        )
    }

    if (isAcquisitionOpen) {
        com.retrocollector.app.offers.presentation.dialog.AcquisitionModal(
            game = game,
            selectedOffer = acquisitionOffer,
            currency = currency,
            onConfirm = { finalPrice, condition, offerId ->
                onConvertOfferToOwned(game, offerId, finalPrice, condition)
                isAcquisitionOpen = false
                acquisitionOffer = null
            },
            onDismiss = {
                isAcquisitionOpen = false
                acquisitionOffer = null
            }
        )
    }
}

@Preview
@Composable
fun MobileGameDetailScreenPreview() {
    RetroTactileTheme {
        val sampleGame = GameItem(
            id = "gc_re4",
            title = "Resident Evil 4",
            franchiseName = "Resident Evil",
            platform = ConsolePlatform.GAMECUBE,
            releaseYear = "2005",
            productCode = "DOL-P-G4BE",
            spottedLocation = "Brockenhaus Bern",
            askingPriceChf = 35.0,
            paidPriceChf = 35.0,
            collectionStatus = CollectionStatus.OWNED,
            languageStatus = LanguageStatus.SUBS_ONLY,
            marketRadar = SwissMarketRadar(
                spottedPriceChf = 35.0,
                medianPriceChf = 31.50,
                historicalMinChf = 28.0,
                historicalMaxChf = 36.0,
                trend = "Stable"
            ),
            safeSkus = listOf(
                SkuInfo(
                    code = "DOL-P-G4BE",
                    region = "UKV",
                    editionNote = "EN audio + EN/FR/DE/ES/IT subs",
                    isSafe = true
                ),
                SkuInfo(code = "DOL-P-G4BP", region = "EUR", editionNote = "EN audio + Multi-5 subs", isSafe = true)
            ),
            riskySkus = listOf(
                SkuInfo(code = "DOL-P-G4BD", region = "NOE", editionNote = "German text & subs only", isSafe = false)
            ),
            collectorVerdict = "UKV/EUR edition recommended with full multilingual subtitles. Avoid German version with censored bonus modes."
        )
        val sampleMessages = listOf(
            ChatMessage(
                id = "msg_1",
                contextId = "gc_re4",
                sender = MessageSender.USER,
                text = "Which European SKU is best for full English support?"
            ),
            ChatMessage(
                id = "msg_2",
                contextId = "gc_re4",
                sender = MessageSender.GEMINI,
                text = "SKU DOL-P-G4BE (UKV) or DOL-P-G4BP (EUR) contains original English voiceover and uncut multilingual text."
            )
        )
        MobileGameDetailScreen(
            game = sampleGame,
            chatMessages = sampleMessages,
            onBack = {}
        )
    }
}
