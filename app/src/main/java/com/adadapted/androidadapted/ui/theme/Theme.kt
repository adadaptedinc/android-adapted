package com.adadapted.androidadapted.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AATealLight,
    onPrimary = DarkBackground,
    primaryContainer = AATeal,
    onPrimaryContainer = LightSurface,
    secondary = AAPurpleLight,
    onSecondary = DarkBackground,
    secondaryContainer = AAPurpleMid,
    onSecondaryContainer = LightSurface,
    tertiary = AABlue,
    background = DarkBackground,
    onBackground = LightSurface,
    surface = DarkSurface,
    onSurface = LightSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = AATealLight
)

private val LightColorScheme = lightColorScheme(
    primary = AAPurpleDark,
    onPrimary = LightSurface,
    primaryContainer = AAPurpleMid,
    onPrimaryContainer = LightSurface,
    secondary = AATeal,
    onSecondary = LightSurface,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = AAPurpleDark,
    tertiary = AABlue,
    background = LightBackground,
    onBackground = AAPurpleDark,
    surface = LightSurface,
    onSurface = AAPurpleDark,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = AAPurpleMid
)

@Composable
fun AndroidAdaptedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
