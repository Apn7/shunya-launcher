package dev.apn7.shunya.feature.settings.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.feature.settings.SettingsNote

/** About: version, what শূন্য means, the privacy promise, font licences, run setup again. */
@Composable
internal fun AboutScreen(version: String, onBack: () -> Unit, onRunOnboarding: () -> Unit) {
    val colors = ShunyaTheme.colors
    ShunyaScreen(title = stringResource(R.string.settings_about), onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
        ) {
            Text(
                text = stringResource(R.string.settings_about_name_bangla),
                style = ShunyaTheme.typography.title,
                color = colors.ink,
            )
            Text(
                text = stringResource(R.string.settings_about_tagline),
                style = ShunyaTheme.typography.listItem,
                color = colors.secondary,
            )
            Text(
                text = stringResource(R.string.settings_version, version),
                style = ShunyaTheme.typography.caption,
                color = colors.tertiary,
                modifier = Modifier.padding(top = Spacing.s),
            )
        }

        SectionHeader(stringResource(R.string.settings_about_meaning_title))
        SettingsNote(stringResource(R.string.settings_about_meaning))

        SectionHeader(stringResource(R.string.settings_about_privacy_title))
        SettingsNote(stringResource(R.string.settings_privacy_statement))
        SettingsNote(stringResource(R.string.settings_about_privacy_detail))

        SectionHeader(stringResource(R.string.settings_about_fonts_title))
        SettingsNote(stringResource(R.string.settings_about_fonts))
        SettingsNote(stringResource(R.string.settings_about_font_credits))

        SettingsRow(
            title = stringResource(R.string.settings_about_onboarding),
            summary = stringResource(R.string.settings_about_onboarding_summary),
            onClick = onRunOnboarding,
        )
    }
}
