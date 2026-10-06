package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.generated.resources.*
import com.retrocollector.app.core.domain.model.SkuInfo
import com.retrocollector.app.core.presentation.theme.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SafeSkuMatrixView(
    safeSkus: List<SkuInfo>,
    riskySkus: List<SkuInfo>,
    activeSkuCode: String? = null,
    onSelectSku: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cleanActiveSku = activeSkuCode?.trim()?.ifBlank { null }
    val hasMatchingSku = cleanActiveSku != null && (
        safeSkus.any { it.code.equals(cleanActiveSku, ignoreCase = true) } ||
        riskySkus.any { it.code.equals(cleanActiveSku, ignoreCase = true) }
    )

    val activeIsRisky = cleanActiveSku != null && riskySkus.any { it.code.equals(cleanActiveSku, ignoreCase = true) }
    val activeIsSafe = cleanActiveSku != null && safeSkus.any { it.code.equals(cleanActiveSku, ignoreCase = true) }
    val activeHeaderBorder = when {
        activeIsRisky -> StatusRiskFg
        activeIsSafe -> StatusEnglishFg
        else -> AccentBlue
    }
    val activeHeaderBg = when {
        activeIsRisky -> StatusRiskBg
        activeIsSafe -> StatusEnglishBg
        else -> SurfaceElevated
    }
    val activeHeaderFg = when {
        activeIsRisky -> StatusRiskFg
        activeIsSafe -> StatusEnglishFg
        else -> AccentBlue
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.sku_matrix_title),
                style = LabelFilterStyle.copy(fontSize = 11.sp),
                color = TextSecondary
            )
            if (cleanActiveSku != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${stringResource(Res.string.sku_active_edition)}:",
                        style = BodySm.copy(fontSize = 11.sp),
                        color = TextSecondary
                    )
                    Box(
                        modifier = Modifier
                            .background(activeHeaderBg, RoundedCornerShape(3.dp))
                            .border(1.dp, activeHeaderBorder, RoundedCornerShape(3.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = cleanActiveSku,
                            color = activeHeaderFg,
                            style = CodeSkuStyle.copy(fontSize = 11.sp)
                        )
                    }
                }
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // If activeSkuCode is set but not in safeSkus or riskySkus, show it first as Detected Active SKU
            if (cleanActiveSku != null && !hasMatchingSku) {
                Row(
                    modifier = Modifier
                        .background(SurfaceElevated, RoundedCornerShape(4.dp))
                        .border(2.dp, AccentBlue, RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = cleanActiveSku,
                        color = TextPrimary,
                        style = CodeSkuStyle,
                        maxLines = 1,
                        softWrap = false
                    )
                    Box(
                        modifier = Modifier
                            .background(AccentBlue, RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.sku_this_copy),
                            color = Color.White,
                            fontSize = 9.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Text(text = "🎯", fontSize = 11.sp)
                }
            }

            // Safe Codes
            safeSkus.forEach { sku ->
                val isActive = cleanActiveSku != null && sku.code.equals(cleanActiveSku, ignoreCase = true)
                val itemModifier = Modifier
                    .background(
                        if (isActive) StatusEnglishBg else StatusEnglishBg.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        if (isActive) 2.dp else 1.dp,
                        if (isActive) StatusEnglishFg else StatusEnglishFg.copy(alpha = 0.4f),
                        RoundedCornerShape(4.dp)
                    )
                    .then(
                        if (onSelectSku != null) Modifier.clickable { onSelectSku(sku.code) } else Modifier
                    )
                    .padding(horizontal = 7.dp, vertical = 3.dp)

                Row(
                    modifier = itemModifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = sku.code,
                        color = StatusEnglishFg,
                        style = CodeSkuStyle,
                        maxLines = 1,
                        softWrap = false
                    )
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = sku.region,
                            color = StatusEnglishFg,
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .background(StatusEnglishFg, RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.sku_this_copy),
                                color = Color.Black,
                                fontSize = 9.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    Text(text = "✔", color = StatusEnglishFg, fontSize = 11.sp)
                }
            }

            // Risky Codes
            riskySkus.forEach { sku ->
                val isActive = cleanActiveSku != null && sku.code.equals(cleanActiveSku, ignoreCase = true)
                val itemModifier = Modifier
                    .background(
                        if (isActive) StatusRiskBg else StatusRiskBg.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .border(
                        if (isActive) 2.dp else 1.dp,
                        if (isActive) StatusRiskFg else StatusRiskFg.copy(alpha = 0.4f),
                        RoundedCornerShape(4.dp)
                    )
                    .then(
                        if (onSelectSku != null) Modifier.clickable { onSelectSku(sku.code) } else Modifier
                    )
                    .padding(horizontal = 7.dp, vertical = 3.dp)

                Row(
                    modifier = itemModifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = sku.code,
                        color = StatusRiskFg,
                        style = CodeSkuStyle,
                        maxLines = 1,
                        softWrap = false
                    )
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = sku.region,
                            color = StatusRiskFg,
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .background(StatusRiskFg, RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.sku_this_copy),
                                color = Color.White,
                                fontSize = 9.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    Text(text = "⊘", color = StatusRiskFg, fontSize = 11.sp)
                }
            }
        }

        if (onSelectSku != null) {
            Text(
                text = stringResource(Res.string.sku_click_to_select),
                style = BodySm.copy(fontSize = 10.sp),
                color = StatusUnverifiedFg
            )
        }
    }
}

@Preview
@Composable
fun SafeSkuMatrixViewPreview() {
    RetroTactileTheme {
        SafeSkuMatrixView(
            safeSkus = listOf(
                SkuInfo(code = "DOL-P-G4BE", region = "UKV", editionNote = "English audio + Multi-5 subs", isSafe = true),
                SkuInfo(code = "DOL-P-G4BP", region = "EUR", editionNote = "English audio + Multi-5 subs", isSafe = true)
            ),
            riskySkus = listOf(
                SkuInfo(code = "DOL-P-G4BD", region = "NOE", editionNote = "German text & subs only", isSafe = false)
            ),
            activeSkuCode = "DOL-P-G4BE",
            onSelectSku = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
