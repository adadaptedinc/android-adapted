package com.adadapted.androidadapted.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// Mint chocolate chip: mint greens, matte blacks, and clean whites, with a little cocoa.

/** The signature mint both themes share, and the near-blacks that read on it. */
internal val SignatureMint = Color(0xFF3DDC97)
internal val OnMint = Color(0xFF00210F)
internal val OnMintLight = Color(0xFF002116)

// Light: a white page with crisp, outlined cards and near-black text. Mint and matte black are
// accents, not washes, so everything stays easy to read.
internal val MatteBlackLight = Color(0xFF111413)
internal val MintOnBlack = Color(0xFF9BF2CF)
internal val DeepMintLight = Color(0xFF006B47)
internal val OnDeepMintLight = Color(0xFFFFFFFF)
internal val CocoaLight = Color(0xFF6D4C3A)
internal val OnCocoaLight = Color(0xFFFFFFFF)
internal val CocoaContainerLight = Color(0xFFF4DCCD)
internal val OnCocoaContainerLight = Color(0xFF2A160B)

internal val SurfaceLight = Color(0xFFFFFFFF)
internal val OnSurfaceLight = Color(0xFF0B0F0D)
internal val SurfaceVariantLight = Color(0xFFEDF0EE)
internal val OnSurfaceVariantLight = Color(0xFF2F3833)
internal val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
internal val SurfaceContainerLowLight = Color(0xFFF4F6F5)
internal val SurfaceContainerLight = Color(0xFFEDF0EE)
internal val SurfaceContainerHighLight = Color(0xFFE9EDEB)
internal val SurfaceContainerHighestLight = Color(0xFFDDE3E0)
internal val OutlineLight = Color(0xFF5B6460)
internal val OutlineVariantLight = Color(0xFFCDD4D0)

// Dark: a near-black page with charcoal, outlined cards and bright mint accents.
internal val SageDark = Color(0xFF5EE6AE)
internal val CocoaDark = Color(0xFFE3BFA9)
internal val OnCocoaDark = Color(0xFF42291A)
internal val CocoaContainerDark = Color(0xFF5B3F2E)
internal val OnCocoaContainerDark = Color(0xFFFFDCC6)

internal val SurfaceDark = Color(0xFF0B0D0C)
internal val OnSurfaceDark = Color(0xFFF1F4F2)
internal val SurfaceVariantDark = Color(0xFF202523)
internal val OnSurfaceVariantDark = Color(0xFFC9D1CC)
internal val SurfaceContainerLowestDark = Color(0xFF060807)
internal val SurfaceContainerLowDark = Color(0xFF181C1A)
internal val SurfaceContainerDark = Color(0xFF1D2220)
internal val SurfaceContainerHighDark = Color(0xFF222826)
internal val SurfaceContainerHighestDark = Color(0xFF2B3230)
internal val OutlineDark = Color(0xFF9BA49F)
internal val OutlineVariantDark = Color(0xFF353B38)

/**
 * Accents that mark a kind of thing, like a list or a log category: a family of mints, plus cocoa
 * for the chocolate chips. Shown over a translucent wash of the same color.
 */
object AccentColors {
    /** The signature mint, bright enough to glow on matte black. */
    val Mint = SignatureMint
    val Jade = Color(0xFF00A878)
    val Seafoam = Color(0xFF2EC4B6)
    val Sky = Color(0xFF4FB6E0)
    val Lime = Color(0xFF8BCF3F)
    val Cocoa = Color(0xFF9A6A48)
    val Red = Color(0xFFFF5A5F)

    private val Cycle = listOf(Mint, Sky, Seafoam, Jade, Lime, Cocoa)

    /** An accent for the [index]th of a run of otherwise-alike things, so neighbors differ. */
    fun cycle(index: Int): Color = Cycle[index % Cycle.size]
}

// App-specific tokens Material's color scheme has no slot for. See [AppColors].

internal val MatteCardLight = Color(0xFF1B201E)
internal val MatteCardDark = Color(0xFF1B2220)
internal val NavBarBlack = Color(0xFF121715)

/**
 * This accent adjusted to read clearly as text or an icon: deepened in light theme and lifted in
 * dark, while keeping its color.
 */
@Composable
@ReadOnlyComposable
fun Color.forText(): Color =
    if (AppTheme.colors.isLight) lerp(this, Color.Black, 0.5f) else lerp(this, Color.White, 0.25f)

/** Text and icons on a solid chip of [forText]: white in light theme, near-black in dark. */
@Composable
@ReadOnlyComposable
fun onAccent(): Color = if (AppTheme.colors.isLight) Color.White else SurfaceDark
