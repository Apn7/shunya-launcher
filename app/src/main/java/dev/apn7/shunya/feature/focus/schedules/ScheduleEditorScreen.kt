package dev.apn7.shunya.feature.focus.schedules

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaDialog
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.component.ShunyaTextField
import dev.apn7.shunya.core.designsystem.component.SwitchRow
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.feature.focus.logic.DayRanges
import dev.apn7.shunya.feature.focus.ui.Paragraph
import dev.apn7.shunya.feature.focus.ui.currentLocale
import dev.apn7.shunya.feature.focus.ui.firstDayOfWeek
import dev.apn7.shunya.feature.focus.ui.minuteOfDayText

private enum class TimeField { Start, End }

/** Name, days, start and end time, on/off, grayscale, delete. Changes are kept until "Save". */
@Composable
internal fun ScheduleEditorScreen(
    draft: ScheduleDraft,
    isExisting: Boolean,
    grayscaleAvailable: Boolean,
    onBack: () -> Unit,
    onChange: ((ScheduleDraft) -> ScheduleDraft) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onGrayscaleSetup: () -> Unit,
) {
    val context = LocalContext.current
    val locale = currentLocale()
    val colors = ShunyaTheme.colors
    val defaultName = stringResource(R.string.focus_schedule_default_name)
    var picking: TimeField? by remember { mutableStateOf<TimeField?>(null) }
    var confirmDelete: Boolean by remember { mutableStateOf(false) }

    ShunyaScreen(
        title = stringResource(if (isExisting) R.string.focus_schedule_edit else R.string.focus_schedule_new),
        onBack = onBack,
        actions = {
            ShunyaTextButton(text = stringResource(R.string.common_save), onClick = onSave, enabled = draft.canSave)
        },
    ) {
        SectionHeader(stringResource(R.string.focus_schedule_name))
        ShunyaTextField(
            value = draft.name,
            onValueChange = { name -> onChange { it.copy(name = name) } },
            placeholder = defaultName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.screenHorizontal),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
        )

        SectionHeader(stringResource(R.string.focus_schedule_days))
        DayChips(
            selected = draft.days,
            order = DayRanges.weekOrder(firstDayOfWeek(locale)),
            locale = locale,
            onToggle = { day -> onChange { it.copy(days = if (day in it.days) it.days - day else it.days + day) } },
        )
        DayPresets(onPick = { days -> onChange { it.copy(days = days) } })
        if (!draft.canSave) {
            Paragraph(text = stringResource(R.string.focus_schedule_days_needed), color = colors.ink)
        }

        SectionHeader(stringResource(R.string.focus_schedule_time))
        SettingsRow(
            title = stringResource(R.string.focus_schedule_starts),
            value = minuteOfDayText(context, draft.startMinute),
            onClick = { picking = TimeField.Start },
        )
        val endText = minuteOfDayText(context, draft.endMinute)
        SettingsRow(
            title = stringResource(R.string.focus_schedule_ends),
            value = if (draft.crossesMidnight) stringResource(R.string.focus_schedule_next_day, endText) else endText,
            onClick = { picking = TimeField.End },
        )

        SectionHeader(stringResource(R.string.focus_schedule_options))
        SwitchRow(
            title = stringResource(R.string.focus_schedule_enabled),
            checked = draft.enabled,
            onCheckedChange = { on -> onChange { it.copy(enabled = on) } },
        )
        SwitchRow(
            title = stringResource(R.string.focus_schedule_grayscale),
            checked = draft.grayscale,
            onCheckedChange = { on -> onChange { it.copy(grayscale = on) } },
            summary = stringResource(
                if (grayscaleAvailable) R.string.focus_schedule_grayscale_summary else R.string.focus_schedule_grayscale_setup,
            ),
        )
        if (draft.grayscale && !grayscaleAvailable) {
            SettingsRow(title = stringResource(R.string.focus_schedule_grayscale_link), onClick = onGrayscaleSetup)
        }
        if (isExisting) {
            ShunyaTextButton(
                text = stringResource(R.string.focus_schedule_delete),
                onClick = { confirmDelete = true },
                style = ShunyaButtonStyle.Danger,
                modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.l),
            )
        }
    }

    val field = picking
    if (field != null) {
        TimePickerDialog(
            title = stringResource(if (field == TimeField.Start) R.string.focus_schedule_pick_start else R.string.focus_schedule_pick_end),
            initialMinute = if (field == TimeField.Start) draft.startMinute else draft.endMinute,
            onConfirm = { minute ->
                picking = null
                onChange { if (field == TimeField.Start) it.copy(startMinute = minute) else it.copy(endMinute = minute) }
            },
            onDismiss = { picking = null },
        )
    }
    if (confirmDelete) {
        ShunyaDialog(
            onDismiss = { confirmDelete = false },
            title = stringResource(R.string.focus_schedule_delete_title),
            confirmText = stringResource(R.string.common_delete),
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            confirmStyle = ShunyaButtonStyle.Danger,
            dismissText = stringResource(R.string.common_cancel),
        ) {
            Text(
                text = stringResource(R.string.focus_schedule_delete_message, draft.name.ifBlank { defaultName }),
                style = ShunyaTheme.typography.body,
                color = colors.secondary,
            )
        }
    }
}
