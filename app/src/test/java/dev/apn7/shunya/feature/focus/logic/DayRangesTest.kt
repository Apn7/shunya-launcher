package dev.apn7.shunya.feature.focus.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DayRangesTest {

    private val sunToThu = setOf(7, 1, 2, 3, 4)

    @Test
    fun weekOrderStartsAtTheGivenDay() {
        assertEquals(listOf(7, 1, 2, 3, 4, 5, 6), DayRanges.weekOrder(7))
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), DayRanges.weekOrder(1))
        assertEquals(listOf(6, 7, 1, 2, 3, 4, 5), DayRanges.weekOrder(6))
    }

    @Test
    fun bangladeshWorkWeekIsOneRun() {
        assertEquals(listOf(DayRun(7, 4, 5)), DayRanges.runs(sunToThu, firstDay = 7))
    }

    @Test
    fun runOverTheEndOfTheWeekStaysWhole() {
        assertEquals(listOf(DayRun(7, 4, 5)), DayRanges.runs(sunToThu, firstDay = 1))
        assertEquals(listOf(DayRun(3, 3, 1), DayRun(6, 1, 3)), DayRanges.runs(setOf(6, 7, 1, 3), firstDay = 1))
    }

    @Test
    fun weekendAndSingleDays() {
        assertEquals(listOf(DayRun(5, 6, 2)), DayRanges.runs(setOf(5, 6), firstDay = 7))
        assertEquals(
            listOf(DayRun(1, 1, 1), DayRun(3, 3, 1), DayRun(5, 5, 1)),
            DayRanges.runs(setOf(1, 3, 5), firstDay = 1),
        )
    }

    @Test
    fun everyDayIsOneRunFromTheWeekStart() {
        assertEquals(listOf(DayRun(6, 5, 7)), DayRanges.runs(setOf(1, 2, 3, 4, 5, 6, 7), firstDay = 6))
    }

    @Test
    fun noDaysAndInvalidDays() {
        assertTrue(DayRanges.runs(emptySet(), firstDay = 1).isEmpty())
        assertEquals(listOf(DayRun(2, 2, 1)), DayRanges.runs(setOf(0, 2, 9), firstDay = 1))
    }
}
