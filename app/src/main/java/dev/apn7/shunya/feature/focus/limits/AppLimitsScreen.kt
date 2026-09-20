package dev.apn7.shunya.feature.focus.limits

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.EmptyState
import dev.apn7.shunya.core.designsystem.component.PermissionCard
import dev.apn7.shunya.core.designsystem.component.ProportionBar
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.durationText
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.ProductLimits
import dev.apn7.shunya.feature.focus.ui.AppSearchField
import dev.apn7.shunya.feature.focus.ui.PackageRow
import dev.apn7.shunya.feature.focus.ui.Paragraph
import dev.apn7.shunya.feature.focus.ui.matching

/**
 * Apps with a daily limit (today's use against it), "Add" opens an app picker, tapping a row opens
 * the preset sheet. The picker is an in-screen mode, closed by Back.
 */
@Composable
internal fun AppLimitsScreen(
    state: AppLimitsUiState,
    hasUsageAccess: Boolean,
    onBack: () -> Unit,
    onSetLimit: (String, Int) -> Unit,
    onRemoveLimit: (String) -> Unit,
    onGrantUsage: () -> Unit,
) {
    var picking: Boolean by rememberSaveable { mutableStateOf(false) }
    var editingPackage: String? by rememberSaveable { mutableStateOf<String?>(null) }

    BackHandler(enabled = picking) { picking = false }

    if (picking) {
        AppPicker(
            apps = state.candidates,
            onBack = { picking = false },
            onPick = { pkg ->
                picking = false
                editingPackage = pkg
            },
        )
    } else {
        LimitsList(
            state = state,
            hasUsageAccess = hasUsageAccess,
            onBack = onBack,
            onAdd = { picking = true },
            onEdit = { pkg -> editingPackage = pkg },
            onGrantUsage = onGrantUsage,
        )
    }

    val editing = editingPackage
    if (editing != null) {
        val existing = state.limits.firstOrNull { it.packageName == editing }
        val label = existing?.label ?: state.candidates.firstOrNull { it.packageName == editing }?.label ?: editing
        LimitSheet(
            appLabel = label,
            currentMinutes = existing?.limitMinutes,
            onSelect = { minutes -> onSetLimit(editing, minutes) },
            onRemove = { onRemoveLimit(editing) },
            onDismiss = { editingPackage = null },
        )
    }
}

@Composable
private fun LimitsList(
    state: AppLimitsUiState,
    hasUsageAccess: Boolean,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onGrantUsage: () -> Unit,
) {
    ShunyaScreen(
        title = stringResource(R.string.focus_limits_title),
        onBack = onBack,
        actions = { ShunyaTextButton(text = stringResource(R.string.common_add), onClick = onAdd) },
    ) {
        if (!hasUsageAccess) {
            PermissionCard(
                title = stringResource(R.string.focus_usage_access_title),
                description = stringResource(R.string.focus_usage_access_description),
                granted = false,
                onGrant = onGrantUsage,
            )
        }
        Paragraph(
            text = stringResource(R.string.focus_limits_intro, ProductLimits.LIMIT_EXTENSION_MINUTES),
            style = ShunyaTheme.typography.bodySmall,
            color = ShunyaTheme.colors.tertiary,
        )
        if (state.limits.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.focus_limits_empty_title),
                message = stringResource(R.string.focus_limits_empty_message),
                action = { ShunyaTextButton(text = stringResource(R.string.common_add), onClick = onAdd, outlined = true) },
            )
        } else {
            state.limits.forEach { row -> LimitItem(row = row, onClick = { onEdit(row.packageName) }) }
        }
    }
}

/** App name, its limit, and today's use as text and as a bar. */
@Composable
private fun LimitItem(row: LimitRow, onClick: () -> Unit) {
    val colors = ShunyaTheme.colors
    val limitMillis = row.limitMinutes * MINUTE_MILLIS
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.m),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = row.label,
                style = ShunyaTheme.typography.body,
                color = colors.ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.focus_limit_per_day, durationText(limitMillis)),
                style = ShunyaTheme.typography.bodySmall,
                color = colors.secondary,
                modifier = Modifier.padding(start = Spacing.m),
            )
        }
        Text(
            text = stringResource(R.string.focus_limit_used_today, durationText(row.usedTodayMillis)),
            style = ShunyaTheme.typography.bodySmall,
            color = colors.tertiary,
            modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.s),
        )
        ProportionBar(fraction = if (limitMillis > 0L) row.usedTodayMillis.toFloat() / limitMillis else 0f)
    }
}

@Composable
private fun AppPicker(apps: List<PackageRow>, onBack: () -> Unit, onPick: (String) -> Unit) {
    var query: String by rememberSaveable { mutableStateOf("") }
    val visible: List<PackageRow> = remember(apps, query) { apps.matching(query) }
    ShunyaScreen(title = stringResource(R.string.focus_limit_pick_title), onBack = onBack, scrollable = false) {
        AppSearchField(query = query, onQueryChange = { query = it })
        if (visible.isEmpty() && apps.isNotEmpty()) {
            EmptyState(title = stringResource(R.string.focus_no_matches))
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(visible, key = { it.packageName }) { row ->
                    SettingsRow(title = row.label, onClick = { onPick(row.packageName) })
                }
            }
        }
    }
}
