package dev.apn7.shunya.feature.onboarding

import android.text.format.DateUtils
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.AppearancePrefs
import dev.apn7.shunya.core.model.FontChoice
import dev.apn7.shunya.core.model.ThemeChoice
import dev.apn7.shunya.feature.settings.RadioOptionRow
import dev.apn7.shunya.feature.settings.appearance.FontOptionRow
import dev.apn7.shunya.feature.settings.themeLabel

private val PREVIEW_APPS = listOf(
    R.string.onboarding_preview_app_1,
    R.string.onboarding_preview_app_2,
    R.string.onboarding_preview_app_3,
)

/** Theme and font, applied live to the whole app, with a small home-screen preview on top. */
@Composable
internal fun LookStep(appearance: AppearancePrefs, onTheme: (ThemeChoice) -> Unit, onFont: (FontChoice) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        StepHeader(
            title = stringResource(R.string.onboarding_look_title),
            body = stringResource(R.string.onboarding_look_body),
        )
        HomePreview()
        SectionHeader(stringResource(R.string.settings_theme))
        ThemeChoice.entries.forEach { theme ->
            RadioOptionRow(
                label = themeLabel(theme),
                selected = theme == appearance.theme,
                onSelect = { onTheme(theme) },
            )
        }
        SectionHeader(stringResource(R.string.settings_font))
        FontChoice.entries.forEach { font ->
            FontOptionRow(
                choice = font,
                selected = font == appearance.font,
                onSelect = { onFont(font) },
            )
        }
    }
}

/** The current time, date and three sample favorites in the live theme and font. */
@Composable
private fun HomePreview() {
    val context = LocalContext.current
    val colors = ShunyaTheme.colors
    val now = remember { System.currentTimeMillis() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s)
            .border(1.dp, colors.divider, RoundedCornerShape(16.dp))
            .padding(Spacing.l),
    ) {
        Text(
            text = DateUtils.formatDateTime(context, now, DateUtils.FORMAT_SHOW_TIME),
            style = ShunyaTheme.typography.clockMedium,
            color = colors.ink,
        )
        Text(
            text = DateUtils.formatDateTime(
                context,
                now,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_NO_YEAR,
            ),
            style = ShunyaTheme.typography.body,
            color = colors.secondary,
        )
        Spacer(modifier = Modifier.height(Spacing.m))
        PREVIEW_APPS.forEach { label ->
            Text(text = stringResource(label), style = ShunyaTheme.typography.homeApp, color = colors.ink)
        }
    }
}
