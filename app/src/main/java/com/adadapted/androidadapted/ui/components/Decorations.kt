package com.adadapted.androidadapted.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.adadapted.androidadapted.ui.theme.AccentColors
import com.adadapted.androidadapted.ui.theme.AppColors
import com.adadapted.androidadapted.ui.theme.AppTheme
import com.adadapted.androidadapted.ui.theme.forText
import com.adadapted.androidadapted.ui.theme.onAccent

/** An icon on a translucent wash of its own color. */
@Composable
fun IconBadge(
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    shape: Shape = RoundedCornerShape(size * 0.32f),
) {
    Surface(shape = shape, color = color.wash(), modifier = modifier.size(size)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = color.forText(), modifier = Modifier.size(size * 0.55f))
        }
    }
}

/** A short label on a solid chip of its color, like an item's origin or a setting's value. */
@Composable
fun Pill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Surface(shape = CircleShape, color = color.forText(), contentColor = onAccent(), modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (icon != null) Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp))
            Text(text, style = MaterialTheme.typography.labelSmall)
        }
    }
}

/** A light tint of this color over the page, so it stands out on the cards it sits on. */
@Composable
internal fun Color.wash(): Color = copy(alpha = 0.16f).compositeOver(MaterialTheme.colorScheme.surface)

/** The hairline every content card carries, so cards read clearly against the page. */
@Composable
fun cardBorder(): BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)

/**
 * The outlined card that holds content: list rows, log entries, ad zones. Rows default to the
 * medium shape; larger, standalone cards pass a larger one.
 */
@Composable
fun ContentCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val color = MaterialTheme.colorScheme.surfaceContainerLow
    if (onClick == null) {
        Surface(shape = shape, color = color, border = cardBorder(), modifier = modifier, content = content)
    } else {
        Surface(onClick = onClick, shape = shape, color = color, border = cardBorder(), modifier = modifier, content = content)
    }
}

/** The gap between rows in a list. */
val ListRowSpacing = 8.dp

/** Extra space that sets a new section apart in a list of rows, on top of [ListRowSpacing]. */
val SectionSpacing = 16.dp

/**
 * A matte-black card with a soft glow in one corner: the "chocolate chip" surface used for hero
 * banners and feature cards. Text and icons inside default to [AppColors.onMatteCard].
 */
@Composable
fun MatteCard(
    modifier: Modifier = Modifier,
    glow: Color = AccentColors.Mint,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(colors.matteCard)
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        colors = listOf(glow.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(size.width, 0f),
                        radius = size.maxDimension * 0.75f,
                    ),
                )
            },
    ) {
        CompositionLocalProvider(LocalContentColor provides colors.onMatteCard) { content() }
    }
}
