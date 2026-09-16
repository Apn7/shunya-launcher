package dev.apn7.shunya.core.navigation

/**
 * Every destination in Shunya. Each route is rendered by exactly one entry composable in its
 * owner's feature package (see `ShunyaNavHost`). Routes are plain values: navigate with
 * `navigator.navigate(Route.ScreenTime)`.
 */
sealed interface Route {

    // Home, drawer, search, app actions.

    /** Home screen. The app drawer, search and quick menu are overlays inside it. */
    data object Home : Route

    /** Settings > Home: favorites editor (add, remove, reorder, rename), max favorites, intention line. */
    data object HomeSettings : Route

    /** Settings > App drawer > Hidden apps: unhide or open hidden apps. */
    data object HiddenApps : Route

    // Focus & wellbeing.

    /** Focus & wellbeing hub: focus now, distracting apps, pause length, limits, schedules, blocking mode, grayscale. */
    data object FocusHub : Route

    data object DistractingApps : Route

    data object AppLimits : Route

    data object Schedules : Route

    /** Edit the schedule with [scheduleId], or create a new one when it is null. */
    data class ScheduleEditor(val scheduleId: String?) : Route

    data object ScreenTime : Route

    /** Screen time of one app, with hourly bars. */
    data class ScreenTimeAppDetail(val packageName: String) : Route

    /** Grayscale: the one-time ADB command, a copy button and the current state. */
    data object GrayscaleSetup : Route

    /**
     * Prominent disclosure for the accessibility service, then a button to system settings.
     * Every feature that needs the service (double-tap lock, system-wide blocking, Permissions)
     * navigates here instead of opening accessibility settings directly.
     */
    data object AccessibilityDisclosure : Route

    // Settings, notifications, onboarding.

    /** Settings root: the list of sections. */
    data object Settings : Route

    data object Appearance : Route

    data object Gestures : Route

    data object DrawerSettings : Route

    /** Notification filter mode, allowed apps, link to the inbox. */
    data object NotificationSettings : Route

    /** Apps whose notifications are never held. */
    data object AllowedNotificationApps : Route

    data object Inbox : Route

    /** Permissions dashboard. */
    data object Permissions : Route

    data object Backup : Route

    data object Language : Route

    data object About : Route

    /** First-run flow; also re-runnable from About. Pushed on top of [Home], never replaces it. */
    data object Onboarding : Route
}
