package com.retrocollector.app.core.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrocollector.app.core.domain.model.AppSection
import com.retrocollector.app.core.presentation.text.TextKeys
import com.retrocollector.app.core.presentation.theme.*

@Composable
fun TactileBottomNavigation(
    activeSection: AppSection,
    wishlistCount: Int,
    collectionCount: Int,
    onSectionSelect: (AppSection) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .border(
                width = 1.dp,
                color = BorderSubtle,
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
            )
            .navigationBarsPadding()
            .height(64.dp)
            .padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Activity (Catalog)
            TactileBottomNavItem(
                label = AppSection.ACTIVITY.label,
                isSelected = activeSection == AppSection.ACTIVITY,
                badgeCount = 0,
                badgeBg = Color.Transparent,
                onClick = { onSectionSelect(AppSection.ACTIVITY) },
                icon = { color -> ActivityNavIcon(color = color) }
            )

            // 2. Discover
            TactileBottomNavItem(
                label = AppSection.DISCOVER.label,
                isSelected = activeSection == AppSection.DISCOVER,
                badgeCount = 0,
                badgeBg = Color.Transparent,
                onClick = { onSectionSelect(AppSection.DISCOVER) },
                icon = { color -> DiscoverNavIcon(color = color) }
            )

            // 3. Wishlist (with badge)
            TactileBottomNavItem(
                label = AppSection.WISHLIST.label,
                isSelected = activeSection == AppSection.WISHLIST,
                badgeCount = wishlistCount,
                badgeBg = Color(0xFF6366F1), // Vibrant Indigo
                onClick = { onSectionSelect(AppSection.WISHLIST) },
                icon = { color -> WishlistNavIcon(color = color) }
            )

            // 4. Collection (with badge)
            TactileBottomNavItem(
                label = AppSection.COLLECTION.label,
                isSelected = activeSection == AppSection.COLLECTION,
                badgeCount = collectionCount,
                badgeBg = Color(0xFF3F3F46), // Muted Zinc Badge
                onClick = { onSectionSelect(AppSection.COLLECTION) },
                icon = { color -> CollectionNavIcon(color = color) }
            )

            // 5. Settings
            TactileBottomNavItem(
                label = TextKeys.Navigation.TAB_SETTINGS,
                isSelected = false,
                badgeCount = 0,
                badgeBg = Color.Transparent,
                onClick = onOpenSettings,
                icon = { color -> SettingsNavIcon(color = color) }
            )
        }
    }
}

@Composable
private fun RowScope.TactileBottomNavItem(
    label: String,
    isSelected: Boolean,
    badgeCount: Int,
    badgeBg: Color,
    onClick: () -> Unit,
    icon: @Composable (color: Color) -> Unit
) {
    val activeColor = Color(0xFF818CF8) // Vibrant Light Indigo / Primary
    val inactiveColor = TextSecondary

    val color by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200)
    )

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            icon(color)

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-4).dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 4.5.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else "$badgeCount",
                        style = CodeSkuStyle.copy(
                            fontSize = 9.sp,
                            color = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            style = LabelFilterStyle.copy(
                fontSize = 11.sp,
                color = color
            ),
            maxLines = 1
        )

        // Active indicator dot
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(4.dp)
                .clip(CircleShape)
                .background(if (isSelected) activeColor else Color.Transparent)
        )
    }
}

// -------------------------------------------------------------------------
// VECTOR CANVAS ICONS (Pixel-perfect matching Stitch Mobile Tab bar)
// -------------------------------------------------------------------------

