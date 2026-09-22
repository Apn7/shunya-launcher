package dev.apn7.shunya.feature.settings.appearance

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ChoiceRow
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.SliderRow
import dev.apn7.shunya.core.designsystem.component.SwitchRow
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.AppearancePrefs
import dev.apn7.shunya.core.model.ClockFormat
import dev.apn7.shunya.core.model.ClockStyle
import dev.apn7.shunya.core.model.FontChoice
import dev.apn7.shunya.core.model.HomeAlignment
import dev.apn7.shunya.core.model.ProductLimits
import dev.apn7.shunya.core.model.TextSizeChoice
import dev.apn7.shunya.core.model.ThemeChoice
import dev.apn7.shunya.feature.settings.alignmentLabel
import dev.apn7.shunya.feature.settings.clockFormatLabel
import dev.apn7.shunya.feature.settings.clockStyleLabel
import dev.apn7.shunya.feature.settings.isLatinOnly
import dev.apn7.shunya.feature.settings.textSizeLabel
import dev.apn7.shunya.feature.settings.themeLabel
import kotlin.math.roundToInt

/** Wallpaper dim slider step, percent. */
private const val DIM_STEP = 5

/** Settings > Appearance (PRD 3.5): theme, font with previews, sizes, home lines, wallpaper. */
@Composable
internal fun AppearanceScreen(
    state: AppearanceUiState,
    banglaUi: Boolean,
    onBack: () -> Unit,
    onUpdate: ((AppearancePrefs) -> AppearancePrefs) -> Unit,
    onShowIntentionChange: (Boolean) -> Unit,
    onChangeWallpaper: () -> Unit,
) {
    val appearance = state.appearance
    ShunyaScreen(title = stringResource(R.string.settings_appearance), onBack = onBack) {
        SectionHeader(stringResource(R.string.settings_section_theme_type))
        ChoiceRow(
            title = stringResource(R.string.settings_theme),
            options = ThemeChoice.entries,
            selected = appearance.theme,
            optionLabel = { themeLabel(it) },
            onSelect = { choice -> onUpdate { it.copy(theme = choice) } },
        )
        ChoiceRow(
            title = stringResource(R.string.settings_text_size),
            options = TextSizeChoice.entries,
            selected = appearance.textSize,
            optionLabel = { textSizeLabel(it) },
            onSelect = { choice -> onUpdate { it.copy(textSize = choice) } },
        )
        SettingsRow(title = stringResource(R.string.settings_font))
        FontChoice.entries.forEach { choice ->
            FontOptionRow(
                choice = choice,
                selected = choice == appearance.font,
                onSelect = { onUpdate { it.copy(font = choice) } },
            )
        }
        if (banglaUi && isLatinOnly(appearance.font)) {
            Text(
                text = stringResource(R.string.settings_font_bangla_note),
                style = ShunyaTheme.typography.bodySmall,
                color = ShunyaTheme.colors.secondary,
                modifier = Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
            )
        }

        SectionHeader(stringResource(R.string.settings_section_home_screen))
        HomeLayoutRows(appearance, onUpdate)
        HomeLineRows(appearance, state.showIntention, onUpdate, onShowIntentionChange)

        SectionHeader(stringResource(R.string.settings_section_wallpaper))
        WallpaperRows(appearance, onUpdate, onChangeWallpaper)
    }
}

