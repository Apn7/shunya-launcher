package dev.apn7.shunya.feature.settings.permissions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.PermissionCard
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.model.PermissionStatus
import dev.apn7.shunya.feature.settings.SettingsNote

/** Where each special access is sent to be granted. */
internal class PermissionActions(
    val setDefaultLauncher: () -> Unit,
    val openUsageAccess: () -> Unit,
    val openNotificationAccess: () -> Unit,
    val openAccessibilityDisclosure: () -> Unit,
    val openGrayscaleSetup: () -> Unit,
)

/**
 * Permissions dashboard (PRD 3.5): every special access with granted / not granted, one line on
 * why Shunya asks, and a button; then the install-time permissions with their reason.
 */
@Composable
internal fun PermissionsScreen(status: PermissionStatus, actions: PermissionActions, onBack: () -> Unit) {
    ShunyaScreen(title = stringResource(R.string.settings_permissions), onBack = onBack) {
        SettingsNote(stringResource(R.string.settings_permissions_intro))
        PermissionCard(
            title = stringResource(R.string.settings_perm_launcher),
            description = stringResource(R.string.settings_perm_launcher_why),
            granted = status.isDefaultLauncher,
            onGrant = actions.setDefaultLauncher,
            actionText = stringResource(R.string.settings_perm_launcher_action),
        )
        PermissionCard(
            title = stringResource(R.string.settings_perm_usage),
            description = stringResource(R.string.settings_perm_usage_why),
            granted = status.hasUsageAccess,
            onGrant = actions.openUsageAccess,
        )
        PermissionCard(
            title = stringResource(R.string.settings_perm_notifications),
            description = stringResource(R.string.settings_perm_notifications_why),
            granted = status.hasNotificationAccess,
            onGrant = actions.openNotificationAccess,
        )
        PermissionCard(
            title = stringResource(R.string.settings_perm_accessibility),
            description = stringResource(R.string.settings_perm_accessibility_why),
            granted = status.isAccessibilityEnabled,
            onGrant = actions.openAccessibilityDisclosure,
            actionText = stringResource(R.string.settings_set_up),
        )
        PermissionCard(
            title = stringResource(R.string.settings_perm_secure),
            description = stringResource(R.string.settings_perm_secure_why),
            granted = status.canWriteSecureSettings,
            onGrant = actions.openGrayscaleSetup,
            actionText = stringResource(R.string.settings_set_up),
        )

        SectionHeader(stringResource(R.string.settings_perm_install_header))
        SettingsRow(
            title = stringResource(R.string.settings_perm_expand),
            summary = stringResource(R.string.settings_perm_expand_why),
        )
        SettingsRow(
            title = stringResource(R.string.settings_perm_delete),
            summary = stringResource(R.string.settings_perm_delete_why),
        )
        SettingsRow(
            title = stringResource(R.string.settings_perm_vibrate),
            summary = stringResource(R.string.settings_perm_vibrate_why),
        )
        SettingsRow(
            title = stringResource(R.string.settings_perm_alarm),
            summary = stringResource(R.string.settings_perm_alarm_why),
        )
        SettingsNote(stringResource(R.string.settings_privacy_statement))
    }
}
