package dev.apn7.shunya.feature.focus.limits

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.SheetAction
import dev.apn7.shunya.core.designsystem.component.ShunyaBottomSheet
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.formatDuration
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing

/** Daily limit choices offered everywhere, in minutes. */
internal val LIMIT_PRESETS_MINUTES: List<Int> = listOf(15, 30, 45, 60, 90, 120)

/**
 * Bottom sheet to set, change or remove the daily limit of one app. [currentMinutes] null means
 * the app has no limit yet (no "Remove" then).
 */
@Composable
internal fun LimitSheet(
    appLabel: String,
    currentMinutes: Int?,
    onSelect: (Int) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    val resources = LocalContext.current.resources
    ShunyaBottomSheet(onDismiss = onDismiss) { close ->
        Text(
            text = stringResource(R.string.focus_limit_sheet_title, appLabel),
            style = ShunyaTheme.typography.listItem,
            color = ShunyaTheme.colors.ink,
            modifier = Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
        )
        LIMIT_PRESETS_MINUTES.forEach { minutes ->
            SheetAction(
                text = stringResource(R.string.focus_limit_per_day, formatDuration(resources, minutes * MINUTE_MILLIS)),
                summary = if (minutes == currentMinutes) stringResource(R.string.focus_limit_current) else null,
                onClick = {
                    close()
                    onSelect(minutes)
                },
            )
        }
        if (currentMinutes != null) {
            SheetAction(
                text = stringResource(R.string.focus_limit_remove),
                style = ShunyaButtonStyle.Danger,
                onClick = {
                    close()
                    onRemove()
                },
            )
        }
    }
}

internal const val MINUTE_MILLIS = 60_000L
