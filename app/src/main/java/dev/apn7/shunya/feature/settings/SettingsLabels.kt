package dev.apn7.shunya.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.theme.ShunyaFonts
import dev.apn7.shunya.core.model.AppLanguage
import dev.apn7.shunya.core.model.ClockFormat
import dev.apn7.shunya.core.model.ClockStyle
import dev.apn7.shunya.core.model.DrawerSort
import dev.apn7.shunya.core.model.FontChoice
import dev.apn7.shunya.core.model.GestureAction
import dev.apn7.shunya.core.model.HomeAlignment
import dev.apn7.shunya.core.model.HomeGesture
import dev.apn7.shunya.core.model.TextSizeChoice
import dev.apn7.shunya.core.model.ThemeChoice

// User-facing labels of every settings choice (shared by Settings and onboarding).

@Composable
fun themeLabel(choice: ThemeChoice): String = stringResource(
    when (choice) {
        ThemeChoice.System -> R.string.settings_theme_system
        ThemeChoice.Paper -> R.string.settings_theme_paper
        ThemeChoice.Ink -> R.string.settings_theme_ink
        ThemeChoice.Slate -> R.string.settings_theme_slate
    },
)

@Composable
fun fontLabel(choice: FontChoice): String = stringResource(
    when (choice) {
        FontChoice.Inter -> R.string.settings_font_inter
        FontChoice.SpaceGrotesk -> R.string.settings_font_space_grotesk
        FontChoice.IbmPlexMono -> R.string.settings_font_ibm_plex_mono
        FontChoice.Lora -> R.string.settings_font_lora
        FontChoice.HindSiliguri -> R.string.settings_font_hind_siliguri
        FontChoice.System -> R.string.settings_font_system
    },
)

@Composable
fun textSizeLabel(choice: TextSizeChoice): String = stringResource(
    when (choice) {
        TextSizeChoice.Small -> R.string.settings_text_size_small
        TextSizeChoice.Medium -> R.string.settings_text_size_medium
        TextSizeChoice.Large -> R.string.settings_text_size_large
        TextSizeChoice.ExtraLarge -> R.string.settings_text_size_extra_large
    },
)

@Composable
fun alignmentLabel(choice: HomeAlignment): String = stringResource(
    when (choice) {
        HomeAlignment.Start -> R.string.settings_alignment_start
        HomeAlignment.Center -> R.string.settings_alignment_center
        HomeAlignment.End -> R.string.settings_alignment_end
    },
)

@Composable
fun clockStyleLabel(choice: ClockStyle): String = stringResource(
    when (choice) {
        ClockStyle.Large -> R.string.settings_clock_large
        ClockStyle.Medium -> R.string.settings_clock_medium
    },
)

@Composable
fun clockFormatLabel(choice: ClockFormat): String = stringResource(
    when (choice) {
        ClockFormat.System -> R.string.settings_clock_format_system
        ClockFormat.TwelveHour -> R.string.settings_clock_format_12
        ClockFormat.TwentyFourHour -> R.string.settings_clock_format_24
    },
)

@Composable
fun drawerSortLabel(choice: DrawerSort): String = stringResource(
    when (choice) {
        DrawerSort.Alphabetical -> R.string.settings_sort_alphabetical
        DrawerSort.MostUsed -> R.string.settings_sort_most_used
    },
)

@Composable
fun gestureLabel(gesture: HomeGesture): String = stringResource(
    when (gesture) {
        HomeGesture.SwipeDown -> R.string.settings_gesture_swipe_down
        HomeGesture.SwipeLeft -> R.string.settings_gesture_swipe_left
        HomeGesture.SwipeRight -> R.string.settings_gesture_swipe_right
        HomeGesture.DoubleTap -> R.string.settings_gesture_double_tap
    },
)

@Composable
fun gestureActionLabel(action: GestureAction): String = stringResource(
    when (action) {
        GestureAction.None -> R.string.settings_action_none
        GestureAction.NotificationShade -> R.string.settings_action_notification_shade
        GestureAction.Search -> R.string.settings_action_search
        GestureAction.OpenApp -> R.string.settings_action_open_app
        GestureAction.ScreenTime -> R.string.settings_action_screen_time
        GestureAction.FocusToggle -> R.string.settings_action_focus_toggle
        GestureAction.LockScreen -> R.string.settings_action_lock_screen
    },
)

@Composable
fun languageLabel(language: AppLanguage): String = stringResource(
    when (language) {
        AppLanguage.System -> R.string.settings_language_system
        AppLanguage.English -> R.string.settings_language_english
        AppLanguage.Bangla -> R.string.settings_language_bangla
    },
)

/**
 * The real typeface of [choice], for font previews only (everything else takes its style from
 * `ShunyaTheme.typography`, which also applies the Bangla fallback).
 */
fun previewFontFamily(choice: FontChoice): FontFamily = when (choice) {
    FontChoice.Inter -> ShunyaFonts.Inter
    FontChoice.SpaceGrotesk -> ShunyaFonts.SpaceGrotesk
    FontChoice.IbmPlexMono -> ShunyaFonts.IbmPlexMono
    FontChoice.Lora -> ShunyaFonts.Lora
    FontChoice.HindSiliguri -> ShunyaFonts.HindSiliguri
    FontChoice.System -> FontFamily.Default
}

/** True for fonts without Bengali glyphs (the theme swaps in Hind Siliguri for a Bangla UI). */
fun isLatinOnly(choice: FontChoice): Boolean = choice != FontChoice.HindSiliguri && choice != FontChoice.System
