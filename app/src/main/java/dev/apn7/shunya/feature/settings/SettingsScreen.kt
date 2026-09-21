package dev.apn7.shunya.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.model.GestureAction
import dev.apn7.shunya.core.model.HomeGesture
import dev.apn7.shunya.core.model.LauncherSettings
import dev.apn7.shunya.core.model.NotificationFilterMode
import dev.apn7.shunya.core.model.PermissionStatus
import dev.apn7.shunya.core.navigation.Route

/** Everything the settings root shows as one-line current values. */
data class SettingsOverview(
    val settings: LauncherSettings,
    val permissions: PermissionStatus,
    val heldCount: Int,
    val hiddenCount: Int,
    val distractingCount: Int,
    val grayscaleOn: Boolean,
    val version: String,
) {
    val gesturesInUse: Int
        get() = HomeGesture.entries.count { settings.gestures.binding(it).action != GestureAction.None }

    val grantedCount: Int
        get() = listOf(
            permissions.isDefaultLauncher,
            permissions.hasUsageAccess,
            permissions.hasNotificationAccess,
            permissions.isAccessibilityEnabled,
            permissions.canWriteSecureSettings,
        ).count { it }

    companion object {
        const val PERMISSION_COUNT = 5
    }
}

/** Settings root (PRD 3.5): calm sections linking to every settings screen, with current values. */
@Composable
internal fun SettingsScreen(overview: SettingsOverview, onBack: () -> Unit, onOpen: (Route) -> Unit) {
    val resources = LocalContext.current.resources
    val settings = overview.settings
    ShunyaScreen(title = stringResource(R.string.settings_title), onBack = onBack) {
        SectionHeader(stringResource(R.string.settings_section_look))
        NavRow(
            title = stringResource(R.string.settings_appearance),
            summary = stringResource(
                R.string.settings_appearance_summary,
                themeLabel(settings.appearance.theme),
                fontLabel(settings.appearance.font),
            ),
            onClick = { onOpen(Route.Appearance) },
        )
        NavRow(
            title = stringResource(R.string.settings_home),
            summary = stringResource(R.string.settings_home_summary, settings.home.maxFavorites),
            onClick = { onOpen(Route.HomeSettings) },
        )
        NavRow(
            title = stringResource(R.string.settings_gestures),
            summary = stringResource(R.string.settings_gestures_summary, overview.gesturesInUse, HomeGesture.entries.size),
            onClick = { onOpen(Route.Gestures) },
        )
        NavRow(
            title = stringResource(R.string.settings_drawer),
            summary = stringResource(R.string.settings_drawer_summary, drawerSortLabel(settings.drawer.sort)),
            onClick = { onOpen(Route.DrawerSettings) },
        )
        NavRow(
            title = stringResource(R.string.settings_hidden_apps),
            summary = if (overview.hiddenCount == 0) {
                stringResource(R.string.settings_hidden_apps_none)
            } else {
                resources.getQuantityString(R.plurals.settings_hidden_apps_count, overview.hiddenCount, overview.hiddenCount)
            },
            onClick = { onOpen(Route.HiddenApps) },
        )

        SectionHeader(stringResource(R.string.settings_section_wellbeing))
        NavRow(
            title = stringResource(R.string.settings_focus),
            summary = if (overview.distractingCount == 0) {
                stringResource(R.string.settings_focus_summary_empty)
            } else {
                resources.getQuantityString(
                    R.plurals.settings_focus_distracting_count,
                    overview.distractingCount,
                    overview.distractingCount,
                )
            },
            onClick = { onOpen(Route.FocusHub) },
        )
        NavRow(
            title = stringResource(R.string.settings_screen_time),
            summary = stringResource(R.string.settings_screen_time_summary),
            onClick = { onOpen(Route.ScreenTime) },
        )
        NavRow(
            title = stringResource(R.string.settings_grayscale),
            summary = when {
                !overview.permissions.canWriteSecureSettings -> stringResource(R.string.settings_grayscale_setup_needed)
                overview.grayscaleOn -> stringResource(R.string.common_on)
                else -> stringResource(R.string.common_off)
            },
            onClick = { onOpen(Route.GrayscaleSetup) },
        )
        NavRow(
            title = stringResource(R.string.settings_notifications),
            summary = if (settings.notifications.filterMode == NotificationFilterMode.Hold) {
                stringResource(R.string.settings_notifications_summary_hold, overview.heldCount)
            } else {
                stringResource(R.string.settings_notifications_summary_off)
            },
            onClick = { onOpen(Route.NotificationSettings) },
        )
        NavRow(
            title = stringResource(R.string.settings_inbox),
            summary = if (settings.notifications.filterMode == NotificationFilterMode.Hold) {
                stringResource(R.string.settings_notifications_summary_hold, overview.heldCount)
            } else {
                null
            },
            onClick = { onOpen(Route.Inbox) },
        )

        SectionHeader(stringResource(R.string.settings_section_system))
        NavRow(
            title = stringResource(R.string.settings_permissions),
            summary = stringResource(R.string.settings_permissions_summary, overview.grantedCount, SettingsOverview.PERMISSION_COUNT),
            onClick = { onOpen(Route.Permissions) },
        )
        NavRow(
            title = stringResource(R.string.settings_accessibility),
            summary = if (overview.permissions.isAccessibilityEnabled) {
                stringResource(R.string.common_on)
            } else {
                stringResource(R.string.settings_accessibility_summary_off)
            },
            onClick = { onOpen(Route.AccessibilityDisclosure) },
        )
        NavRow(
            title = stringResource(R.string.settings_language),
            summary = languageLabel(settings.language),
            onClick = { onOpen(Route.Language) },
        )
        NavRow(
            title = stringResource(R.string.settings_backup),
            summary = stringResource(R.string.settings_backup_summary),
            onClick = { onOpen(Route.Backup) },
        )

        SectionHeader(stringResource(R.string.settings_section_shunya))
        NavRow(
            title = stringResource(R.string.settings_about),
            summary = stringResource(R.string.settings_version, overview.version),
            onClick = { onOpen(Route.About) },
        )
    }
}

@Composable
private fun NavRow(title: String, summary: String?, onClick: () -> Unit) {
    SettingsRow(title = title, summary = summary, onClick = onClick)
}
