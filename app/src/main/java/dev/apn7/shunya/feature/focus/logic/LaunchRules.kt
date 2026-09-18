package dev.apn7.shunya.feature.focus.logic

/** Focus that is active right now: [endsAt] epoch millis (null = until stopped), [scheduleName] when a schedule causes it. */
data class ActiveFocus(
    val endsAt: Long?,
    val scheduleName: String? = null,
)

/** A daily-limit extension as stored: [minutes] granted on the ISO local [date] ("2026-09-22"). */
data class LimitExtension(
    val date: String,
    val minutes: Int,
)

/** Everything the launch decision needs for one app, already read from settings and usage. */
data class LaunchFacts(
    val distracting: Boolean,
    /** Mindful pause length, already within the product range. */
    val pauseSeconds: Int,
    /** Null when no session or schedule is active. */
    val focus: ActiveFocus?,
    /** Daily limit in minutes, or null for none. */
    val limitMinutes: Int?,
    /** The app's stored extension (any day), or null. */
    val extension: LimitExtension?,
    /** Today's ISO local date. */
    val today: String,
    val usedTodayMillis: Long,
    val opensToday: Int,
)

/** Outcome of [LaunchRules.decide]; mapped 1:1 onto `LaunchDecision`. */
sealed interface LaunchVerdict {
    data object Allow : LaunchVerdict

    data class Pause(val seconds: Int, val usedTodayMillis: Long, val opensToday: Int) : LaunchVerdict

    data class LimitReached(val limitMinutes: Int, val usedTodayMillis: Long, val canExtend: Boolean) : LaunchVerdict

    data class Blocked(val endsAt: Long?, val scheduleName: String?) : LaunchVerdict
}

/**
 * The launch rules of PRD 3.3, in precedence order:
 * 1. Blocked: a focus session or schedule is active and the app is distracting.
 * 2. LimitReached: today's use is at or above the daily limit plus today's extension.
 * 3. Pause: the app is distracting (mindful pause).
 * 4. Allow.
 *
 * "5 more minutes" may be taken once per app per day: an extension counts only on the day it was
 * granted, so a new day starts with the plain limit and a fresh extension.
 */
object LaunchRules {

    private const val MINUTE_MILLIS = 60_000L

    fun decide(facts: LaunchFacts): LaunchVerdict {
        val focus = facts.focus
        val limit = facts.limitMinutes
        return when {
            facts.distracting && focus != null -> LaunchVerdict.Blocked(focus.endsAt, focus.scheduleName)
            limit != null && isLimitReached(limit, facts.extension, facts.today, facts.usedTodayMillis) ->
                LaunchVerdict.LimitReached(limit, facts.usedTodayMillis, canExtend(facts.extension, facts.today))
            facts.distracting -> LaunchVerdict.Pause(facts.pauseSeconds, facts.usedTodayMillis, facts.opensToday)
            else -> LaunchVerdict.Allow
        }
    }

    /** Extra minutes granted for [today]; an extension from another day counts as none. */
    fun extensionMinutesToday(extension: LimitExtension?, today: String): Int =
        if (extension != null && extension.date == today) extension.minutes.coerceAtLeast(0) else 0

    /** True while today's one extension has not been used. */
    fun canExtend(extension: LimitExtension?, today: String): Boolean = extensionMinutesToday(extension, today) == 0

    /** True when [usedMillis] reaches the limit plus today's extension. Limits of 0 or less mean "no limit". */
    fun isLimitReached(limitMinutes: Int, extension: LimitExtension?, today: String, usedMillis: Long): Boolean {
        if (limitMinutes <= 0) return false
        val allowedMillis = (limitMinutes + extensionMinutesToday(extension, today)) * MINUTE_MILLIS
        return usedMillis >= allowedMillis
    }
}
