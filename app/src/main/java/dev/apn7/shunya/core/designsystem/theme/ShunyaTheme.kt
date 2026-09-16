package dev.apn7.shunya.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import dev.apn7.shunya.core.model.AppearancePrefs

/** Current colours; prefer `ShunyaTheme.colors`. */
val LocalShunyaColors = staticCompositionLocalOf { ShunyaPalettes.Paper }

/** Current type scale; prefer `ShunyaTheme.typography`. */
val LocalShunyaTypography = staticCompositionLocalOf { shunyaTypography(ShunyaFonts.Inter, AppearancePrefs().textSize) }

/**
 * Applies the user's theme, font and text size to [content]. Also maps the tokens onto a
 * Material 3 colour scheme and typography, so any M3 widget used inside blends in.
 * `MainActivity` and the gate wrap everything in it; previews can call it with defaults.
 */
@Composable
fun ShunyaTheme(
    appearance: AppearancePrefs = AppearancePrefs(),
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val banglaUi = LocalConfiguration.current.locales.get(0)?.language == "bn"
    val colors = remember(appearance.theme, systemDark) { ShunyaPalettes.resolve(appearance.theme, systemDark) }
    val typography = remember(appearance.font, appearance.textSize, banglaUi) {
        shunyaTypography(ShunyaFonts.forChoice(appearance.font, banglaUi), appearance.textSize)
    }
    val colorScheme = remember(colors) { colors.toMaterialColorScheme() }
    val materialTypography = remember(typography) { typography.toMaterialTypography() }

    MaterialTheme(colorScheme = colorScheme, typography = materialTypography) {
        CompositionLocalProvider(
            LocalShunyaColors provides colors,
            LocalShunyaTypography provides typography,
            LocalContentColor provides colors.ink,
            content = content,
        )
    }
}

/** Accessors for the current tokens: `ShunyaTheme.colors.secondary`, `ShunyaTheme.typography.body`. */
object ShunyaTheme {
    val colors: ShunyaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalShunyaColors.current

    val typography: ShunyaTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalShunyaTypography.current
}

private fun ShunyaColors.toMaterialColorScheme(): ColorScheme {
    val subtle = ink.copy(alpha = 0.08f)
    return if (isDark) {
        darkColorScheme(
            primary = ink, onPrimary = background, primaryContainer = subtle, onPrimaryContainer = ink,
            secondary = secondary, onSecondary = background, secondaryContainer = subtle, onSecondaryContainer = ink,
            tertiary = secondary, onTertiary = background,
            background = background, onBackground = ink,
            surface = surface, onSurface = ink, surfaceVariant = subtle, onSurfaceVariant = secondary,
            surfaceTint = surface, inverseSurface = ink, inverseOnSurface = background,
            error = danger, onError = background,
            outline = tertiary, outlineVariant = divider, scrim = scrim,
            surfaceBright = surface, surfaceDim = background,
            surfaceContainerLowest = background, surfaceContainerLow = surface, surfaceContainer = surface,
            surfaceContainerHigh = surface, surfaceContainerHighest = surface,
        )
    } else {
        lightColorScheme(
            primary = ink, onPrimary = background, primaryContainer = subtle, onPrimaryContainer = ink,
            secondary = secondary, onSecondary = background, secondaryContainer = subtle, onSecondaryContainer = ink,
            tertiary = secondary, onTertiary = background,
            background = background, onBackground = ink,
            surface = surface, onSurface = ink, surfaceVariant = subtle, onSurfaceVariant = secondary,
            surfaceTint = surface, inverseSurface = ink, inverseOnSurface = background,
            error = danger, onError = background,
            outline = tertiary, outlineVariant = divider, scrim = scrim,
            surfaceBright = surface, surfaceDim = background,
            surfaceContainerLowest = background, surfaceContainerLow = surface, surfaceContainer = surface,
            surfaceContainerHigh = surface, surfaceContainerHighest = surface,
        )
    }
}

private fun ShunyaTypography.toMaterialTypography(): Typography = Typography(
    displayLarge = clockLarge,
    displayMedium = clockMedium,
    displaySmall = title,
    headlineLarge = title,
    headlineMedium = title,
    headlineSmall = title,
    titleLarge = listItem,
    titleMedium = body,
    titleSmall = label,
    bodyLarge = body,
    bodyMedium = bodySmall,
    bodySmall = caption,
    labelLarge = label,
    labelMedium = caption,
    labelSmall = caption,
)