@Composable
fun ActivityNavIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = size.width
        val h = size.height

        // Clipboard outline
        val path = Path().apply {
            moveTo(w * 0.28f, h * 0.22f)
            lineTo(w * 0.18f, h * 0.22f)
            quadraticBezierTo(w * 0.12f, h * 0.22f, w * 0.12f, h * 0.32f)
            lineTo(w * 0.12f, h * 0.86f)
            quadraticBezierTo(w * 0.12f, h * 0.94f, w * 0.20f, h * 0.94f)
            lineTo(w * 0.80f, h * 0.94f)
            quadraticBezierTo(w * 0.88f, h * 0.94f, w * 0.88f, h * 0.86f)
            lineTo(w * 0.88f, h * 0.32f)
            quadraticBezierTo(w * 0.88f, h * 0.22f, w * 0.82f, h * 0.22f)
            lineTo(w * 0.72f, h * 0.22f)
        }
        drawPath(path, color, style = stroke)

        // Clip on top
        val clipPath = Path().apply {
            moveTo(w * 0.34f, h * 0.22f)
            lineTo(w * 0.34f, h * 0.12f)
            quadraticBezierTo(w * 0.34f, h * 0.06f, w * 0.42f, h * 0.06f)
            lineTo(w * 0.58f, h * 0.06f)
            quadraticBezierTo(w * 0.66f, h * 0.06f, w * 0.66f, h * 0.12f)
            lineTo(w * 0.66f, h * 0.22f)
            close()
        }
        drawPath(clipPath, color, style = stroke)

        // Internal lines
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.46f),
            end = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.46f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.64f),
            end = androidx.compose.ui.geometry.Offset(w * 0.60f, h * 0.64f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun DiscoverNavIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = size.width
        val h = size.height

        // Compass outer circle
        drawCircle(
            color = color,
            radius = w * 0.42f,
            style = stroke
        )

        // Compass needle
        val needle = Path().apply {
            moveTo(w * 0.66f, h * 0.34f)
            lineTo(w * 0.54f, h * 0.54f)
            lineTo(w * 0.34f, h * 0.66f)
            lineTo(w * 0.46f, h * 0.46f)
            close()
        }
        drawPath(needle, color, style = stroke)
    }
}

@Composable
fun WishlistNavIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = size.width
        val h = size.height

        // Heart outline
        val heart = Path().apply {
            moveTo(w * 0.5f, h * 0.82f)
            cubicTo(w * 0.12f, h * 0.55f, w * 0.08f, h * 0.28f, w * 0.28f, h * 0.18f)
            cubicTo(w * 0.40f, h * 0.12f, w * 0.48f, h * 0.24f, w * 0.5f, h * 0.30f)
            cubicTo(w * 0.52f, h * 0.24f, w * 0.60f, h * 0.12f, w * 0.72f, h * 0.18f)
            cubicTo(w * 0.92f, h * 0.28f, w * 0.88f, h * 0.55f, w * 0.5f, h * 0.82f)
            close()
        }
        drawPath(heart, color, style = stroke)
    }
}

@Composable
fun CollectionNavIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = size.width
        val h = size.height

        // Isometric Box / Cube outline
        val topFace = Path().apply {
            moveTo(w * 0.5f, h * 0.12f)
            lineTo(w * 0.84f, h * 0.30f)
            lineTo(w * 0.5f, h * 0.48f)
            lineTo(w * 0.16f, h * 0.30f)
            close()
        }
        drawPath(topFace, color, style = stroke)

        // Left Face
        val leftFace = Path().apply {
            moveTo(w * 0.16f, h * 0.30f)
            lineTo(w * 0.16f, h * 0.68f)
            lineTo(w * 0.5f, h * 0.88f)
            lineTo(w * 0.5f, h * 0.48f)
        }
        drawPath(leftFace, color, style = stroke)

        // Right Face
        val rightFace = Path().apply {
            moveTo(w * 0.84f, h * 0.30f)
            lineTo(w * 0.84f, h * 0.68f)
            lineTo(w * 0.5f, h * 0.88f)
        }
        drawPath(rightFace, color, style = stroke)
    }
}

@Composable
fun SettingsNavIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        val w = size.width
        val h = size.height

        // Inner Circle
        drawCircle(
            color = color,
            radius = w * 0.18f,
            style = stroke
        )

        // Cog Teeth Circle
        drawCircle(
            color = color,
            radius = w * 0.38f,
            style = Stroke(
                width = 1.6f.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        )
    }
}
