package com.retrocollector.app.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.CollectionStatus
import com.retrocollector.app.core.presentation.theme.*

@Composable
fun CollectionStatusSelector(
    currentStatus: CollectionStatus,
    onStatusSelect: (CollectionStatus) -> Unit,
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = false
) {
    Row(
        modifier = modifier
            .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.wrapContentWidth())
            .background(SurfaceBase, RoundedCornerShape(4.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        CollectionStatus.displayStatuses.forEach { status ->
            @Suppress("DEPRECATION")
            val isCurrent = currentStatus == status ||
                (status == CollectionStatus.WISHLIST && currentStatus == CollectionStatus.HUNTING)

            val bg = if (isCurrent) {
                when (status) {
                    CollectionStatus.WISHLIST -> StatusEditionBg
                    CollectionStatus.OWNED -> StatusEnglishBg
                    CollectionStatus.PASS -> StatusRiskBg
                    else -> StatusEditionBg
                }
            } else Color.Transparent

            val textColor = if (isCurrent) TextPrimary else TextSecondary

            Box(
                modifier = Modifier
                    .then(if (fillMaxWidth) Modifier.weight(1f) else Modifier)
                    .defaultMinSize(minHeight = 36.dp)
                    .background(bg, RoundedCornerShape(3.dp))
                    .semantics {
                        role = Role.RadioButton
                        selected = isCurrent
                    }
                    .clickable { onStatusSelect(status) }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${status.icon} ${status.label}",
                    style = LabelFilterStyle.copy(fontSize = 11.sp),
                    color = textColor,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
