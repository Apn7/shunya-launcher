package dev.apn7.shunya.feature.home.components

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import dev.apn7.shunya.core.model.ClockFormat
import dev.apn7.shunya.feature.home.logic.ClockTicks
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

/**
 * The current time, updated exactly when the minute (or second, with [withSeconds]) changes and
 * only while home is visible. Returning to home refreshes it immediately.
 */
@Composable
internal fun rememberNow(withSeconds: Boolean): Long {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val now by produceState(System.currentTimeMillis(), withSeconds, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                val current = System.currentTimeMillis()
                value = current
                val wait = if (withSeconds) {
                    ClockTicks.millisUntilNextSecond(current)
                } else {
                    ClockTicks.millisUntilNextMinute(current)
                }
                delay(wait)
            }
        }
    }
    return now
}

/**
 * Formats the home clock, date and alarm in the UI language (Bangla digits in Bangla).
 * Uses fixed, universally supported patterns plus the locale's best date pattern.
 */
internal class ClockFormatter(locale: Locale, val is24Hour: Boolean, withSeconds: Boolean) {

    private val timeFormat = SimpleDateFormat(timePattern(is24Hour, withSeconds), locale)
    private val shortTimeFormat = SimpleDateFormat(timePattern(is24Hour, withSeconds = false), locale)
    private val amPmFormat = SimpleDateFormat("a", locale)
    private val dayFormat = SimpleDateFormat("EEE", locale)
    private val dateFormat = SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, "EEEEdMMMM"), locale)

    /** "9:41" or "21:41" (with seconds when asked). */
    fun time(millis: Long): String = format(timeFormat, millis)

    /** "AM"/"PM" in a 12-hour clock, else null. */
    fun amPm(millis: Long): String? = if (is24Hour) null else format(amPmFormat, millis)

    /** "Tuesday, 22 September" (order and words follow the locale). */
    fun date(millis: Long): String = format(dateFormat, millis)

    /** "7:00 AM" when the alarm is within a day of [nowMillis], else "Wed 7:00 AM". */
    fun alarm(alarmMillis: Long, nowMillis: Long): String {
        val time = format(shortTimeFormat, alarmMillis)
        val withMarker = if (is24Hour) time else time + " " + format(amPmFormat, alarmMillis)
        return if (alarmMillis - nowMillis < DAY_MILLIS) withMarker else format(dayFormat, alarmMillis) + " " + withMarker
    }

    private fun format(format: SimpleDateFormat, millis: Long): String {
        // The zone may change while Shunya runs (travel): always format in the current one.
        format.timeZone = TimeZone.getDefault()
        return format.format(Date(millis))
    }

    private companion object {
        fun timePattern(is24Hour: Boolean, withSeconds: Boolean): String {
            val base = if (is24Hour) "HH:mm" else "h:mm"
            return if (withSeconds) "$base:ss" else base
        }
    }
}

/** A [ClockFormatter] for the current locale and the 12/24-hour choice ([ClockFormat.System] follows the phone). */
@Composable
internal fun rememberClockFormatter(format: ClockFormat, withSeconds: Boolean): ClockFormatter {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales.get(0) ?: Locale.getDefault()
    val is24Hour = when (format) {
        ClockFormat.System -> DateFormat.is24HourFormat(context)
        ClockFormat.TwelveHour -> false
        ClockFormat.TwentyFourHour -> true
    }
    return remember(locale, is24Hour, withSeconds) { ClockFormatter(locale, is24Hour, withSeconds) }
}
