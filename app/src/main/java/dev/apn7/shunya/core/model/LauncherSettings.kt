package dev.apn7.shunya.core.model

import kotlinx.serialization.Serializable

/**
 * Every user setting (PRD 3.1, 3.2, 3.4, 3.5), persisted as JSON by `SettingsRepository`.
 *
 * Grouped by settings section so screens can observe just their part. Every property has a
 * default, so new fields can be added at any time without a migration; never rename a field.
 * Focus rules (distracting apps, limits, sessions, schedules) live in [FocusConfig] instead.
 */
@Serializable
data class LauncherSettings(
    val appearance: AppearancePrefs = AppearancePrefs(),
    val home: HomePrefs = HomePrefs(),
    val gestures: GesturePrefs = GesturePrefs(),
    val drawer: DrawerPrefs = DrawerPrefs(),
    val notifications: NotificationPrefs = NotificationPrefs(),
    val blockingMode: BlockingMode = BlockingMode.LauncherOnly,
    /** Mirrors the per-app language (the system stores the real value on Android 13+). */
    val language: AppLanguage = AppLanguage.System,
    /** False until onboarding finished or was skipped; onboarding shows on first launch only. */
    val onboardingDone: Boolean = false,
)

/** Settings > Appearance. */
@Serializable
data class AppearancePrefs(
    val theme: ThemeChoice = ThemeChoice.System,
    val font: FontChoice = FontChoice.Inter,
    val textSize: TextSizeChoice = TextSizeChoice.Medium,
    val alignment: HomeAlignment = HomeAlignment.Start,
    val clockStyle: ClockStyle = ClockStyle.Large,
    val clockFormat: ClockFormat = ClockFormat.System,
    val showClock: Boolean = true,
    val showSeconds: Boolean = false,
    val showDate: Boolean = true,
    val showBattery: Boolean = false,
    val showNextAlarm: Boolean = false,
    /** Focus time left / notifications held / screen time today (most relevant one). */
    val showStatusLine: Boolean = true,
    /** Show the system status bar on home. */
    val showStatusBar: Boolean = true,
    /** Show the system wallpaper behind home instead of the solid theme background. */
    val wallpaperMode: Boolean = false,
    /** Dim scrim over the wallpaper, percent 0..[ProductLimits.WALLPAPER_DIM_MAX]. */
    val wallpaperDimPercent: Int = ProductLimits.WALLPAPER_DIM_DEFAULT,
)

/** Settings > Home. The ordered favorites themselves are stored in [AppOverrides.favorites]. */
@Serializable
data class HomePrefs(
    /** 0..[ProductLimits.FAVORITES_MAX]. */
    val maxFavorites: Int = ProductLimits.FAVORITES_DEFAULT,
    val showIntention: Boolean = false,
    /** The user's one-liner, e.g. "Today: finish thesis slides". */
    val intention: String = "",
    /** Subtle "apps" text button on home, for people who can't or don't swipe. */
    val showAppsButton: Boolean = false,
    /** The one-time "double-tap needs the accessibility service" hint was shown. */
    val lockHintShown: Boolean = false,
)

/** Settings > App drawer. Hidden apps are stored in [AppOverrides.hidden]. */
@Serializable
data class DrawerPrefs(
    val sort: DrawerSort = DrawerSort.Alphabetical,
    val showSectionLetters: Boolean = false,
    val autoShowKeyboard: Boolean = true,
    val autoLaunchSingleMatch: Boolean = false,
    val showWorkApps: Boolean = true,
)

/** Settings > Notifications. */
@Serializable
data class NotificationPrefs(
    val filterMode: NotificationFilterMode = NotificationFilterMode.Off,
    /**
     * Packages whose notifications are never held. Null means "never customised": the filter then
     * uses its built-in defaults (dialer, SMS, clock, calendar, Shunya). Write a set to customise.
     */
    val allowedPackages: Set<String>? = null,
)
