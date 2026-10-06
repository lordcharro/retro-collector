package com.retrocollector.app.dossier.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.retrocollector.app.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.retrocollector.app.core.domain.model.GameItem
import com.retrocollector.app.core.presentation.components.PlatformBadge
import com.retrocollector.app.core.presentation.theme.*

@Composable
fun TactileGameDossierMediaHeader(
    game: GameItem,
    modifier: Modifier = Modifier,
    showTitle: Boolean = true
) {
    val platformColor = Color(game.platform.brandColorHex)
    var isDescriptionExpanded by remember(game.id) { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. MEDIA ROW (Box Art + Optional Spine Target) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val hasSpine = !game.spineImageUrl.isNullOrBlank()

            // Box Art Primary Preview (Width expands if no spine)
            Box(
                modifier = Modifier
                    .weight(if (hasSpine) 0.68f else 1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceContainerHigh)
            ) {
                if (!game.coverImageUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = game.coverImageUrl,
                        contentDescription = "${game.title} cover art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize().background(SurfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = platformColor,
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            TactileGameCoverPlaceholder(game = game)
                        }
                    )
                } else {
                    TactileGameCoverPlaceholder(game = game)
                }

                // Bottom Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, SurfaceBase.copy(alpha = 0.85f))
                            )
                        )
                )

                // Docked Edition / Platform Badge Pill
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceBase.copy(alpha = 0.90f))
                        .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "✓",
                        color = StatusEnglishFg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(Res.string.dossier_pal_edition_badge, game.platform.displayName),
                        style = LabelBadgeStyle.copy(fontSize = 10.sp),
                        color = TextPrimary
                    )
                }
            }

            // Spine Macro Target Preview (Optional Side Card)
            if (hasSpine) {
                Box(
                    modifier = Modifier
                        .weight(0.32f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceContainerHigh)
                ) {
                    SubcomposeAsyncImage(
                        model = game.spineImageUrl,
                        contentDescription = "${game.title} spine close-up",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize().background(SurfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = AccentBlue,
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier.fillMaxSize().background(SurfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(Res.string.dossier_spine_label),
                                    style = CodeSkuStyle,
                                    color = TextSecondary
                                )
                            }
                        }
                    )

                    // Spine Top Label
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SurfaceBase.copy(alpha = 0.85f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.dossier_spine_label),
                            style = CodeSkuStyle.copy(fontSize = 9.sp),
                            color = TextPrimary
                        )
                    }

                    // Spine Target SKU Badge
                    if (!game.productCode.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(SurfaceBase.copy(alpha = 0.90f))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Column {
                                Text(
                                    text = stringResource(Res.string.dossier_target_sku),
                                    style = LabelFilterStyle.copy(fontSize = 8.sp),
                                    color = TextSecondary
                                )
                                Text(
                                    text = game.productCode,
                                    style = CodeSkuStyle.copy(fontSize = 10.sp),
                                    color = AccentBlue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. TITLE & PLATFORM IDENTITY ---
        if (showTitle) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // --- 3. CANONICAL GAME DESCRIPTION ---
        if (game.description.isNotBlank()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceContainer.copy(alpha = 0.6f))
                    .border(BorderStroke(1.dp, BorderSubtle), RoundedCornerShape(6.dp))
                    .padding(10.dp)
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.dossier_overview_label),
                        style = LabelBadgeStyle.copy(fontSize = 10.sp, letterSpacing = 1.sp),
                        color = TextSecondary
                    )
                    if (game.description.length > 140) {
                        Text(
                            text = if (isDescriptionExpanded) {
                                stringResource(Res.string.dossier_show_less)
                            } else {
                                stringResource(Res.string.dossier_read_more)
                            },
                            style = LabelFilterStyle.copy(fontSize = 11.sp),
                            color = AccentBlue,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = game.description,
                    style = BodyMd,
                    color = TextPrimary,
                    maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TactileGameCoverPlaceholder(
    game: GameItem,
    modifier: Modifier = Modifier
) {
    val platformColor = Color(game.platform.brandColorHex)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceContainerLow),
        contentAlignment = Alignment.Center
    ) {
        // Decorative cartridge lines
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Accent Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(platformColor)
            )

            // Center Cartridge Emblem
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(platformColor.copy(alpha = 0.15f))
                        .border(1.dp, platformColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = game.platform.id.uppercase().take(3),
                        color = platformColor,
                        style = CodeSkuStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = game.title,
                    style = LabelFilterStyle.copy(fontSize = 12.sp),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom subtle pin indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(8) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(width = 6.dp, height = 3.dp)
                            .background(Color(0xFFD97706).copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}
