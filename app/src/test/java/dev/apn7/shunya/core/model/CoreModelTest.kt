package dev.apn7.shunya.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreModelTest {

    private val maps = AppKey("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
    private val work = AppKey("com.slack", "com.slack.Main", userSerial = 10L)

    @Test
    fun appKeyIdRoundTrips() {
        assertEquals(maps, AppKey.fromId(maps.id))
        assertEquals(work, AppKey.fromId(work.id))
    }

    @Test
    fun appKeyRejectsMalformedIds() {
        assertNull(AppKey.fromId(""))
        assertNull(AppKey.fromId("no-slash#0"))
        assertNull(AppKey.fromId("pkg/activity"))
        assertNull(AppKey.fromId("pkg/activity#x"))
    }

    @Test
    fun blankLabelClearsRename() {
        val renamed = AppOverrides().withLabel(maps, "  Maps  ")
        assertEquals("Maps", renamed.labelFor(maps))
        assertNull(renamed.withLabel(maps, " ").labelFor(maps))
    }

    @Test
    fun favoritesRespectMaxAndOrder() {
        val full = AppOverrides().withFavorite(maps, favorite = true, max = 1)
        assertTrue(full.isFull(1))
        assertEquals(listOf(maps.id), full.withFavorite(work, favorite = true, max = 1).favorites)
        val two = full.withFavorite(work, favorite = true)
        assertEquals(listOf(work.id, maps.id), two.movingFavorite(from = 1, to = 0).favorites)
        assertEquals(two, two.movingFavorite(from = 0, to = 5))
    }

    @Test
    fun hidingRemovesFromFavorites() {
        val hidden = AppOverrides().withFavorite(maps, favorite = true).withHidden(maps, hidden = true)
        assertTrue(hidden.isHidden(maps))
        assertFalse(hidden.isFavorite(maps))
    }

    @Test
    fun extensionsOnlyCountForTheirDay() {
        val today = "2026-09-22"
        val extended = LimitExtensions().plus("com.youtube", 5, today)
        assertEquals(5, extended.minutesFor("com.youtube", today))
        assertEquals(0, extended.minutesFor("com.youtube", "2026-09-23"))
        assertEquals(mapOf("com.x" to 5), extended.plus("com.x", 5, "2026-09-23").minutesByPackage)
    }

    @Test
    fun openEndedSessionStaysActive() {
        val session = FocusSession(startedAt = 1_000L)
        assertTrue(session.isActiveAt(10_000_000L))
        assertNull(session.remainingMillis(5_000L))
        val timed = FocusSession(startedAt = 1_000L, endsAt = 2_000L)
        assertFalse(timed.isActiveAt(2_000L))
        assertEquals(0L, timed.remainingMillis(3_000L))
    }

    @Test
    fun gestureBindingsAreIndependent() {
        val app = GestureBinding(GestureAction.OpenApp, maps)
        val prefs = GesturePrefs().withBinding(HomeGesture.SwipeRight, app)
        assertEquals(app, prefs.binding(HomeGesture.SwipeRight))
        assertEquals(GestureAction.ScreenTime, prefs.binding(HomeGesture.SwipeLeft).action)
        assertTrue(HomeGesture.values().all { prefs.binding(it).action in it.options })
    }
}
