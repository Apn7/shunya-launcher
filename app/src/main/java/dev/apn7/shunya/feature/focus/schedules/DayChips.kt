package dev.apn7.shunya.feature.focus.schedules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.Schedule
import dev.apn7.shunya.feature.focus.ui.shortDayName
import java.util.Locale

private val ChipShape = RoundedCornerShape(12.dp)

/** Seven day toggles in the locale's week order; a selected day is filled with ink. */
@Composable
internal fun DayChips(
    selected: Set<Int>,
    order: List<Int>,
    locale: Locale,
    onToggle: (Int) -> Unit,
) {
    val colors = ShunyaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        order.forEach { day ->
            val isOn = day in selected
            val frame = if (isOn) {
                Modifier.background(colors.ink)
            } else {
                Modifier.border(1.dp, colors.divider, ChipShape)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = Spacing.minTouchTarget)
                    .clip(ChipShape)
                    .then(frame)
                    .toggleable(value = isOn, role = Role.Checkbox, onValueChange = { onToggle(day) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = shortDayName(day, locale),
                    style = ShunyaTheme.typography.caption,
                    color = if (isOn) colors.background else colors.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

/** One-tap day sets: every day, the Bangladesh work week, the weekend. */
@Composable
internal fun DayPresets(onPick: (Set<Int>) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.s),
    ) {
        ShunyaTextButton(
            text = stringResource(R.string.focus_days_every_day),
            onClick = { onPick(Schedule.ALL_DAYS) },
            style = ShunyaButtonStyle.Secondary,
        )
        ShunyaTextButton(
            text = daysSummary(Schedule.SUN_TO_THU),
            onClick = { onPick(Schedule.SUN_TO_THU) },
            style = ShunyaButtonStyle.Secondary,
        )
        ShunyaTextButton(
            text = daysSummary(SchedulePresets.FRI_SAT),
            onClick = { onPick(SchedulePresets.FRI_SAT) },
            style = ShunyaButtonStyle.Secondary,
        )
    }
}
