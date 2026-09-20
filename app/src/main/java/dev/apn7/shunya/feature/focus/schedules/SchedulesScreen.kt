package dev.apn7.shunya.feature.focus.schedules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.EmptyState
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.model.Schedule
import dev.apn7.shunya.feature.focus.ui.Paragraph

/** The user's schedules; when there are none, the PRD's two examples can be added with one tap. */
@Composable
internal fun SchedulesScreen(
    state: SchedulesUiState,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onAddPreset: (Schedule) -> Unit,
) {
    val defaultName = stringResource(R.string.focus_schedule_default_name)
    ShunyaScreen(
        title = stringResource(R.string.focus_schedules_title),
        onBack = onBack,
        actions = { ShunyaTextButton(text = stringResource(R.string.common_add), onClick = onAdd) },
    ) {
        Paragraph(
            text = stringResource(R.string.focus_schedules_intro),
            style = ShunyaTheme.typography.bodySmall,
            color = ShunyaTheme.colors.tertiary,
        )
        if (state.schedules.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.focus_schedules_empty_title),
                message = stringResource(R.string.focus_schedules_empty_message),
            )
            SectionHeader(stringResource(R.string.focus_schedules_examples))
            val bedtimeName = stringResource(R.string.focus_schedule_preset_bedtime)
            val workName = stringResource(R.string.focus_schedule_preset_work)
            val bedtime = remember(bedtimeName) { SchedulePresets.bedtime(bedtimeName) }
            val work = remember(workName) { SchedulePresets.work(workName) }
            SettingsRow(title = bedtime.name, summary = scheduleSummary(bedtime), value = stringResource(R.string.common_add), onClick = { onAddPreset(bedtime) })
            SettingsRow(title = work.name, summary = scheduleSummary(work), value = stringResource(R.string.common_add), onClick = { onAddPreset(work) })
        } else {
            state.schedules.forEach { schedule ->
                val value = when {
                    schedule.id in state.activeIds -> stringResource(R.string.focus_schedule_active)
                    schedule.enabled -> stringResource(R.string.common_on)
                    else -> stringResource(R.string.common_off)
                }
                SettingsRow(
                    title = schedule.name.ifBlank { defaultName },
                    summary = scheduleSummary(schedule),
                    value = value,
                    onClick = { onEdit(schedule.id) },
                )
            }
        }
    }
}
