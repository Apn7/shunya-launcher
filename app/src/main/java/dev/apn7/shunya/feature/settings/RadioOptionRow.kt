package dev.apn7.shunya.feature.settings

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
import androidx.compose.ui.semantics.Role
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing

/** One inline single-choice option (language list, onboarding theme picker). */
@Composable
internal fun RadioOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = ShunyaTheme.colors
    Row(
        modifier = modifier
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
