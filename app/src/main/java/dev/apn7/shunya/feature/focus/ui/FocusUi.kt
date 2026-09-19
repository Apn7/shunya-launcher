package dev.apn7.shunya.feature.focus.ui

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import java.util.Date

/** The current time, refreshed every [periodMillis] while in composition (for "23m left" lines). */
@Composable
fun rememberNow(periodMillis: Long = 1_000L): Long {
    val now: Long by produceState(System.currentTimeMillis(), periodMillis) {
        while (true) {
            delay(periodMillis)
            value = System.currentTimeMillis()
        }
    }
    return now
}

/** Wall-clock time of [epochMillis] in the phone's 12/24-hour style, e.g. "07:00" or "7:00 AM". */
fun clockTime(context: Context, epochMillis: Long): String = DateFormat.getTimeFormat(context).format(Date(epochMillis))

/** "07:30" style text for minutes after midnight, in the phone's 12/24-hour style. */
fun minuteOfDayText(context: Context, minuteOfDay: Int): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.HOUR_OF_DAY, minuteOfDay / 60)
    calendar.set(java.util.Calendar.MINUTE, minuteOfDay % 60)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    return DateFormat.getTimeFormat(context).format(calendar.time)
}
