package dev.apn7.shunya.feature.settings.language

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.AppLanguage
import dev.apn7.shunya.feature.settings.SettingsNote
import dev.apn7.shunya.feature.settings.languageLabel

/**
 * Settings > Language: Follow system / English / বাংলা. Below Android 13 per-app languages don't
 * exist, so the choices are shown disabled with a note and a shortcut to the system setting.
 */
@Composable
internal fun LanguageScreen(
    selected: AppLanguage,
    supported: Boolean,
    onBack: () -> Unit,
    onSelect: (AppLanguage) -> Unit,
    onOpenSystemSettings: () -> Unit,
) {
    ShunyaScreen(title = stringResource(R.string.settings_language), onBack = onBack) {
        AppLanguage.entries.forEach { language ->
            LanguageOptionRow(
                label = languageLabel(language),
                selected = language == selected,
                enabled = supported,
                onSelect = { if (language != selected) onSelect(language) },
            )
        }
        if (supported) {
            SettingsNote(stringResource(R.string.settings_language_hint))
        } else {
            SettingsNote(stringResource(R.string.settings_language_old_android))
            ShunyaTextButton(
                text = stringResource(R.string.settings_language_open_system),
                onClick = onOpenSystemSettings,
                modifier = Modifier.padding(horizontal = Spacing.s),
            )
        }
    }
}

@Composable
private fun LanguageOptionRow(label: String, selected: Boolean, enabled: Boolean, onSelect: () -> Unit) {
    val colors = ShunyaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onSelect)
            .heightIn(min = Spacing.minTouchTarget)
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(selectedColor = colors.ink, unselectedColor = colors.tertiary),
        )
        Text(
            text = label,
            style = ShunyaTheme.typography.body,
            color = if (enabled) colors.ink else colors.tertiary,
            modifier = Modifier.padding(start = Spacing.m),
        )
    }
}
