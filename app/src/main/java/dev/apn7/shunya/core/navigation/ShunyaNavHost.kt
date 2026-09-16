package dev.apn7.shunya.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.apn7.shunya.core.designsystem.theme.Motion
import dev.apn7.shunya.feature.focus.accessibility.AccessibilityDisclosureEntry
import dev.apn7.shunya.feature.focus.distracting.DistractingAppsEntry
import dev.apn7.shunya.feature.focus.grayscale.GrayscaleSetupEntry
import dev.apn7.shunya.feature.focus.hub.FocusHubEntry
import dev.apn7.shunya.feature.focus.limits.AppLimitsEntry
import dev.apn7.shunya.feature.focus.schedules.ScheduleEditorEntry
import dev.apn7.shunya.feature.focus.schedules.SchedulesEntry
import dev.apn7.shunya.feature.focus.screentime.ScreenTimeAppDetailEntry
import dev.apn7.shunya.feature.focus.screentime.ScreenTimeEntry
import dev.apn7.shunya.feature.home.HomeEntry
import dev.apn7.shunya.feature.home.favorites.HomeSettingsEntry
import dev.apn7.shunya.feature.home.hidden.HiddenAppsEntry
import dev.apn7.shunya.feature.notifications.NotificationSettingsEntry
import dev.apn7.shunya.feature.notifications.allowed.AllowedNotificationAppsEntry
import dev.apn7.shunya.feature.notifications.inbox.InboxEntry
import dev.apn7.shunya.feature.onboarding.OnboardingEntry
import dev.apn7.shunya.feature.settings.SettingsEntry
import dev.apn7.shunya.feature.settings.about.AboutEntry
import dev.apn7.shunya.feature.settings.appearance.AppearanceEntry
import dev.apn7.shunya.feature.settings.backup.BackupEntry
import dev.apn7.shunya.feature.settings.drawer.DrawerSettingsEntry
import dev.apn7.shunya.feature.settings.gestures.GesturesEntry
import dev.apn7.shunya.feature.settings.language.LanguageEntry
import dev.apn7.shunya.feature.settings.permissions.PermissionsEntry

/**
 * Renders the top of [navigator]'s back stack with a short crossfade. Back pops one screen;
 * on [Route.Home] Back is left to the home screen (and finally swallowed by `MainActivity`).
 * Each screen gets its own ViewModel store and saved-state scope.
 */
@Composable
fun ShunyaNavHost(navigator: Navigator, modifier: Modifier = Modifier) {
    BackHandler(enabled = navigator.canGoBack) { navigator.back() }
    val savedState = rememberSaveableStateHolder()
    Crossfade(
        targetState = navigator.currentEntry,
        modifier = modifier,
        animationSpec = tween(Motion.MEDIUM_MILLIS),
        label = "route",
    ) { entry ->
        savedState.SaveableStateProvider(entry.id) {
            CompositionLocalProvider(LocalViewModelStoreOwner provides entry) {
                RouteContent(entry.route, navigator)
            }
        }
        DisposableEffect(entry) {
            onDispose {
                if (!navigator.isInBackStack(entry)) {
                    entry.clear()
                    savedState.removeState(entry.id)
                }
            }
        }
    }
}

/** The single place that maps a route to its owner's entry composable. */
@Composable
private fun RouteContent(route: Route, navigator: Navigator) {
    when (route) {
        Route.Home -> HomeEntry(navigator)
        Route.HomeSettings -> HomeSettingsEntry(navigator)
        Route.HiddenApps -> HiddenAppsEntry(navigator)

        Route.FocusHub -> FocusHubEntry(navigator)
        Route.DistractingApps -> DistractingAppsEntry(navigator)
        Route.AppLimits -> AppLimitsEntry(navigator)
        Route.Schedules -> SchedulesEntry(navigator)
        is Route.ScheduleEditor -> ScheduleEditorEntry(navigator, route.scheduleId)
        Route.ScreenTime -> ScreenTimeEntry(navigator)
        is Route.ScreenTimeAppDetail -> ScreenTimeAppDetailEntry(navigator, route.packageName)
        Route.GrayscaleSetup -> GrayscaleSetupEntry(navigator)
        Route.AccessibilityDisclosure -> AccessibilityDisclosureEntry(navigator)

        Route.Settings -> SettingsEntry(navigator)
        Route.Appearance -> AppearanceEntry(navigator)
        Route.Gestures -> GesturesEntry(navigator)
        Route.DrawerSettings -> DrawerSettingsEntry(navigator)
        Route.NotificationSettings -> NotificationSettingsEntry(navigator)
        Route.AllowedNotificationApps -> AllowedNotificationAppsEntry(navigator)
        Route.Inbox -> InboxEntry(navigator)
        Route.Permissions -> PermissionsEntry(navigator)
        Route.Backup -> BackupEntry(navigator)
        Route.Language -> LanguageEntry(navigator)
        Route.About -> AboutEntry(navigator)
        Route.Onboarding -> OnboardingEntry(navigator)
    }
}
