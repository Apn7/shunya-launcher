package dev.apn7.shunya.feature.focus.ui

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import java.util.Date
import java.util.TimeZone

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

/**
 * "07:30" / "7:30 AM" for minutes after midnight, in the phone's 12/24-hour style. Formatted in UTC on
 * the epoch day, so a daylight-saving change today can never shift the label.
 */
fun minuteOfDayText(context: Context, minuteOfDay: Int): String {
    val format = DateFormat.getTimeFormat(context)
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(Date(minuteOfDay.coerceIn(0, MINUTES_PER_DAY - 1) * 60_000L))
}

private const val MINUTES_PER_DAY = 1440
