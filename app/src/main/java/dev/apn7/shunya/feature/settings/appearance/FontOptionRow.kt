package dev.apn7.shunya.feature.settings.appearance

import androidx.compose.foundation.layout.Column
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
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.FontChoice
import dev.apn7.shunya.feature.settings.fontLabel
import dev.apn7.shunya.feature.settings.previewFontFamily

/**
 * One selectable typeface, its name and a sample line rendered in that very font (Appearance and
 * onboarding). The only place that sets a font family directly: previews must show the real font.
 */
@Composable
internal fun FontOptionRow(choice: FontChoice, selected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    val colors = ShunyaTheme.colors
    val family = previewFontFamily(choice)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .heightIn(min = Spacing.minTouchTarget)
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = colors.ink, unselectedColor = colors.tertiary),
        )
        Column(modifier = Modifier.padding(start = Spacing.m)) {
            Text(
                text = fontLabel(choice),
                style = ShunyaTheme.typography.body.copy(fontFamily = family),
                color = colors.ink,
            )
            Text(
                text = stringResource(R.string.settings_font_preview),
                style = ShunyaTheme.typography.bodySmall.copy(fontFamily = family),
                color = colors.secondary,
            )
        }
    }
}
