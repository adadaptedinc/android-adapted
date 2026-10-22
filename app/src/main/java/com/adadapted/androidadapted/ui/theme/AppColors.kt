package com.adadapted.androidadapted.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Colors the app's look needs beyond Material's color scheme, provided by [AndroidAdaptedTheme]
 * and read through [AppTheme.colors].
 */
@Immutable
data class AppColors(
    val isLight: Boolean,
    /** The "chocolate chip" surface of hero banners and feature cards. */
    val matteCard: Color,
    val onMatteCard: Color,
    /** The bottom bar, matte black in both themes. */
    val navBar: Color,
    val onNavBar: Color,
    val navBarSelected: Color,
)

internal val LightAppColors = AppColors(
    isLight = true,
    matteCard = MatteCardLight,
    onMatteCard = Color.White,
    navBar = NavBarBlack,
    onNavBar = Color.White,
    navBarSelected = AccentColors.Mint,
)

internal val DarkAppColors = LightAppColors.copy(
    isLight = false,
    matteCard = MatteCardDark,
)

internal val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}
