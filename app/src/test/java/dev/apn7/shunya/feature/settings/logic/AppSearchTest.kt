package dev.apn7.shunya.feature.settings.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSearchTest {

    @Test
    fun blankQueryMatchesEverything() {
        assertTrue(AppSearch.matches("Maps", ""))
        assertTrue(AppSearch.matches("Maps", "   "))
    }

    @Test
    fun substringIsCaseInsensitive() {
        assertTrue(AppSearch.matches("YouTube", "tube"))
        assertTrue(AppSearch.matches("YouTube", "YOU"))
        assertFalse(AppSearch.matches("YouTube", "maps"))
    }

    @Test
    fun accentsAreIgnoredBothWays() {
        assertTrue(AppSearch.matches("Café Finder", "cafe"))
        assertTrue(AppSearch.matches("Cafe Finder", "café"))
    }

    @Test
    fun wordInitialsMatch() {
        assertTrue(AppSearch.matches("Google Maps", "gm"))
        assertTrue(AppSearch.matches("Google Play Store", "gps"))
        assertFalse(AppSearch.matches("Google Maps", "mg"))
    }

    @Test
    fun banglaLabelsMatchBanglaQueries() {
        assertTrue(AppSearch.matches("বিকাশ", "বিকা"))
        assertFalse(AppSearch.matches("বিকাশ", "নগদ"))
    }

    @Test
    fun filterKeepsOriginalOrder() {
        val labels = listOf("Messages", "Maps", "Music", "Phone")
        assertEquals(listOf("Messages", "Maps", "Music"), AppSearch.filter(labels, "m") { it })
        assertEquals(labels, AppSearch.filter(labels, "") { it })
    }
}
