package dev.apn7.shunya.feature.focus.schedules

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.model.Schedule
import dev.apn7.shunya.feature.focus.logic.DayRanges
import dev.apn7.shunya.feature.focus.ui.currentLocale
import dev.apn7.shunya.feature.focus.ui.firstDayOfWeek
import dev.apn7.shunya.feature.focus.ui.minuteOfDayText
import dev.apn7.shunya.feature.focus.ui.shortDayName
import java.util.Locale
import java.util.UUID

/** "Every day", "Sun–Thu", "Fri, Sat", "Mon, Wed, Fri". */
@Composable
internal fun daysSummary(days: Set<Int>): String {
    val locale = currentLocale()
    val runs = DayRanges.runs(days, firstDayOfWeek(locale))
    if (runs.isEmpty()) return stringResource(R.string.focus_days_none)
    if (runs.size == 1 && runs[0].length == DayRanges.DAYS_PER_WEEK) return stringResource(R.string.focus_days_every_day)
    return runs.joinToString(", ") { run ->
        when (run.length) {
            1 -> shortDayName(run.first, locale)
            2 -> shortDayName(run.first, locale) + ", " + shortDayName(run.last, locale)
            else -> shortDayName(run.first, locale) + "–" + shortDayName(run.last, locale)
        }
    }
}

/** "23:00–07:00" in the phone's clock style. */
@Composable
internal fun timeRangeText(startMinute: Int, endMinute: Int): String {
    val context = LocalContext.current
    return stringResource(R.string.focus_time_range, minuteOfDayText(context, startMinute), minuteOfDayText(context, endMinute))
}

/** "23:00–07:00 · Every day". */
@Composable
internal fun scheduleSummary(schedule: Schedule): String =
    stringResource(R.string.focus_schedule_summary, timeRangeText(schedule.startMinute, schedule.endMinute), daysSummary(schedule.days))

/** Ready-made schedules from the PRD, offered when the list is empty. */
internal object SchedulePresets {

    /** Bedtime 23:00–07:00 every day. */
    fun bedtime(name: String): Schedule =
        Schedule(id = newId(), name = name, days = Schedule.ALL_DAYS, startMinute = 23 * 60, endMinute = 7 * 60)

    /** Work 09:00–17:00 Sunday to Thursday (Bangladesh work week). */
    fun work(name: String): Schedule =
        Schedule(id = newId(), name = name, days = Schedule.SUN_TO_THU, startMinute = 9 * 60, endMinute = 17 * 60)

    /** Weekend days in Bangladesh: Friday and Saturday. */
    val FRI_SAT: Set<Int> = setOf(5, 6)

    fun newId(): String = UUID.randomUUID().toString()
}
