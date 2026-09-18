package dev.apn7.shunya.feature.focus.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageTimelineTest {

    private val min = 60_000L
    private val hour = 60 * min
    private val start = 1_000_000_000_000L
    private val end = start + 24 * hour

    private fun resumed(t: Long, pkg: String, cls: String = "$pkg.Main") = UsageEvent(t, UsageEventType.Resumed, pkg, cls)
    private fun paused(t: Long, pkg: String, cls: String = "$pkg.Main") = UsageEvent(t, UsageEventType.Paused, pkg, cls)
    private fun stopped(t: Long, pkg: String, cls: String = "$pkg.Main") = UsageEvent(t, UsageEventType.Stopped, pkg, cls)
    private fun screenOff(t: Long) = UsageEvent(t, UsageEventType.AllStopped)
    private fun unlock(t: Long) = UsageEvent(t, UsageEventType.Unlock)

    private fun build(vararg events: UsageEvent, ignored: Set<String> = emptySet()) =
        UsageTimeline.build(events.toList(), start, end, ignored)

    @Test
    fun resumePausePairCountsItsDuration() {
        val timeline = build(resumed(start + hour, "yt"), paused(start + hour + 10 * min, "yt"))
        assertEquals(10 * min, timeline.foregroundOf("yt"))
        assertEquals(1, timeline.opensOf("yt"))
    }

    @Test
    fun appAlreadyInFrontAtWindowStartCountsFromTheStart() {
        val timeline = build(paused(start + 5 * min, "yt"))
        assertEquals(5 * min, timeline.foregroundOf("yt"))
        assertEquals(0, timeline.opensOf("yt"))
    }

    @Test
    fun appStillInFrontAtWindowEndCountsUntilTheEnd() {
        val timeline = UsageTimeline.build(listOf(resumed(start + hour, "yt")), start, start + 2 * hour)
        assertEquals(hour, timeline.foregroundOf("yt"))
    }

    @Test
    fun lookBackResumeIsClampedAndIsNotAnOpen() {
        val timeline = build(resumed(start - 30 * min, "yt"), paused(start + 20 * min, "yt"))
        assertEquals(20 * min, timeline.foregroundOf("yt"))
        assertEquals(0, timeline.opensOf("yt"))
    }

    @Test
    fun overlappingActivitiesOfOneAppAreCountedOnce() {
        val timeline = build(
            resumed(start + 0 * min, "maps", "maps.A"),
            resumed(start + 5 * min, "maps", "maps.B"),
            paused(start + 10 * min, "maps", "maps.A"),
            paused(start + 20 * min, "maps", "maps.B"),
        )
        assertEquals(20 * min, timeline.foregroundOf("maps"))
        assertEquals(1, timeline.opensOf("maps"))
    }

    @Test
    fun switchingActivitiesInsideAnAppIsNotANewOpen() {
        val timeline = build(
            resumed(start, "chat", "chat.List"),
            paused(start + min, "chat", "chat.List"),
            resumed(start + min, "chat", "chat.Thread"),
            paused(start + 3 * min, "chat", "chat.Thread"),
        )
        assertEquals(3 * min, timeline.foregroundOf("chat"))
        assertEquals(1, timeline.opensOf("chat"))
    }

    @Test
    fun missingPauseIsClosedByScreenOff() {
        val timeline = build(resumed(start, "yt"), screenOff(start + 7 * min), paused(start + 7 * min + 500, "yt"))
        assertEquals(7 * min, timeline.foregroundOf("yt"))
    }

    @Test
    fun missingPauseIsClosedByStop() {
        val timeline = build(resumed(start, "yt"), stopped(start + 4 * min, "yt"))
        assertEquals(4 * min, timeline.foregroundOf("yt"))
    }

    @Test
    fun stopAfterPauseAddsNothing() {
        val timeline = build(resumed(start, "yt"), paused(start + 4 * min, "yt"), stopped(start + 5 * min, "yt"))
        assertEquals(4 * min, timeline.foregroundOf("yt"))
    }

    @Test
    fun leadingStopOfAnUnseenActivityIsIgnored() {
        val timeline = build(stopped(start + 3 * min, "yt"))
        assertEquals(0L, timeline.foregroundOf("yt"))
    }

    @Test
    fun unseenPauseAfterScreenOffEndsAtTheScreenOff() {
        val timeline = build(screenOff(start + 2 * min), paused(start + 90 * min, "yt"))
        assertEquals(2 * min, timeline.foregroundOf("yt"))
    }

    @Test
    fun duplicateResumeKeepsTheFirstStart() {
        val timeline = build(resumed(start, "yt"), resumed(start + 5 * min, "yt"), paused(start + 10 * min, "yt"))
        assertEquals(10 * min, timeline.foregroundOf("yt"))
        assertEquals(1, timeline.opensOf("yt"))
    }

    @Test
    fun opensCountSwitchesFromOtherAppsOnly() {
        val timeline = build(
            resumed(start, "yt"), paused(start + min, "yt"),
            resumed(start + min, "chat"), paused(start + 2 * min, "chat"),
            resumed(start + 2 * min, "yt"), paused(start + 3 * min, "yt"),
            screenOff(start + 3 * min),
            resumed(start + 10 * min, "yt"), paused(start + 11 * min, "yt"),
        )
        assertEquals(2, timeline.opensOf("yt"))
        assertEquals(1, timeline.opensOf("chat"))
        assertEquals(mapOf("yt" to 2, "chat" to 1), timeline.opensByPackage())
    }

    @Test
    fun ignoredPackageIsDroppedButStillSeparatesOpens() {
        val timeline = build(
            resumed(start, "yt"), paused(start + min, "yt"),
            resumed(start + min, "shunya"), paused(start + 2 * min, "shunya"),
            resumed(start + 2 * min, "yt"), paused(start + 3 * min, "yt"),
            ignored = setOf("shunya"),
        )
        assertEquals(2, timeline.opensOf("yt"))
        assertEquals(0L, timeline.foregroundOf("shunya"))
        assertEquals(setOf("yt"), timeline.foregroundByPackage().keys)
    }

    @Test
    fun unlocksAreCountedInsideTheWindowOnly() {
        val timeline = build(unlock(start - min), unlock(start + min), unlock(start + 2 * hour))
        assertEquals(2, timeline.unlockCount())
        assertEquals(1, timeline.unlockCount(start, start + hour))
    }

    @Test
    fun spanAcrossMidnightIsSplitBetweenDays() {
        val midnight = start + 24 * hour
        val timeline = UsageTimeline.build(
            listOf(resumed(midnight - 10 * min, "yt"), paused(midnight + 15 * min, "yt")),
            start,
            midnight + 24 * hour,
        )
        assertEquals(listOf(10 * min, 15 * min), timeline.bucketMillis(listOf(start, midnight, midnight + 24 * hour)))
        assertEquals(10 * min, timeline.foregroundOf("yt", start, midnight))
    }

    @Test
    fun bucketsCanBeLimitedToOnePackage() {
        val timeline = build(
            resumed(start, "yt"), paused(start + 30 * min, "yt"),
            resumed(start + 30 * min, "chat"), paused(start + 90 * min, "chat"),
        )
        val hours = listOf(start, start + hour, start + 2 * hour)
        assertEquals(listOf(30 * min, 0L), timeline.bucketMillis(hours, "yt"))
        assertEquals(listOf(hour, 30 * min), timeline.bucketMillis(hours))
        assertTrue(timeline.bucketMillis(listOf(start)).isEmpty())
    }

    @Test
    fun eventsAreSortedAndThoseAfterTheWindowIgnored() {
        val timeline = UsageTimeline.build(
            listOf(paused(start + 10 * min, "yt"), resumed(start, "yt"), resumed(start + 3 * hour, "chat")),
            start,
            start + 2 * hour,
        )
        assertEquals(10 * min, timeline.foregroundOf("yt"))
        assertEquals(0L, timeline.foregroundOf("chat"))
        assertEquals(0, timeline.opensOf("chat"))
    }

    @Test
    fun splitScreenAppsBothCount() {
        val timeline = build(
            resumed(start, "maps"),
            resumed(start + min, "music"),
            paused(start + 5 * min, "music"),
            paused(start + 6 * min, "maps"),
        )
        assertEquals(6 * min, timeline.foregroundOf("maps"))
        assertEquals(4 * min, timeline.foregroundOf("music"))
    }
}
