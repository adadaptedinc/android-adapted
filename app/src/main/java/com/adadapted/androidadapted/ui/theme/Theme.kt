package com.adadapted.androidadapted.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = MatteBlackLight,
    onPrimary = MintOnBlack,
    primaryContainer = SignatureMint,
    onPrimaryContainer = OnMintLight,
    secondary = DeepMintLight,
    onSecondary = OnDeepMintLight,
    secondaryContainer = SignatureMint,
    onSecondaryContainer = OnMintLight,
    tertiary = CocoaLight,
    onTertiary = OnCocoaLight,
    tertiaryContainer = CocoaContainerLight,
    onTertiaryContainer = OnCocoaContainerLight,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = SignatureMint,
    onPrimary = OnMint,
    primaryContainer = SignatureMint,
    onPrimaryContainer = OnMint,
    secondary = SageDark,
    onSecondary = OnMint,
    secondaryContainer = SignatureMint,
    onSecondaryContainer = OnMint,
    tertiary = CocoaDark,
    onTertiary = OnCocoaDark,
    tertiaryContainer = CocoaContainerDark,
    onTertiaryContainer = OnCocoaContainerDark,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun AndroidAdaptedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAppColors provides if (darkTheme) DarkAppColors else LightAppColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content,
        )
    }
}

/**
 * Top bars sit on the page, then turn solid in its color once content scrolls beneath them.
 *
 * The see-through state is that same color at zero alpha rather than [Color.Transparent], which is
 * transparent black: the bar animates between the two, and fading in from black flashes dark.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun appTopBarColors(): TopAppBarColors {
    val solid = MaterialTheme.colorScheme.background
    return TopAppBarDefaults.topAppBarColors(
        containerColor = solid.copy(alpha = 0f),
        scrolledContainerColor = solid,
    )
}
