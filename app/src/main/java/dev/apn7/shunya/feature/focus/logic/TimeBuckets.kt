package dev.apn7.shunya.feature.focus.logic

import java.time.LocalDate
import java.time.ZoneId

/**
 * Local-calendar bucket boundaries in epoch millis. Days and hours follow the phone's time zone,
 * daylight-saving changes included, so "today" always starts at local midnight.
 */
object TimeBuckets {

    /** Epoch millis of the start of [date] (local midnight, or the first valid time that day). */
    fun startOfDay(date: LocalDate, zone: ZoneId): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()

    /**
     * Boundaries of the [days] local days ending with [lastDay], oldest first: `days + 1` values.
     * Bucket `i` is `[result[i], result[i + 1])` and is the day `lastDay.minusDays(days - 1 - i)`.
     */
    fun dayBoundaries(lastDay: LocalDate, days: Int, zone: ZoneId): List<Long> {
        val count = days.coerceAtLeast(1)
        val first = lastDay.minusDays((count - 1).toLong())
        return (0..count).map { offset -> startOfDay(first.plusDays(offset.toLong()), zone) }
    }

    /**
     * Boundaries of the 24 clock hours of [date]: 25 values. On a daylight-saving day a skipped hour
     * becomes an empty bucket and a repeated hour a two-hour bucket, so each bucket keeps its clock label.
     */
    fun hourBoundaries(date: LocalDate, zone: ZoneId): List<Long> {
        val hours = (0 until HOURS_PER_DAY).map { hour -> date.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli() }
        return hours + startOfDay(date.plusDays(1), zone)
    }

    const val HOURS_PER_DAY = 24
}
