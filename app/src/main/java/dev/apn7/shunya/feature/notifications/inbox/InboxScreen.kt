package dev.apn7.shunya.feature.notifications.inbox

import android.content.Context
import android.text.format.DateUtils
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.EmptyState
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaDialog
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.HeldNotification

/** Inbox: grouped by app, newest first; tap opens, "Dismiss" removes one, "Clear all" empties it. */
@Composable
internal fun InboxScreen(
    state: InboxUiState,
    onBack: () -> Unit,
    onOpen: (HeldNotification) -> Unit,
    onDismiss: (String) -> Unit,
    onClearAll: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    ShunyaScreen(
        title = stringResource(R.string.inbox_title),
        onBack = onBack,
        scrollable = false,
        actions = {
            if (state.total > 0) {
                ShunyaTextButton(
                    text = stringResource(R.string.inbox_clear_all),
                    onClick = { confirmClear = true },
                    style = ShunyaButtonStyle.Secondary,
                )
            }
        },
    ) {
        if (state.groups.isEmpty()) {
            if (state.holdOn) {
                EmptyState(
                    title = stringResource(R.string.inbox_empty_title),
                    message = stringResource(R.string.inbox_empty_hold),
                )
            } else {
                EmptyState(
                    title = stringResource(R.string.inbox_empty_title),
                    message = stringResource(R.string.inbox_empty_off),
                    action = { ShunyaTextButton(text = stringResource(R.string.inbox_open_settings), onClick = onOpenSettings) },
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                state.groups.forEach { group ->
                    item(key = "app:" + group.packageName) {
                        SectionHeader(stringResource(R.string.inbox_group_header, group.items.first().appLabel, group.items.size))
                    }
                    items(group.items, key = { it.key }) { held ->
                        InboxItemRow(item = held, onOpen = { onOpen(held) }, onDismiss = { onDismiss(held.key) })
                    }
                }
            }
        }
    }
    if (confirmClear) {
        ClearDialog(
            total = state.total,
            onConfirm = {
                confirmClear = false
                onClearAll()
            },
            onDismiss = { confirmClear = false },
        )
    }
}

@Composable
private fun InboxItemRow(item: HeldNotification, onOpen: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val colors = ShunyaTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(start = Spacing.screenHorizontal, end = Spacing.s, top = Spacing.s, bottom = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title.ifBlank { item.appLabel },
                style = ShunyaTheme.typography.body,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.text.isNotBlank()) {
                Text(
                    text = item.text,
                    style = ShunyaTheme.typography.bodySmall,
                    color = colors.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = heldTime(context, item.postedAt),
                style = ShunyaTheme.typography.caption,
                color = colors.tertiary,
            )
        }
        ShunyaTextButton(
            text = stringResource(R.string.inbox_dismiss),
            onClick = onDismiss,
            style = ShunyaButtonStyle.Secondary,
        )
    }
}

@Composable
private fun ClearDialog(total: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val resources = LocalContext.current.resources
    ShunyaDialog(
        onDismiss = onDismiss,
        title = stringResource(R.string.inbox_clear_title),
        confirmText = stringResource(R.string.inbox_clear_all),
        onConfirm = onConfirm,
        confirmStyle = ShunyaButtonStyle.Danger,
        dismissText = stringResource(R.string.common_cancel),
    ) {
        Text(
            text = resources.getQuantityString(R.plurals.inbox_clear_message, total, total),
            style = ShunyaTheme.typography.body,
            color = ShunyaTheme.colors.secondary,
        )
    }
}

/** "10:24" today, "22 Sep, 10:24" before; follows the 12/24-hour setting and the UI language. */
private fun heldTime(context: Context, millis: Long): String {
    val flags = if (DateUtils.isToday(millis)) {
        DateUtils.FORMAT_SHOW_TIME
    } else {
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_TIME
    }
    return DateUtils.formatDateTime(context, millis, flags)
}
