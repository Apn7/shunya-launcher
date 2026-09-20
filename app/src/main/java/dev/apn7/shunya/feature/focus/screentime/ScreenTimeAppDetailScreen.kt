package dev.apn7.shunya.feature.focus.screentime

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.PermissionCard
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.SwitchRow
import dev.apn7.shunya.core.designsystem.durationText
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.feature.focus.limits.LimitSheet
import dev.apn7.shunya.feature.focus.limits.MINUTE_MILLIS
import dev.apn7.shunya.feature.focus.ui.Paragraph

/** One app: today's time and opens, the last 7 days, today by hour, and quick focus actions. */
@Composable
internal fun ScreenTimeAppDetailScreen(
    state: AppDetailUiState,
    onBack: () -> Unit,
    onDistracting: (Boolean) -> Unit,
    onSetLimit: (Int) -> Unit,
    onRemoveLimit: () -> Unit,
    onAppInfo: () -> Unit,
    onGrantUsage: () -> Unit,
) {
    val colors = ShunyaTheme.colors
    val resources = LocalContext.current.resources
    val usage = state.usage
    var limitSheetOpen: Boolean by rememberSaveable { mutableStateOf(false) }

    ShunyaScreen(title = usage.label, onBack = onBack) {
        if (!usage.hasAccess) {
            PermissionCard(
                title = stringResource(R.string.focus_usage_access_title),
                description = stringResource(R.string.focus_usage_access_description),
                granted = false,
                onGrant = onGrantUsage,
            )
        } else if (!usage.loading) {
            SectionHeader(stringResource(R.string.focus_screentime_today))
            Text(
                text = durationText(usage.todayMillis),
                style = ShunyaTheme.typography.title,
                color = colors.ink,
                modifier = Modifier.padding(horizontal = Spacing.screenHorizontal),
            )
            Paragraph(resources.getQuantityString(R.plurals.focus_opens, usage.opensToday, usage.opensToday))
            Paragraph(
                text = stringResource(R.string.focus_detail_week_total, durationText(usage.weekMillis)),
                style = ShunyaTheme.typography.bodySmall,
                color = colors.tertiary,
            )
            SectionHeader(stringResource(R.string.focus_detail_hours))
            HourlyChart(hours = usage.hours)
        }

        SectionHeader(stringResource(R.string.focus_detail_actions))
        SwitchRow(
            title = stringResource(R.string.focus_detail_distracting),
            checked = state.distracting,
            onCheckedChange = onDistracting,
            summary = stringResource(R.string.focus_detail_distracting_summary),
        )
        val limit = state.limitMinutes
        SettingsRow(
            title = stringResource(R.string.focus_detail_limit),
            value = if (limit != null) {
                stringResource(R.string.focus_limit_per_day, durationText(limit * MINUTE_MILLIS))
            } else {
                stringResource(R.string.focus_limit_none)
            },
            onClick = { limitSheetOpen = true },
        )
        SettingsRow(title = stringResource(R.string.focus_detail_app_info), onClick = onAppInfo)
        Spacer(Modifier.height(Spacing.l))
    }

    if (limitSheetOpen) {
        LimitSheet(
            appLabel = usage.label,
            currentMinutes = state.limitMinutes,
            onSelect = onSetLimit,
            onRemove = onRemoveLimit,
            onDismiss = { limitSheetOpen = false },
        )
    }
}
