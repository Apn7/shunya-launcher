package dev.apn7.shunya.feature.home.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeLogicTest {

    private val minute = 60_000L

    @Test
    fun clockTicksAlignToTheNextMinuteAndSecond() {
        assertEquals(minute, ClockTicks.millisUntilNextMinute(0L))
        assertEquals(1L, ClockTicks.millisUntilNextMinute(minute - 1))
        assertEquals(30_000L, ClockTicks.millisUntilNextMinute(10 * minute + 30_000L))
        assertEquals(1_000L, ClockTicks.millisUntilNextSecond(5_000L))
        assertEquals(750L, ClockTicks.millisUntilNextSecond(5_250L))
    }

    @Test
    fun countdownsRoundUpToWholeMinutes() {
        assertEquals(0L, ClockTicks.ceilToMinute(0L))
        assertEquals(0L, ClockTicks.ceilToMinute(-5L))
        assertEquals(minute, ClockTicks.ceilToMinute(1L))
        assertEquals(25 * minute, ClockTicks.ceilToMinute(24 * minute + 10_000L))
        assertEquals(25 * minute, ClockTicks.ceilToMinute(25 * minute))
    }

    @Test
    fun statusPrefersFocusThenHeldNotificationsThenScreenTime() {
        val now = 1_000_000L
        assertEquals(
            HomeStatus.Focus(23 * minute),
            HomeStatus.pick(true, now + 23 * minute, now, heldCount = 4, screenTimeMillis = 5L),
        )
        assertEquals(HomeStatus.Focus(null), HomeStatus.pick(true, null, now, 0, null))
        assertEquals(HomeStatus.HeldNotifications(4), HomeStatus.pick(false, null, now, 4, 5L))
        assertEquals(HomeStatus.ScreenTime(72 * minute), HomeStatus.pick(false, null, now, 0, 72 * minute))
        assertNull(HomeStatus.pick(false, null, now, 0, null))
    }

    @Test
    fun focusThatAlreadyEndedShowsZeroLeft() {
        assertEquals(HomeStatus.Focus(0L), HomeStatus.pick(true, 10L, 20L, 0, null))
    }

    @Test
    fun swipesNeedDistanceAndAClearAxis() {
        assertNull(SwipeClassifier.classify(5f, -10f, minDistance = 50f))
        assertEquals(SwipeDirection.Up, SwipeClassifier.classify(10f, -80f, 50f))
        assertEquals(SwipeDirection.Down, SwipeClassifier.classify(-10f, 80f, 50f))
        assertEquals(SwipeDirection.Left, SwipeClassifier.classify(-90f, 20f, 50f))
        assertEquals(SwipeDirection.Right, SwipeClassifier.classify(90f, -20f, 50f))
        assertNull(SwipeClassifier.classify(70f, 70f, 50f))
    }
}
