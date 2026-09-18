package dev.apn7.shunya.feature.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.durationText
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.AppearancePrefs
import dev.apn7.shunya.core.model.ClockStyle
import dev.apn7.shunya.core.model.HomeAlignment
import dev.apn7.shunya.feature.home.logic.HomeStatus
import dev.apn7.shunya.feature.home.system.BatteryState

/** Column alignment for the home alignment setting. */
internal fun HomeAlignment.horizontal(): Alignment.Horizontal = when (this) {
    HomeAlignment.Start -> Alignment.Start
    HomeAlignment.Center -> Alignment.CenterHorizontally
    HomeAlignment.End -> Alignment.End
}

/** Text alignment for the home alignment setting. */
internal fun HomeAlignment.textAlign(): TextAlign = when (this) {
    HomeAlignment.Start -> TextAlign.Start
    HomeAlignment.Center -> TextAlign.Center
    HomeAlignment.End -> TextAlign.End
}

/** Clickable without ripple (home is calm) but with a role and a TalkBack label. */
@Composable
internal fun Modifier.quietClickable(label: String, onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClickLabel = label,
        role = Role.Button,
        onClick = onClick,
    )
}

/** Big light clock (tap: alarms) and the date line (tap: calendar). */
@Composable
internal fun ClockBlock(
    now: Long,
    formatter: ClockFormatter,
    appearance: AppearancePrefs,
    onClockClick: () -> Unit,
    onDateClick: () -> Unit,
) {
    val colors = ShunyaTheme.colors
    val typography = ShunyaTheme.typography
    if (appearance.showClock) {
        val clockStyle = if (appearance.clockStyle == ClockStyle.Large) typography.clockLarge else typography.clockMedium
        Row(modifier = Modifier.quietClickable(stringResource(R.string.home_open_alarms), onClockClick)) {
            Text(
                text = formatter.time(now),
                style = clockStyle,
                color = colors.ink,
                maxLines = 1,
                modifier = Modifier.alignByBaseline(),
            )
            val marker = formatter.amPm(now)
            if (marker != null) {
                Text(
                    text = marker,
                    style = typography.label,
                    color = colors.secondary,
                    modifier = Modifier
                        .alignByBaseline()
                        .padding(start = Spacing.s),
                )
            }
        }
    }
    if (appearance.showDate) {
        Text(
            text = formatter.date(now),
            style = typography.body,
            color = colors.secondary,
            textAlign = appearance.alignment.textAlign(),
            modifier = Modifier
                .quietClickable(stringResource(R.string.home_open_calendar), onDateClick)
                .padding(vertical = Spacing.xs),
        )
    }
}

/** Battery, next alarm and the status line: small secondary lines under the date. */
@Composable
internal fun InfoLines(
    battery: BatteryState?,
    nextAlarm: String?,
    status: HomeStatus?,
    alignment: HomeAlignment,
) {
    val lines = ArrayList<String>(3)
    if (battery != null) {
        val id = if (battery.isCharging) R.string.home_battery_charging else R.string.home_battery
        lines.add(stringResource(id, battery.percent))
    }
    if (nextAlarm != null) lines.add(stringResource(R.string.home_next_alarm, nextAlarm))
    if (status != null) lines.add(statusText(status))
    if (lines.isEmpty()) return
    Column(horizontalAlignment = alignment.horizontal(), modifier = Modifier.padding(top = Spacing.s)) {
        lines.forEach { line ->
            Text(
                text = line,
                style = ShunyaTheme.typography.bodySmall,
                color = ShunyaTheme.colors.secondary,
                textAlign = alignment.textAlign(),
            )
        }
    }
}

/** The intention line: the user's one-liner, or a faint hint to write one. Tap to edit. */
@Composable
internal fun IntentionLine(intention: String, alignment: HomeAlignment, onClick: () -> Unit) {
    val blank = intention.isBlank()
    Text(
        text = if (blank) stringResource(R.string.home_intention_hint) else intention,
        style = ShunyaTheme.typography.body,
        color = if (blank) ShunyaTheme.colors.tertiary else ShunyaTheme.colors.ink,
        textAlign = alignment.textAlign(),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .padding(top = Spacing.m)
            .quietClickable(stringResource(R.string.home_edit_intention), onClick),
    )
}

@Composable
private fun statusText(status: HomeStatus): String = when (status) {
    is HomeStatus.Focus -> {
        val remaining = status.remainingMillis
        if (remaining == null) {
            stringResource(R.string.home_status_focus_on)
        } else {
            stringResource(R.string.home_status_focus_left, durationText(remaining))
        }
    }
    is HomeStatus.HeldNotifications ->
        LocalContext.current.resources.getQuantityString(R.plurals.home_status_held, status.count, status.count)
    is HomeStatus.ScreenTime -> stringResource(R.string.home_status_screen_time, durationText(status.millis))
}
