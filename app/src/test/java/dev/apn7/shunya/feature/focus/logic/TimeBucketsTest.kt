package dev.apn7.shunya.feature.focus.logic

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class TimeBucketsTest {

    private val hour = 3_600_000L
    private val dhaka = ZoneId.of("Asia/Dhaka")
    private val london = ZoneId.of("Europe/London")

    @Test
    fun dayBoundariesAreLocalMidnightsOldestFirst() {
        val bounds = TimeBuckets.dayBoundaries(LocalDate.of(2026, 9, 22), 7, dhaka)
        assertEquals(8, bounds.size)
        assertEquals(TimeBuckets.startOfDay(LocalDate.of(2026, 9, 16), dhaka), bounds.first())
        assertEquals(TimeBuckets.startOfDay(LocalDate.of(2026, 9, 23), dhaka), bounds.last())
        for (i in 0 until 7) assertEquals(24 * hour, bounds[i + 1] - bounds[i])
    }

    @Test
    fun dhakaMidnightIsSixHoursBeforeUtcMidnight() {
        val utcMidnight = LocalDate.of(2026, 9, 22).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        assertEquals(utcMidnight - 6 * hour, TimeBuckets.startOfDay(LocalDate.of(2026, 9, 22), dhaka))
    }

    @Test
    fun hourBoundariesCoverTheDay() {
        val bounds = TimeBuckets.hourBoundaries(LocalDate.of(2026, 9, 22), dhaka)
        assertEquals(25, bounds.size)
        for (i in 0 until 24) assertEquals(hour, bounds[i + 1] - bounds[i])
    }

    @Test
    fun springForwardDayHasAnEmptyHourAndIs23HoursLong() {
        val bounds = TimeBuckets.hourBoundaries(LocalDate.of(2026, 3, 29), london)
        assertEquals(25, bounds.size)
        assertEquals(23 * hour, bounds.last() - bounds.first())
        assertEquals(0L, bounds[2] - bounds[1])
    }

    @Test
    fun fallBackDayHasADoubleHourAndIs25HoursLong() {
        val bounds = TimeBuckets.hourBoundaries(LocalDate.of(2026, 10, 25), london)
        assertEquals(25 * hour, bounds.last() - bounds.first())
        assertEquals(2 * hour, bounds[2] - bounds[1])
    }

    @Test
    fun zeroDaysMeansToday() {
        assertEquals(2, TimeBuckets.dayBoundaries(LocalDate.of(2026, 9, 22), 0, dhaka).size)
    }
}
