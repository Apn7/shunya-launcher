package dev.apn7.shunya.feature.home.search.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FuzzyMatcherTest {

    private fun kind(query: String, label: String): MatchKind? =
        FuzzyMatcher.match(SearchQuery(query), SearchKey(label))?.kind

    private fun rank(query: String, vararg labels: String): List<String> =
        FuzzyMatcher.rank(query, labels.toList()) { listOf(SearchKey(it)) }

    @Test
    fun recognisesEveryMatchKind() {
        assertEquals(MatchKind.Exact, kind("youtube", "YouTube"))
        assertEquals(MatchKind.Prefix, kind("you", "YouTube"))
        assertEquals(MatchKind.WordInitials, kind("yt", "YouTube"))
        assertEquals(MatchKind.WordInitials, kind("gm", "Google Maps"))
        assertEquals(MatchKind.WordPrefix, kind("maps", "Google Maps"))
        assertEquals(MatchKind.Substring, kind("ube", "YouTube"))
        assertEquals(MatchKind.Subsequence, kind("ytb", "YouTube"))
        assertNull(kind("xyz", "YouTube"))
    }

    @Test
    fun ignoresCaseAccentsAndSpaces() {
        assertEquals(MatchKind.Exact, kind("YOUTUBE", "YouTube"))
        assertEquals(MatchKind.Exact, kind("you tube", "YouTube"))
        assertEquals(MatchKind.Exact, kind("pokemon go", "Pokémon GO"))
        assertEquals(MatchKind.Prefix, kind("Poké", "Pokémon GO"))
        assertEquals(MatchKind.Prefix, kind("googlem", "Google Maps"))
    }

    @Test
    fun multiWordQueriesMatchWordPrefixesInOrder() {
        assertEquals(MatchKind.WordPrefix, kind("goo ma", "Google Maps"))
        assertEquals(MatchKind.WordPrefix, kind("tube mus", "YouTube Music"))
        assertNull(kind("ma goo", "Google Maps"))
    }

    @Test
    fun blankOrSymbolOnlyQueryMatchesNothing() {
        assertNull(kind("", "YouTube"))
        assertNull(kind("  ", "YouTube"))
        assertEquals(listOf("A", "B"), rank(" ", "A", "B"))
    }

    @Test
    fun ranksExactThenPrefixThenInitialsThenWordPrefixThenSubstringThenSubsequence() {
        val ranked = rank(
            "ma",
            "Gmail", // substring "ma"
            "Google Maps", // word prefix
            "Money Assistant", // word initials "ma"
            "Maps", // prefix
            "Ma", // exact
            "Mega", // subsequence m..a
        )
        assertEquals(listOf("Ma", "Maps", "Money Assistant", "Google Maps", "Gmail", "Mega"), ranked)
    }

    @Test
    fun specExamples() {
        assertEquals("YouTube", rank("yt", "Yahoo", "YouTube", "Twitter").first())
        assertEquals("Google Maps", rank("gm", "Google Maps", "Messages", "Gallery").first())
        // Prefix beats initials: "gm" is the start of Gmail.
        assertEquals(listOf("Gmail", "Google Maps"), rank("gm", "Google Maps", "Gmail"))
    }

    @Test
    fun shorterLabelsWinWithinATierAndTiesKeepInputOrder() {
        assertEquals(listOf("Maps", "Maps.me"), rank("ma", "Maps.me", "Maps"))
        assertEquals(listOf("Clock A", "Clock B"), rank("clock", "Clock A", "Clock B"))
    }

    @Test
    fun bestKeyWinsForRenamedApps() {
        val apps = listOf("Tube" to "YouTube", "Notes" to "Keep Notes")
        val ranked = FuzzyMatcher.rank("youtube", apps) { listOf(SearchKey(it.first), SearchKey(it.second)) }
        assertEquals(listOf("Tube" to "YouTube"), ranked)
    }

    @Test
    fun matchesBanglaLabels() {
        assertEquals(MatchKind.Prefix, kind("বাং", "বাংলা অভিধান"))
        assertEquals(MatchKind.WordPrefix, kind("অভি", "বাংলা অভিধান"))
        assertTrue(rank("অভিধান", "বাংলা অভিধান", "YouTube").contains("বাংলা অভিধান"))
    }

    @Test
    fun subsequencePrefersTighterMatches() {
        // "cln": Calendar has 2 gaps (c-a-l-e-n), Chat Online has 6.
        assertEquals(MatchKind.Subsequence, kind("cln", "Calendar"))
        assertEquals(listOf("Calendar", "Chat Online"), rank("cln", "Chat Online", "Calendar"))
    }

    @Test
    fun initialsBeatSubsequence() {
        assertEquals(listOf("Call Log Tracker", "Calculator"), rank("clt", "Calculator", "Call Log Tracker"))
    }
}
