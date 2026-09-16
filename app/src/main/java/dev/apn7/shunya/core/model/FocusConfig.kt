package dev.apn7.shunya.core.model

import kotlinx.serialization.Serializable

/**
 * Focus & wellbeing rules (PRD 3.3), persisted by `FocusConfigRepository`. Owned by the focus feature;
 * other features read it (e.g. the drawer's "Mark as distracting" writes [distractingPackages]).
 * Apps are referenced by package name because usage statistics are per package.
 */
@Serializable
data class FocusConfig(
    /** Apps that get the mindful pause and are blocked during focus sessions and schedules. */
    val distractingPackages: Set<String> = emptySet(),
    /** Mindful pause countdown in seconds ([ProductLimits.PAUSE_SECONDS_MIN]..[ProductLimits.PAUSE_SECONDS_MAX]). */
    val defaultPauseSeconds: Int = ProductLimits.PAUSE_SECONDS_DEFAULT,
    /** Optional per-app pause length in seconds; apps not listed use [defaultPauseSeconds]. */
    val pauseSecondsByPackage: Map<String, Int> = emptyMap(),
    /** Daily limits: package to minutes per day. */
    val dailyLimitMinutes: Map<String, Int> = emptyMap(),
    /** "5 more minutes" granted today. */
    val extensions: LimitExtensions = LimitExtensions(),
    /** The running manual focus session, or null. */
    val session: FocusSession? = null,
    /** Recurring focus windows, in the user's order. */
    val schedules: List<Schedule> = emptyList(),
    /** Durations offered by "Focus now", minutes. */
    val sessionDurationsMinutes: List<Int> = ProductLimits.FOCUS_DURATIONS_DEFAULT,
) {
    fun isDistracting(packageName: String): Boolean = packageName in distractingPackages

    fun pauseSecondsFor(packageName: String): Int = pauseSecondsByPackage[packageName] ?: defaultPauseSeconds

    fun limitMinutesFor(packageName: String): Int? = dailyLimitMinutes[packageName]
}

/**
 * Limit extensions for one local day. Entries from any other day are void, so no cleanup job
 * is needed. [date] is an ISO local date (`java.time.LocalDate.toString()`, e.g. "2026-09-22").
 */
@Serializable
data class LimitExtensions(
    val date: String = "",
    /** Extra minutes granted on [date], by package. */
    val minutesByPackage: Map<String, Int> = emptyMap(),
) {
    /** Extra minutes granted to [packageName] on [today] (0 if none). */
    fun minutesFor(packageName: String, today: String): Int =
        if (date == today) minutesByPackage[packageName] ?: 0 else 0

    /** Records [minutes] more for [packageName] on [today], discarding other days' entries. */
    fun plus(packageName: String, minutes: Int, today: String): LimitExtensions {
        val current = if (date == today) minutesByPackage else emptyMap()
        val total = (current[packageName] ?: 0) + minutes
        return LimitExtensions(date = today, minutesByPackage = current + (packageName to total))
    }
}

/** A manual focus session. Times are epoch millis; [endsAt] null means "until I stop". */
@Serializable
data class FocusSession(
    val startedAt: Long,
    val endsAt: Long? = null,
) {
    fun isActiveAt(nowMillis: Long): Boolean = nowMillis >= startedAt && (endsAt == null || nowMillis < endsAt)

    /** Millis left at [nowMillis], or null for an open-ended session. */
    fun remainingMillis(nowMillis: Long): Long? = endsAt?.let { (it - nowMillis).coerceAtLeast(0L) }
}

/**
 * A named recurring focus window, e.g. "Bedtime 23:00–07:00 every day".
 *
 * Times are minutes after local midnight (0..1439). When [endMinute] is less than or equal to
 * [startMinute] the window crosses midnight and ends on the next day (equal = 24 hours).
 * [days] are ISO days of week (1 = Monday … 7 = Sunday, `java.time.DayOfWeek.getValue()`); a
 * window belongs to the day it starts on.
 */
@Serializable
data class Schedule(
    val id: String,
    val name: String,
    val days: Set<Int> = ALL_DAYS,
    val startMinute: Int,
    val endMinute: Int,
    val enabled: Boolean = true,
    /** Turn grayscale on while this schedule is active. */
    val grayscale: Boolean = false,
) {
    companion object {
        val ALL_DAYS: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7)

        /** Sunday to Thursday: the Bangladesh work week (ISO numbers). */
        val SUN_TO_THU: Set<Int> = setOf(7, 1, 2, 3, 4)
    }
}
