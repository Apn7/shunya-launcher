package dev.apn7.shunya.feature.focus.hub

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ChoiceRow
import dev.apn7.shunya.core.designsystem.component.PermissionCard
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.component.SwitchRow
import dev.apn7.shunya.core.designsystem.durationText
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.BlockingMode
import dev.apn7.shunya.core.model.FocusStatus
import dev.apn7.shunya.core.navigation.Route
import dev.apn7.shunya.feature.focus.ui.Paragraph
import dev.apn7.shunya.feature.focus.ui.clockTime
import dev.apn7.shunya.feature.focus.ui.rememberNow

/** Focus & wellbeing hub: focus now, rules, wellbeing tools and where the rules apply. */
@Composable
internal fun FocusHubScreen(
    state: FocusHubUiState,
    hasUsageAccess: Boolean,
    accessibilityOn: Boolean,
    grayscaleAvailable: Boolean,
    onBack: () -> Unit,
    onStart: (Int?) -> Unit,
    onEnd: () -> Unit,
    onNavigate: (Route) -> Unit,
    onBlockingMode: (BlockingMode) -> Unit,
    onGrayscale: (Boolean) -> Unit,
    onGrantUsage: () -> Unit,
) {
    val resources = LocalContext.current.resources
    val colors = ShunyaTheme.colors
    ShunyaScreen(title = stringResource(R.string.focus_hub_title), onBack = onBack) {
        val status = state.status
        if (status.isActive) {
            ActiveFocusCard(status = status, onEnd = onEnd, onSchedules = { onNavigate(Route.Schedules) })
        }
        if (status.session == null) {
            SectionHeader(stringResource(R.string.focus_hub_now_header))
            Paragraph(
                text = stringResource(R.string.focus_hub_now_summary),
                style = ShunyaTheme.typography.bodySmall,
                color = colors.tertiary,
            )
            state.durations.forEach { minutes ->
                SettingsRow(
                    title = resources.getQuantityString(R.plurals.focus_minutes, minutes, minutes),
                    onClick = { onStart(minutes) },
                )
            }
            SettingsRow(title = stringResource(R.string.focus_hub_until_stop), onClick = { onStart(null) })
        }

        SectionHeader(stringResource(R.string.focus_hub_rules_header))
        SettingsRow(
            title = stringResource(R.string.focus_distracting_title),
            summary = stringResource(R.string.focus_hub_distracting_summary),
            value = resources.getQuantityString(R.plurals.focus_apps_count, state.distractingCount, state.distractingCount),
            onClick = { onNavigate(Route.DistractingApps) },
        )
        SettingsRow(
            title = stringResource(R.string.focus_limits_title),
            summary = stringResource(R.string.focus_hub_limits_summary),
            value = resources.getQuantityString(R.plurals.focus_apps_count, state.limitsCount, state.limitsCount),
            onClick = { onNavigate(Route.AppLimits) },
        )
        SettingsRow(
            title = stringResource(R.string.focus_schedules_title),
            summary = stringResource(R.string.focus_hub_schedules_summary),
            value = if (state.schedulesOn > 0) stringResource(R.string.focus_hub_count_on, state.schedulesOn) else null,
            onClick = { onNavigate(Route.Schedules) },
        )

        SectionHeader(stringResource(R.string.focus_hub_wellbeing_header))
        SettingsRow(
            title = stringResource(R.string.focus_screentime_title),
            summary = stringResource(R.string.focus_hub_screentime_summary),
            onClick = { onNavigate(Route.ScreenTime) },
        )
        if (grayscaleAvailable) {
            SwitchRow(
                title = stringResource(R.string.focus_grayscale_title),
                checked = state.grayscaleOn,
                onCheckedChange = onGrayscale,
                summary = stringResource(R.string.focus_grayscale_switch_summary),
            )
        } else {
            SettingsRow(
                title = stringResource(R.string.focus_grayscale_title),
                summary = stringResource(R.string.focus_grayscale_needs_setup),
                value = stringResource(R.string.focus_hub_set_up),
                onClick = { onNavigate(Route.GrayscaleSetup) },
            )
        }

        SectionHeader(stringResource(R.string.focus_hub_blocking_header))
        ChoiceRow(
            title = stringResource(R.string.focus_blocking_mode),
            options = BlockingMode.entries,
            selected = state.blockingMode,
            optionLabel = { mode -> blockingModeLabel(mode) },
            onSelect = onBlockingMode,
            summary = stringResource(R.string.focus_blocking_summary),
        )
        if (state.blockingMode == BlockingMode.SystemWide && !accessibilityOn) {
            Paragraph(text = stringResource(R.string.focus_hub_needs_service), color = colors.ink)
        }
        SettingsRow(
            title = stringResource(R.string.focus_a11y_title),
            value = stringResource(if (accessibilityOn) R.string.common_on else R.string.common_off),
            onClick = { onNavigate(Route.AccessibilityDisclosure) },
        )
        if (!hasUsageAccess) {
            PermissionCard(
                title = stringResource(R.string.focus_usage_access_title),
                description = stringResource(R.string.focus_usage_access_description),
                granted = false,
                onGrant = onGrantUsage,
                modifier = Modifier.padding(top = Spacing.m),
            )
        }
        Spacer(Modifier.height(Spacing.l))
    }
}

