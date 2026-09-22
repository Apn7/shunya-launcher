package dev.apn7.shunya.core.model

/** Product numbers from the PRD, in one place so every screen agrees on them. */
object ProductLimits {
    /** Home favorites: hard upper bound and the default for "max favorites". */
    const val FAVORITES_MAX = 8
    const val FAVORITES_DEFAULT = 6

    /** Wallpaper dim scrim, percent. */
    const val WALLPAPER_DIM_MAX = 80
    const val WALLPAPER_DIM_DEFAULT = 40

    /** Mindful pause countdown, seconds. */
    const val PAUSE_SECONDS_MIN = 3
    const val PAUSE_SECONDS_MAX = 30
    const val PAUSE_SECONDS_DEFAULT = 10

    /** Daily-limit choices offered everywhere (action sheet, focus screens), minutes. */
    val DAILY_LIMIT_PRESETS_MINUTES: List<Int> = listOf(15, 30, 45, 60, 90, 120)

    /** "5 more minutes" on the limit screen; at most one extension per app per day. */
    const val LIMIT_EXTENSION_MINUTES = 5

    /** Hold time for "End focus" on the blocked screen. */
    const val END_FOCUS_HOLD_SECONDS = 10

    /** Durations offered by "Focus now", minutes ("until I stop" is always offered too). */
    val FOCUS_DURATIONS_DEFAULT: List<Int> = listOf(25, 45, 60, 90)

    /** Notification inbox capacity; the oldest items are dropped first. */
    const val INBOX_MAX_ITEMS = 300
}
