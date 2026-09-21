package dev.apn7.shunya.feature.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ChoiceRow
import dev.apn7.shunya.core.designsystem.component.PermissionCard
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.model.NotificationFilterMode
import dev.apn7.shunya.feature.settings.SettingsNote

@Composable
internal fun filterModeLabel(mode: NotificationFilterMode): String = stringResource(
    when (mode) {
        NotificationFilterMode.Off -> R.string.notifications_mode_off
        NotificationFilterMode.Hold -> R.string.notifications_mode_hold
    },
)

/** Settings > Notifications: an honest disclosure, the mode, access status, allowed apps and inbox. */
@Composable
internal fun NotificationSettingsScreen(
    state: NotificationSettingsUiState,
    onBack: () -> Unit,
    onModeChange: (NotificationFilterMode) -> Unit,
    onGrantAccess: () -> Unit,
    onOpenAllowedApps: () -> Unit,
    onOpenInbox: () -> Unit,
) {
    val resources = LocalContext.current.resources
    val hold = state.prefs.filterMode == NotificationFilterMode.Hold
    ShunyaScreen(title = stringResource(R.string.notifications_title), onBack = onBack) {
        SettingsNote(stringResource(R.string.notifications_disclosure))
        ChoiceRow(
            title = stringResource(R.string.notifications_mode),
            options = NotificationFilterMode.entries,
            selected = state.prefs.filterMode,
            optionLabel = { filterModeLabel(it) },
            onSelect = onModeChange,
            summary = stringResource(
                if (hold) R.string.notifications_mode_hold_summary else R.string.notifications_mode_off_summary,
            ),
        )
        if (hold && !state.hasAccess) {
            SettingsNote(stringResource(R.string.notifications_access_missing))
        }
        PermissionCard(
            title = stringResource(R.string.notifications_access_title),
            description = stringResource(R.string.notifications_access_why),
            granted = state.hasAccess,
            onGrant = onGrantAccess,
        )
        val allowed = state.prefs.allowedPackages
        SettingsRow(
            title = stringResource(R.string.notifications_allowed),
            summary = if (allowed == null) {
                stringResource(R.string.notifications_allowed_defaults_summary)
            } else {
                resources.getQuantityString(R.plurals.notifications_allowed_count, allowed.size, allowed.size)
            },
            onClick = onOpenAllowedApps,
        )
        SettingsRow(
            title = stringResource(R.string.notifications_inbox),
            summary = if (state.heldCount == 0) {
                stringResource(R.string.notifications_inbox_empty_summary)
            } else {
                resources.getQuantityString(R.plurals.notifications_inbox_count, state.heldCount, state.heldCount)
            },
            onClick = onOpenInbox,
        )
    }
}
