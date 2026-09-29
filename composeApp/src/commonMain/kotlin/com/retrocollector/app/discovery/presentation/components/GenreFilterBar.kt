package com.retrocollector.app.discovery.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.GameGenre
import com.retrocollector.app.core.presentation.theme.*

@Composable
fun GenreFilterBar(
    selectedGenre: GameGenre,
    onGenreSelected: (GameGenre) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
    ) {
        items(GameGenre.entries) { genre ->
            val isSelected = selectedGenre == genre
            val containerColor = if (isSelected) ConsoleGamecube else SurfaceCard
            val textColor = if (isSelected) Color.White else TextSecondary
            val borderStroke = if (isSelected) null else BorderStroke(1.dp, BorderSubtle)

            Box(
                modifier = Modifier
                    .background(containerColor, RoundedCornerShape(4.dp))
                    .then(
                        if (borderStroke != null) {
                            Modifier.border(borderStroke, RoundedCornerShape(4.dp))
                        } else Modifier
                    )
                    .clickable { onGenreSelected(genre) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(text = genre.icon, fontSize = 12.sp)
                    Text(
                        text = genre.displayName,
                        style = LabelFilterStyle.copy(fontSize = 12.sp),
                        color = textColor
                    )
                }
            }
        }
    }
}
