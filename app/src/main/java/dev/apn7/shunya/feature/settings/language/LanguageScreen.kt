package dev.apn7.shunya.feature.settings.language

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.AppLanguage
import dev.apn7.shunya.feature.settings.RadioOptionRow
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
            RadioOptionRow(
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
