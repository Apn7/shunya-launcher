package dev.apn7.shunya.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing

/**
 * A row showing the current choice; tapping opens a single-choice dialog.
 * [optionLabel] is composable so it can call `stringResource`.
 *
 * ```
 * ChoiceRow(
 *     title = themeTitle,
 *     options = ThemeChoice.entries,
 *     selected = appearance.theme,
 *     optionLabel = { themeLabel(it) },
 *     onSelect = { choice -> settings.edit { it.copy(appearance = it.appearance.copy(theme = choice)) } },
 * )
 * ```
 */
@Composable
fun <T> ChoiceRow(
    title: String,
    options: List<T>,
    selected: T,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    SettingsRow(
        title = title,
        modifier = modifier,
        summary = summary,
        value = optionLabel(selected),
        enabled = enabled,
        onClick = { open = true },
    )
    if (open) {
        ChoiceDialog(
            title = title,
            options = options,
            selected = selected,
            optionLabel = optionLabel,
            onSelect = { choice ->
                open = false
                onSelect(choice)
            },
            onDismiss = { open = false },
        )
    }
}

/** Single-choice dialog with radio rows; picking an option confirms it immediately. */
@Composable
fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    selected: T,
    optionLabel: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = ShunyaTheme.colors
    ShunyaDialog(
        onDismiss = onDismiss,
        title = title,
        dismissText = stringResource(R.string.common_cancel),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = Spacing.minTouchTarget)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(option) }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(selectedColor = colors.ink, unselectedColor = colors.tertiary),
                )
                Text(
                    text = optionLabel(option),
                    style = ShunyaTheme.typography.body,
                    color = colors.ink,
                    modifier = Modifier.padding(start = Spacing.m),
                )
            }
        }
    }
}
