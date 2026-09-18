package dev.apn7.shunya.feature.focus.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchRulesTest {

    private val min = 60_000L
    private val today = "2026-09-22"
    private val yesterday = "2026-09-21"

    private fun facts(
        distracting: Boolean = false,
        focus: ActiveFocus? = null,
        limit: Int? = null,
        extension: LimitExtension? = null,
        used: Long = 0L,
        opens: Int = 0,
    ) = LaunchFacts(
        distracting = distracting,
        pauseSeconds = 10,
        focus = focus,
        limitMinutes = limit,
        extension = extension,
        today = today,
        usedTodayMillis = used,
        opensToday = opens,
    )

    @Test
    fun ordinaryAppIsAllowed() {
        assertEquals(LaunchVerdict.Allow, LaunchRules.decide(facts()))
    }

    @Test
    fun distractingAppGetsTheMindfulPause() {
        assertEquals(LaunchVerdict.Pause(10, 47 * min, 12), LaunchRules.decide(facts(distracting = true, used = 47 * min, opens = 12)))
    }

    @Test
    fun focusBlocksDistractingAppsBeforeAnythingElse() {
        val focus = ActiveFocus(endsAt = 123L, scheduleName = "Bedtime")
        val verdict = LaunchRules.decide(facts(distracting = true, focus = focus, limit = 30, used = 90 * min))
        assertEquals(LaunchVerdict.Blocked(123L, "Bedtime"), verdict)
    }

    @Test
    fun focusDoesNotBlockOtherApps() {
        assertEquals(LaunchVerdict.Allow, LaunchRules.decide(facts(focus = ActiveFocus(endsAt = null))))
    }

    @Test
    fun limitWinsOverPause() {
        val verdict = LaunchRules.decide(facts(distracting = true, limit = 30, used = 30 * min))
        assertEquals(LaunchVerdict.LimitReached(30, 30 * min, canExtend = true), verdict)
    }

    @Test
    fun underTheLimitFallsThrough() {
        assertEquals(LaunchVerdict.Allow, LaunchRules.decide(facts(limit = 30, used = 29 * min)))
        assertTrue(LaunchRules.decide(facts(distracting = true, limit = 30, used = 29 * min)) is LaunchVerdict.Pause)
    }

    @Test
    fun todaysExtensionAddsFiveMinutesOnce() {
        val extension = LimitExtension(today, 5)
        assertEquals(LaunchVerdict.Allow, LaunchRules.decide(facts(limit = 30, extension = extension, used = 32 * min)))
        val verdict = LaunchRules.decide(facts(limit = 30, extension = extension, used = 35 * min))
        assertEquals(LaunchVerdict.LimitReached(30, 35 * min, canExtend = false), verdict)
        assertFalse(LaunchRules.canExtend(extension, today))
    }

    @Test
    fun yesterdaysExtensionIsVoidToday() {
        val extension = LimitExtension(yesterday, 5)
        assertEquals(0, LaunchRules.extensionMinutesToday(extension, today))
        assertTrue(LaunchRules.canExtend(extension, today))
        val verdict = LaunchRules.decide(facts(limit = 30, extension = extension, used = 31 * min))
        assertEquals(LaunchVerdict.LimitReached(30, 31 * min, canExtend = true), verdict)
    }

    @Test
    fun zeroOrNegativeLimitMeansNoLimit() {
        assertFalse(LaunchRules.isLimitReached(0, null, today, 999 * min))
        assertEquals(LaunchVerdict.Allow, LaunchRules.decide(facts(limit = 0, used = 10 * min)))
    }

    @Test
    fun missingUsageNeverReachesALimit() {
        assertEquals(LaunchVerdict.Allow, LaunchRules.decide(facts(limit = 15, used = 0L)))
    }
}
