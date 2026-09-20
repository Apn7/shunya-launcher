package dev.apn7.shunya.feature.focus.logic

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * A recurring focus window as the rules see it (mapped from `Schedule`).
 *
 * [startMinute] and [endMinute] are minutes after local midnight (0..1439). When [endMinute] is less
 * than or equal to [startMinute] the window crosses midnight and ends the next day (equal = 24 hours).
 * [days] are ISO days of week (1 = Monday … 7 = Sunday); a window belongs to the day it starts on.
 */
data class ScheduleWindow(
    val days: Set<Int>,
    val startMinute: Int,
    val endMinute: Int,
    val enabled: Boolean = true,
)

/** Pure schedule evaluation: no alarms, just "is it on at this local time, and until when". */
object ScheduleRules {

    const val MINUTES_PER_DAY = 1440

    /** Enough to follow back-to-back windows for a week without looping forever on always-on setups. */
    private const val MAX_CHAIN_STEPS = 8

    /** True when [window] is active at the local time [at]. */
    fun isActive(window: ScheduleWindow, at: LocalDateTime): Boolean = currentEnd(window, at) != null

    /**
     * End of the occurrence of [window] that contains [at] (exclusive), or null when [window] is not
     * active at [at]. An overnight window that started yesterday ends today; one that started today
     * ends tomorrow.
     */
    fun currentEnd(window: ScheduleWindow, at: LocalDateTime): LocalDateTime? {
        if (!window.enabled || window.days.isEmpty()) return null
        val start = window.startMinute.coerceIn(0, MINUTES_PER_DAY - 1)
        val end = window.endMinute.coerceIn(0, MINUTES_PER_DAY - 1)
        val minute = at.hour * 60 + at.minute
        val today = at.toLocalDate()
        val day = today.dayOfWeek.value
        if (end > start) {
            val inside = day in window.days && minute >= start && minute < end
            return if (inside) today.atStartOfDay().plusMinutes(end.toLong()) else null
        }
        return when {
            day in window.days && minute >= start -> today.plusDays(1).atStartOfDay().plusMinutes(end.toLong())
            previousDay(day) in window.days && minute < end -> today.atStartOfDay().plusMinutes(end.toLong())
            else -> null
        }
    }

    /**
     * When the [windows] active at [at] stop blocking, following windows that overlap or continue
     * back-to-back (Work 09:00–17:00 then Evening 17:00–21:00 ends at 21:00). Null when none is active.
     */
    fun activeUntil(windows: List<ScheduleWindow>, at: LocalDateTime): LocalDateTime? {
        var end: LocalDateTime? = null
        var probe = at
        for (step in 0 until MAX_CHAIN_STEPS) {
            val next = latestEnd(windows, probe) ?: break
            val current = end
            if (current != null && !next.isAfter(current)) break
            end = next
            probe = next
        }
        return end
    }

    /** ISO day before [isoDay] (Monday's previous day is Sunday). */
    fun previousDay(isoDay: Int): Int = if (isoDay <= 1) 7 else isoDay - 1

    private fun latestEnd(windows: List<ScheduleWindow>, at: LocalDateTime): LocalDateTime? {
        var latest: LocalDateTime? = null
        for (window in windows) {
            val end = currentEnd(window, at) ?: continue
            val current = latest
            if (current == null || end.isAfter(current)) latest = end
        }
        return latest
    }
}

/** A manual focus session as the rules see it. [endsAt] null means "until I stop". Epoch millis. */
data class SessionSpan(
    val startedAt: Long,
    val endsAt: Long?,
) {
    fun isActiveAt(nowMillis: Long): Boolean = nowMillis >= startedAt && (endsAt == null || nowMillis < endsAt)
}

/** Combines the manual session and the schedules into "is focus on, and until when". */
object FocusRules {

    /** True when the session or any enabled schedule is active at [nowMillis]. */
    fun isActive(nowMillis: Long, session: SessionSpan?, windows: List<ScheduleWindow>, zone: ZoneId): Boolean {
        if (session != null && session.isActiveAt(nowMillis)) return true
        val now = toLocal(nowMillis, zone)
        return windows.any { ScheduleRules.isActive(it, now) }
    }

    /**
     * Epoch millis when blocking ends: the later of the session end and the schedules' end, also
     * following a schedule that is running when the session stops. Null when focus is open-ended
     * (a session "until I stop") or not active at all.
     */
    fun blockingEndsAt(nowMillis: Long, session: SessionSpan?, windows: List<ScheduleWindow>, zone: ZoneId): Long? {
        val activeSession = session?.takeIf { it.isActiveAt(nowMillis) }
        if (activeSession != null && activeSession.endsAt == null) return null
        val scheduleEnd = ScheduleRules.activeUntil(windows, toLocal(nowMillis, zone))?.let { toMillis(it, zone) }
        val sessionEnd = activeSession?.endsAt ?: return scheduleEnd
        if (scheduleEnd != null && scheduleEnd >= sessionEnd) return scheduleEnd
        val afterSession = ScheduleRules.activeUntil(windows, toLocal(sessionEnd, zone))?.let { toMillis(it, zone) }
        return if (afterSession != null && afterSession > sessionEnd) afterSession else sessionEnd
    }

    fun toLocal(epochMillis: Long, zone: ZoneId): LocalDateTime =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDateTime()

    fun toMillis(local: LocalDateTime, zone: ZoneId): Long = local.atZone(zone).toInstant().toEpochMilli()
}
