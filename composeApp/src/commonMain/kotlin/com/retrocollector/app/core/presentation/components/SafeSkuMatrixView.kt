package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.retrocollector.app.core.domain.model.SkuInfo
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SafeSkuMatrixView(
    safeSkus: List<SkuInfo>,
    riskySkus: List<SkuInfo>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = TextKeys.Sku.MATRIX_TITLE,
            style = LabelFilterStyle.copy(fontSize = 11.sp),
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Códigos Seguros
            safeSkus.forEach { sku ->
                Row(
                    modifier = Modifier
                        .background(StatusEnglishBg, RoundedCornerShape(4.dp))
                        .border(1.dp, StatusEnglishFg.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
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
                    Text(text = "✔", color = StatusEnglishFg, fontSize = 11.sp)
                }
            }

            // Códigos de Risco
            riskySkus.forEach { sku ->
                Row(
                    modifier = Modifier
                        .background(StatusRiskBg, RoundedCornerShape(4.dp))
                        .border(1.dp, StatusRiskFg.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
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
                    Text(text = "⊘", color = StatusRiskFg, fontSize = 11.sp)
                }
            }
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
            modifier = Modifier.padding(16.dp)
        )
    }
}
