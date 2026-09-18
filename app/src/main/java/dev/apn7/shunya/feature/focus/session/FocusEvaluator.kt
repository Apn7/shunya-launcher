package dev.apn7.shunya.feature.focus.session

import dev.apn7.shunya.core.model.FocusConfig
import dev.apn7.shunya.core.model.FocusSession
import dev.apn7.shunya.core.model.FocusStatus
import dev.apn7.shunya.core.model.Schedule
import dev.apn7.shunya.feature.focus.logic.FocusRules
import dev.apn7.shunya.feature.focus.logic.ScheduleRules
import dev.apn7.shunya.feature.focus.logic.ScheduleWindow
import dev.apn7.shunya.feature.focus.logic.SessionSpan
import java.time.ZoneId

/** Maps the stored [FocusConfig] onto the pure focus rules. */
internal object FocusEvaluator {

    /** What is active at [nowMillis] in [zone]. */
    fun status(config: FocusConfig, nowMillis: Long, zone: ZoneId): FocusStatus {
        val session = config.session?.takeIf { it.isActiveAt(nowMillis) }
        val local = FocusRules.toLocal(nowMillis, zone)
        val activeSchedules = config.schedules.filter { ScheduleRules.isActive(it.toWindow(), local) }
        if (session == null && activeSchedules.isEmpty()) return FocusStatus.Inactive
        val endsAt = FocusRules.blockingEndsAt(nowMillis, session?.toSpan(), config.schedules.map { it.toWindow() }, zone)
        return FocusStatus(session = session, activeSchedules = activeSchedules, endsAt = endsAt)
    }

    /** True when [packageName] is distracting and focus is active at [nowMillis]. */
    fun isBlocked(config: FocusConfig, packageName: String, nowMillis: Long, zone: ZoneId): Boolean =
        config.isDistracting(packageName) &&
            FocusRules.isActive(nowMillis, config.session?.toSpan(), config.schedules.map { it.toWindow() }, zone)
}

internal fun Schedule.toWindow(): ScheduleWindow =
    ScheduleWindow(days = days, startMinute = startMinute, endMinute = endMinute, enabled = enabled)

internal fun FocusSession.toSpan(): SessionSpan = SessionSpan(startedAt = startedAt, endsAt = endsAt)
