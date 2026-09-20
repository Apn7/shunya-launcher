package dev.apn7.shunya.feature.focus.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class ScheduleRulesTest {

    private val everyDay = setOf(1, 2, 3, 4, 5, 6, 7)
    private val sunToThu = setOf(7, 1, 2, 3, 4)
    private val work = ScheduleWindow(sunToThu, startMinute = 9 * 60, endMinute = 17 * 60)
    private val bedtime = ScheduleWindow(everyDay, startMinute = 23 * 60, endMinute = 7 * 60)
    private val zone = ZoneId.of("Asia/Dhaka")

    // 2026-09-20 is a Sunday, 2026-09-25 a Friday, 2026-09-26 a Saturday.
    private fun at(day: Int, hour: Int, minute: Int = 0) = LocalDateTime.of(2026, 9, day, hour, minute)

    private fun millis(day: Int, hour: Int, minute: Int = 0) = FocusRules.toMillis(at(day, hour, minute), zone)

    @Test
    fun sameDayWindowFollowsTheBangladeshWorkWeek() {
        assertTrue(ScheduleRules.isActive(work, at(20, 10)))
        assertTrue(ScheduleRules.isActive(work, at(24, 9)))
        assertFalse(ScheduleRules.isActive(work, at(25, 10)))
        assertFalse(ScheduleRules.isActive(work, at(26, 10)))
        assertEquals(at(20, 17), ScheduleRules.currentEnd(work, at(20, 10)))
    }

    @Test
    fun endMinuteIsExclusiveAndStartInclusive() {
        assertTrue(ScheduleRules.isActive(work, at(21, 9, 0)))
        assertTrue(ScheduleRules.isActive(work, at(21, 16, 59)))
        assertFalse(ScheduleRules.isActive(work, at(21, 17, 0)))
        assertFalse(ScheduleRules.isActive(work, at(21, 8, 59)))
    }

    @Test
    fun overnightWindowEndsTheNextMorning() {
        assertEquals(at(22, 7), ScheduleRules.currentEnd(bedtime, at(21, 23, 30)))
        assertEquals(at(22, 7), ScheduleRules.currentEnd(bedtime, at(22, 6, 59)))
        assertFalse(ScheduleRules.isActive(bedtime, at(22, 7, 0)))
        assertFalse(ScheduleRules.isActive(bedtime, at(22, 22, 59)))
    }

    @Test
    fun overnightWindowBelongsToTheDayItStarts() {
        val fridayNight = ScheduleWindow(setOf(5), startMinute = 23 * 60, endMinute = 7 * 60)
        assertTrue(ScheduleRules.isActive(fridayNight, at(25, 23, 30)))
        assertTrue(ScheduleRules.isActive(fridayNight, at(26, 6, 0)))
        assertFalse(ScheduleRules.isActive(fridayNight, at(26, 23, 30)))
        assertFalse(ScheduleRules.isActive(fridayNight, at(25, 6, 0)))
    }

    @Test
    fun sundayNightContinuesIntoMonday() {
        val sundayNight = ScheduleWindow(setOf(7), startMinute = 22 * 60, endMinute = 2 * 60)
        assertTrue(ScheduleRules.isActive(sundayNight, at(21, 1, 0)))
        assertFalse(ScheduleRules.isActive(sundayNight, at(22, 1, 0)))
        assertEquals(7, ScheduleRules.previousDay(1))
        assertEquals(6, ScheduleRules.previousDay(7))
    }

    @Test
    fun equalStartAndEndLastsTwentyFourHours() {
        val monday = ScheduleWindow(setOf(1), startMinute = 8 * 60, endMinute = 8 * 60)
        assertEquals(at(22, 8), ScheduleRules.currentEnd(monday, at(21, 8, 0)))
        assertTrue(ScheduleRules.isActive(monday, at(22, 7, 59)))
        assertFalse(ScheduleRules.isActive(monday, at(21, 7, 59)))
    }

    @Test
    fun disabledOrDaylessWindowsAreNeverActive() {
        assertFalse(ScheduleRules.isActive(work.copy(enabled = false), at(20, 10)))
        assertFalse(ScheduleRules.isActive(work.copy(days = emptySet()), at(20, 10)))
    }

    @Test
    fun backToBackAndOverlappingWindowsChain() {
        val evening = ScheduleWindow(everyDay, startMinute = 17 * 60, endMinute = 21 * 60)
        assertEquals(at(20, 21), ScheduleRules.activeUntil(listOf(work, evening), at(20, 10)))
        val late = ScheduleWindow(everyDay, startMinute = 22 * 60, endMinute = 2 * 60)
        val early = ScheduleWindow(everyDay, startMinute = 60, endMinute = 6 * 60)
        assertEquals(at(22, 6), ScheduleRules.activeUntil(listOf(late, early), at(21, 23)))
        assertNull(ScheduleRules.activeUntil(listOf(work), at(25, 10)))
    }

    @Test
    fun openEndedSessionHasNoEnd() {
        val session = SessionSpan(startedAt = millis(20, 9), endsAt = null)
        assertNull(FocusRules.blockingEndsAt(millis(20, 10), session, listOf(work), zone))
        assertTrue(FocusRules.isActive(millis(20, 10), session, emptyList(), zone))
    }

    @Test
    fun sessionOnlyEndsWithTheSession() {
        val session = SessionSpan(startedAt = millis(25, 10), endsAt = millis(25, 10, 25))
        assertEquals(millis(25, 10, 25), FocusRules.blockingEndsAt(millis(25, 10, 5), session, listOf(work), zone))
    }

    @Test
    fun laterScheduleEndWins() {
        val session = SessionSpan(startedAt = millis(20, 10), endsAt = millis(20, 10, 25))
        assertEquals(millis(20, 17), FocusRules.blockingEndsAt(millis(20, 10, 5), session, listOf(work), zone))
    }

    @Test
    fun scheduleRunningWhenTheSessionEndsExtendsIt() {
        val session = SessionSpan(startedAt = millis(21, 22, 30), endsAt = millis(21, 23, 30))
        assertEquals(millis(22, 7), FocusRules.blockingEndsAt(millis(21, 22, 45), session, listOf(bedtime), zone))
    }

    @Test
    fun expiredSessionAndNoScheduleMeansInactive() {
        val session = SessionSpan(startedAt = millis(25, 9), endsAt = millis(25, 9, 30))
        assertNull(FocusRules.blockingEndsAt(millis(25, 10), session, listOf(work), zone))
        assertFalse(FocusRules.isActive(millis(25, 10), session, listOf(work), zone))
    }
}
