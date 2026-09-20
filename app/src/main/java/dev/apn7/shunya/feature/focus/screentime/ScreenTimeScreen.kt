package dev.apn7.shunya.feature.focus.screentime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.EmptyState
import dev.apn7.shunya.core.designsystem.component.PermissionCard
import dev.apn7.shunya.core.designsystem.component.ProportionBar
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.durationText
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.feature.focus.ui.Paragraph

/** Today's total and unlocks, the last 7 days, and today's apps with proportional bars. */
@Composable
internal fun ScreenTimeScreen(
    state: ScreenTimeUiState,
    onBack: () -> Unit,
    onAppClick: (String) -> Unit,
    onGrantUsage: () -> Unit,
) {
    val colors = ShunyaTheme.colors
    val resources = LocalContext.current.resources
    ShunyaScreen(title = stringResource(R.string.focus_screentime_title), onBack = onBack) {
        if (!state.hasAccess) {
            PermissionCard(
                title = stringResource(R.string.focus_usage_access_title),
                description = stringResource(R.string.focus_usage_access_description),
                granted = false,
                onGrant = onGrantUsage,
            )
            Paragraph(
                text = stringResource(R.string.focus_screentime_privacy),
                style = ShunyaTheme.typography.bodySmall,
                color = colors.tertiary,
            )
        } else if (!state.loading) {
            SectionHeader(stringResource(R.string.focus_screentime_today))
            Text(
                text = durationText(state.totalMillis),
                style = ShunyaTheme.typography.title,
                color = colors.ink,
                modifier = Modifier.padding(horizontal = Spacing.screenHorizontal),
            )
            val unlocks = state.unlockCount
            if (unlocks != null) {
                Paragraph(resources.getQuantityString(R.plurals.focus_unlocks_today, unlocks, unlocks))
            }

            SectionHeader(stringResource(R.string.focus_screentime_week))
            WeekChart(days = state.days)
            Paragraph(
                text = stringResource(R.string.focus_screentime_average, durationText(state.dailyAverageMillis)),
                style = ShunyaTheme.typography.bodySmall,
                color = colors.tertiary,
            )

            SectionHeader(stringResource(R.string.focus_screentime_by_app))
            if (state.apps.isEmpty()) {
                EmptyState(title = stringResource(R.string.focus_screentime_empty))
            } else {
                val top = state.apps.first().foregroundMillis.coerceAtLeast(1L)
                state.apps.forEach { row ->
                    UsageRow(row = row, fraction = row.foregroundMillis.toFloat() / top, onClick = { onAppClick(row.packageName) })
                }
            }
            Spacer(Modifier.height(Spacing.l))
        }
    }
}

/** App name and time, how often it was opened, and its share against the most used app. */
@Composable
private fun UsageRow(row: AppUsageRow, fraction: Float, onClick: () -> Unit) {
    val colors = ShunyaTheme.colors
    val resources = LocalContext.current.resources
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s + Spacing.xs),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = row.label, style = ShunyaTheme.typography.body, color = colors.ink, modifier = Modifier.weight(1f))
            Text(
                text = durationText(row.foregroundMillis),
                style = ShunyaTheme.typography.bodySmall,
                color = colors.secondary,
                modifier = Modifier.padding(start = Spacing.m),
            )
        }
        Text(
            text = resources.getQuantityString(R.plurals.focus_opens, row.opens, row.opens),
            style = ShunyaTheme.typography.caption,
            color = colors.tertiary,
            modifier = Modifier.padding(top = Spacing.xs, bottom = Spacing.s),
        )
        ProportionBar(fraction = fraction)
    }
}
