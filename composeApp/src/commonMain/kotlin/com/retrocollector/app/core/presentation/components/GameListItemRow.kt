package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.ConsolePlatform
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.domain.model.LanguageStatus
import com.retrocollector.app.core.presentation.theme.*
import com.retrocollector.app.core.presentation.util.PriceFormatter
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun GameListItemRow(
    game: GameItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val platformColor = when (game.platform) {
        ConsolePlatform.N64 -> ConsoleN64
        ConsolePlatform.GAMECUBE -> ConsoleGamecube
        ConsolePlatform.PS3 -> ConsolePS3
        ConsolePlatform.SWITCH -> ConsoleSwitch
    }

    val rowBg = if (isSelected) SurfaceElevated else SurfaceCard

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .background(rowBg)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "${game.title}, Plataforma ${game.platform.displayName}, ${game.languageStatus.label}"
                stateDescription = if (isSelected) "Selecionado" else "Não selecionado"
                onClick(label = "Abrir dossiê de ${game.title}") {
                    onClick()
                    true
                }
            }
            .clickable(onClick = onClick)
    ) {
        // Indicador lateral de seleção ativa
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .align(Alignment.CenterStart)
                    .background(platformColor)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coluna Esquerda: Título, SKU e Localização
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = game.title,
                        style = HeadlineSm,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (!game.productCode.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .background(SurfaceContainer, RoundedCornerShape(3.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = game.productCode,
                                style = CodeSkuStyle.copy(fontSize = 11.sp),
                                color = AccentBlue,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.padding(top = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (game.spottedLocation.isNotBlank()) {
                        Text(
                            text = "• ${game.spottedLocation}",
                            style = BodySm,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (game.releaseYear.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .background(SurfaceContainerHigh, RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 0.5.dp)
                        ) {
                            Text(
                                text = game.releaseYear,
                                style = CodeSkuStyle.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // Coluna Direita: Preço em CHF e Badge de Risco de Língua
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val displayPrice = game.askingPriceChf ?: game.targetPriceChf ?: game.paidPriceChf

                Text(
                    text = PriceFormatter.format(displayPrice, "CHF"),
                    style = CodePriceStyle,
                    color = TextPrimary,
                    maxLines = 1,
                    softWrap = false
                )

                LanguageRiskBadge(status = game.languageStatus)
            }
        }
    }
}

@Preview
@Composable
internal fun GameListItemRowPreview() {
    RetroTactileTheme {
        GameListItemRow(
            game = GameItem(
                id = "gc_re4",
                title = "Resident Evil 4",
                franchiseName = "Resident Evil",
                platform = ConsolePlatform.GAMECUBE,
                releaseYear = "2005",
                productCode = "DOL-P-G4BE",
                spottedLocation = "Brockenhaus Bern",
                askingPriceChf = 35.0,
                languageStatus = LanguageStatus.SUBS_ONLY
            ),
            isSelected = true,
            onClick = {}
        )
    }
}

