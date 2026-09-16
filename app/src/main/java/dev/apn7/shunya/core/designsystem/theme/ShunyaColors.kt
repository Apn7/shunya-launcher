package dev.apn7.shunya.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import dev.apn7.shunya.core.model.ProductLimits
import dev.apn7.shunya.core.model.ThemeChoice

/**
 * "Paper & Ink" colour tokens. Monochrome by design: emphasis comes from weight and opacity,
 * never from an accent colour. [danger] is the single exception, for destructive actions only.
 * Read them in composables with `ShunyaTheme.colors`.
 */
@Immutable
data class ShunyaColors(
    /** Screen background. */
    val background: Color,
    /** Primary text and icons ("ink"). */
    val ink: Color,
    /** Secondary text: [ink] at ~60 %. */
    val secondary: Color,
    /** Hints, disabled text, section headers: [ink] at ~38 %. */
    val tertiary: Color,
    /** Hairlines and tracks: [ink] at ~12 %. */
    val divider: Color,
    /** Sheets and dialogs: a hair lighter/darker than [background]. */
    val surface: Color,
    /** Backdrop behind sheets and dialogs. */
    val scrim: Color,
    /** Destructive actions (uninstall, delete, end focus). */
    val danger: Color,
    val isDark: Boolean,
) {
    /** Alias of [ink] for readers used to Material naming. */
    val onBackground: Color get() = ink

    /**
     * Scrim drawn over the system wallpaper in wallpaper mode: the theme background at
     * [dimPercent] opacity, so text keeps the theme's contrast on any wallpaper.
     */
    fun wallpaperScrim(dimPercent: Int): Color =
        background.copy(alpha = dimPercent.coerceIn(0, ProductLimits.WALLPAPER_DIM_MAX) / 100f)
}

/** The three palettes of PRD section 2, and the mapping from [ThemeChoice]. */
object ShunyaPalettes {

    val Paper: ShunyaColors = palette(
        background = Color(0xFFF3EFE6),
        ink = Color(0xFF1B1B1B),
        surface = Color(0xFFF9F6EF),
        danger = Color(0xFFA33A2C),
        isDark = false,
    )

    val Ink: ShunyaColors = palette(
        background = Color(0xFF000000),
        ink = Color(0xFFEDEDED),
        surface = Color(0xFF111111),
        danger = Color(0xFFE5806F),
        isDark = true,
    )

    val Slate: ShunyaColors = palette(
        background = Color(0xFF15171A),
        ink = Color(0xFFE6E6E6),
        surface = Color(0xFF1F2226),
        danger = Color(0xFFE5806F),
        isDark = true,
    )

    /** [ThemeChoice.System] is Paper in light mode and Ink in dark mode. */
    fun resolve(choice: ThemeChoice, systemDark: Boolean): ShunyaColors = when (choice) {
        ThemeChoice.System -> if (systemDark) Ink else Paper
        ThemeChoice.Paper -> Paper
        ThemeChoice.Ink -> Ink
        ThemeChoice.Slate -> Slate
    }

    private fun palette(background: Color, ink: Color, surface: Color, danger: Color, isDark: Boolean) = ShunyaColors(
        background = background,
        ink = ink,
        secondary = ink.copy(alpha = 0.60f),
        tertiary = ink.copy(alpha = 0.38f),
        divider = ink.copy(alpha = 0.12f),
        surface = surface,
        scrim = Color.Black.copy(alpha = if (isDark) 0.60f else 0.32f),
        danger = danger,
        isDark = isDark,
    )
}
