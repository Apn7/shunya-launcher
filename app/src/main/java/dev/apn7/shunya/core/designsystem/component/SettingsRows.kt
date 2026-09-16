package dev.apn7.shunya.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing

private val RowMinHeight = 56.dp

/** Small header above a group of rows ("Appearance", "Focus"). */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = ShunyaTheme.typography.section,
        color = ShunyaTheme.colors.tertiary,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Spacing.screenHorizontal, end = Spacing.screenHorizontal, top = Spacing.l, bottom = Spacing.xs)
            .semantics { heading() },
    )
}

/**
 * A tappable row: [title], optional [summary] below and optional current [value] on the right.
 * Without [onClick] it is plain information.
 */
@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    value: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val click = if (onClick != null) {
        Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    RowFrame(modifier.then(click)) {
        RowTexts(title, summary, enabled)
        if (value != null) {
            Text(
                text = value,
                style = ShunyaTheme.typography.bodySmall,
                color = if (enabled) ShunyaTheme.colors.secondary else ShunyaTheme.colors.tertiary,
                modifier = Modifier.padding(start = Spacing.m),
            )
        }
    }
}

/** A row with a switch; the whole row toggles. */
@Composable
fun SwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
) {
    val colors = ShunyaTheme.colors
    RowFrame(modifier.toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)) {
        RowTexts(title, summary, enabled)
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            modifier = Modifier.padding(start = Spacing.m),
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.background,
                checkedTrackColor = colors.ink,
                checkedBorderColor = colors.ink,
                uncheckedThumbColor = colors.secondary,
                uncheckedTrackColor = Color.Transparent,
                uncheckedBorderColor = colors.tertiary,
            ),
        )
    }
}

/** A row with a checkbox, for multi-select lists (distracting apps, favorites, allowed apps). */
@Composable
fun CheckRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
) {
    val colors = ShunyaTheme.colors
    RowFrame(modifier.toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onCheckedChange)) {
        RowTexts(title, summary, enabled)
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            modifier = Modifier.padding(start = Spacing.m),
            colors = CheckboxDefaults.colors(
                checkedColor = colors.ink,
                uncheckedColor = colors.tertiary,
                checkmarkColor = colors.background,
            ),
        )
    }
}

/** Shared layout of every row: full width, 56 dp minimum, screen padding. */
@Composable
internal fun RowFrame(modifier: Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RowMinHeight)
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/** Title + optional summary, taking the remaining width. */
@Composable
internal fun RowScope.RowTexts(title: String, summary: String?, enabled: Boolean) {
    val colors = ShunyaTheme.colors
    Column(Modifier.weight(1f)) {
        Text(
            text = title,
            style = ShunyaTheme.typography.body,
            color = if (enabled) colors.ink else colors.tertiary,
        )
        if (summary != null) {
            Text(
                text = summary,
                style = ShunyaTheme.typography.bodySmall,
                color = if (enabled) colors.secondary else colors.tertiary,
            )
        }
    }
}