@Composable
internal fun blockingModeLabel(mode: BlockingMode): String = when (mode) {
    BlockingMode.LauncherOnly -> stringResource(R.string.focus_blocking_launcher_only)
    BlockingMode.SystemWide -> stringResource(R.string.focus_blocking_system_wide)
}

/** What is on right now and until when; a running session can be ended here. */
@Composable
private fun ActiveFocusCard(status: FocusStatus, onEnd: () -> Unit, onSchedules: () -> Unit) {
    val context = LocalContext.current
    val colors = ShunyaTheme.colors
    val now = rememberNow()
    val session = status.session
    val endsAt = status.endsAt
    val defaultName = stringResource(R.string.focus_schedule_default_name)
    val scheduleName = status.activeSchedules.firstOrNull()?.name.orEmpty().ifBlank { defaultName }
    val headline = when {
        session != null && endsAt != null -> stringResource(R.string.focus_hub_active_left, durationText(endsAt - now))
        session != null -> stringResource(R.string.focus_hub_active_open)
        else -> stringResource(R.string.focus_hub_schedule_on, scheduleName)
    }
    val detail = if (endsAt != null) {
        stringResource(R.string.focus_hub_active_until, clockTime(context, endsAt))
    } else {
        stringResource(R.string.focus_hub_active_until_stop)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s)
            .border(1.dp, colors.divider, RoundedCornerShape(16.dp))
            .padding(start = Spacing.m, end = Spacing.s, top = Spacing.m, bottom = Spacing.s),
    ) {
        Text(text = headline, style = ShunyaTheme.typography.listItem, color = colors.ink)
        Text(
            text = detail,
            style = ShunyaTheme.typography.bodySmall,
            color = colors.secondary,
            modifier = Modifier.padding(top = Spacing.xs),
        )
        if (session != null) {
            ShunyaTextButton(
                text = stringResource(R.string.focus_hub_end),
                onClick = onEnd,
                style = ShunyaButtonStyle.Danger,
                modifier = Modifier.align(Alignment.End),
            )
        } else {
            Text(
                text = stringResource(R.string.focus_hub_schedule_note),
                style = ShunyaTheme.typography.caption,
                color = colors.tertiary,
                modifier = Modifier.padding(top = Spacing.s),
            )
            ShunyaTextButton(
                text = stringResource(R.string.focus_schedules_title),
                onClick = onSchedules,
                style = ShunyaButtonStyle.Secondary,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}