@Composable
private fun HomeLayoutRows(appearance: AppearancePrefs, onUpdate: ((AppearancePrefs) -> AppearancePrefs) -> Unit) {
    ChoiceRow(
        title = stringResource(R.string.settings_alignment),
        options = HomeAlignment.entries,
        selected = appearance.alignment,
        optionLabel = { alignmentLabel(it) },
        onSelect = { choice -> onUpdate { it.copy(alignment = choice) } },
    )
    SwitchRow(
        title = stringResource(R.string.settings_show_clock),
        checked = appearance.showClock,
        onCheckedChange = { on -> onUpdate { it.copy(showClock = on) } },
    )
    ChoiceRow(
        title = stringResource(R.string.settings_clock_size),
        options = ClockStyle.entries,
        selected = appearance.clockStyle,
        optionLabel = { clockStyleLabel(it) },
        onSelect = { choice -> onUpdate { it.copy(clockStyle = choice) } },
        enabled = appearance.showClock,
    )
    ChoiceRow(
        title = stringResource(R.string.settings_clock_format),
        options = ClockFormat.entries,
        selected = appearance.clockFormat,
        optionLabel = { clockFormatLabel(it) },
        onSelect = { choice -> onUpdate { it.copy(clockFormat = choice) } },
        enabled = appearance.showClock,
    )
    SwitchRow(
        title = stringResource(R.string.settings_show_seconds),
        checked = appearance.showSeconds,
        onCheckedChange = { on -> onUpdate { it.copy(showSeconds = on) } },
        enabled = appearance.showClock,
    )
}

@Composable
private fun HomeLineRows(
    appearance: AppearancePrefs,
    showIntention: Boolean,
    onUpdate: ((AppearancePrefs) -> AppearancePrefs) -> Unit,
    onShowIntentionChange: (Boolean) -> Unit,
) {
    SwitchRow(
        title = stringResource(R.string.settings_show_date),
        checked = appearance.showDate,
        onCheckedChange = { on -> onUpdate { it.copy(showDate = on) } },
    )
    SwitchRow(
        title = stringResource(R.string.settings_show_battery),
        checked = appearance.showBattery,
        onCheckedChange = { on -> onUpdate { it.copy(showBattery = on) } },
    )
    SwitchRow(
        title = stringResource(R.string.settings_show_next_alarm),
        checked = appearance.showNextAlarm,
        onCheckedChange = { on -> onUpdate { it.copy(showNextAlarm = on) } },
    )
    SwitchRow(
        title = stringResource(R.string.settings_show_status_line),
        summary = stringResource(R.string.settings_show_status_line_summary),
        checked = appearance.showStatusLine,
        onCheckedChange = { on -> onUpdate { it.copy(showStatusLine = on) } },
    )
    SwitchRow(
        title = stringResource(R.string.settings_show_intention),
        summary = stringResource(R.string.settings_show_intention_summary),
        checked = showIntention,
        onCheckedChange = onShowIntentionChange,
    )
    SwitchRow(
        title = stringResource(R.string.settings_show_status_bar),
        summary = stringResource(R.string.settings_show_status_bar_summary),
        checked = appearance.showStatusBar,
        onCheckedChange = { on -> onUpdate { it.copy(showStatusBar = on) } },
    )
}

@Composable
private fun WallpaperRows(
    appearance: AppearancePrefs,
    onUpdate: ((AppearancePrefs) -> AppearancePrefs) -> Unit,
    onChangeWallpaper: () -> Unit,
) {
    SwitchRow(
        title = stringResource(R.string.settings_wallpaper_mode),
        summary = stringResource(R.string.settings_wallpaper_mode_summary),
        checked = appearance.wallpaperMode,
        onCheckedChange = { on -> onUpdate { it.copy(wallpaperMode = on) } },
    )
    // Dragging only moves the local value; the setting is written once the finger lifts.
    var dim by remember(appearance.wallpaperDimPercent) {
        mutableFloatStateOf(appearance.wallpaperDimPercent.toFloat())
    }
    SliderRow(
        title = stringResource(R.string.settings_wallpaper_dim),
        value = dim,
        onValueChange = { dim = it },
        valueRange = 0f..ProductLimits.WALLPAPER_DIM_MAX.toFloat(),
        steps = ProductLimits.WALLPAPER_DIM_MAX / DIM_STEP - 1,
        valueLabel = stringResource(R.string.settings_percent, dim.roundToInt()),
        enabled = appearance.wallpaperMode,
        onValueChangeFinished = {
            val percent = dim.roundToInt().coerceIn(0, ProductLimits.WALLPAPER_DIM_MAX)
            onUpdate { it.copy(wallpaperDimPercent = percent) }
        },
    )
    SettingsRow(
        title = stringResource(R.string.settings_wallpaper_change),
        onClick = onChangeWallpaper,
    )
}
