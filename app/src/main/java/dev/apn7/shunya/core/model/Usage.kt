package dev.apn7.shunya.core.model

import java.time.LocalDate

/** Usage of one app over a period (today unless stated otherwise). */
data class AppUsage(
    val packageName: String,
    /** Time in the foreground, millis. */
    val foregroundMillis: Long,
    /** Times the app was opened (brought to the foreground). */
    val launchCount: Int,
) {
    companion object {
        fun none(packageName: String): AppUsage = AppUsage(packageName, foregroundMillis = 0L, launchCount = 0)
    }
}

/** Today's screen time (since local midnight). */
data class UsageSummary(
    val date: LocalDate,
    val totalMillis: Long,
    /** Device unlocks today; null when unknown (Android 8.x or no usage access). */
    val unlockCount: Int?,
    /** Per-app usage, most used first; apps with no foreground time are left out. */
    val apps: List<AppUsage>,
) {
    companion object {
        fun empty(date: LocalDate): UsageSummary = UsageSummary(date, totalMillis = 0L, unlockCount = null, apps = emptyList())
    }
}

/** Total screen time of one local day (for the 7-day chart). */
data class DayUsage(
    val date: LocalDate,
    val totalMillis: Long,
)

/** Foreground time within one hour of a day, [hour] in 0..23. */
data class HourUsage(
    val hour: Int,
    val foregroundMillis: Long,
)
