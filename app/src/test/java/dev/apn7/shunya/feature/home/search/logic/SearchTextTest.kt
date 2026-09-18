package dev.apn7.shunya.feature.home.search.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchTextTest {

    @Test
    fun foldIgnoresCaseAndAccents() {
        assertEquals("cafe", SearchText.fold("Café"))
        assertEquals("pokemon go", SearchText.fold("Pokémon GO"))
        assertEquals("strasse", SearchText.fold("Straße"))
        assertEquals("istanbul", SearchText.fold("İstanbul"))
        assertEquals("nono", SearchText.fold("Ñoño"))
    }

    @Test
    fun wordsSplitAtSeparatorsAndCamelCase() {
        assertEquals(listOf("google", "maps"), SearchText.words("Google Maps"))
        assertEquals(listOf("you", "tube"), SearchText.words("YouTube"))
        assertEquals(listOf("you", "tube", "music"), SearchText.words("YouTube Music"))
        assertEquals(listOf("bbc", "news"), SearchText.words("BBCNews"))
        assertEquals(listOf("files", "by", "google"), SearchText.words("Files by Google"))
        assertEquals(listOf("web", "3"), SearchText.words("Web3"))
        assertEquals(listOf("moj", "short", "video"), SearchText.words("Moj - Short Video"))
    }

    @Test
    fun banglaVowelSignsStayInsideTheirWord() {
        // "কুমার" contains the vowel sign "ু" (a combining mark): still one word.
        assertEquals(listOf("কুমার"), SearchText.words("কুমার"))
        assertEquals(listOf("বাংলা", "অভিধান"), SearchText.words("বাংলা অভিধান"))
    }

    @Test
    fun queryTokensSplitOnlyAtSeparators() {
        assertEquals(listOf("goo", "ma"), SearchText.queryTokens("  Goo  MA "))
        assertEquals(listOf("youtube"), SearchText.queryTokens("YouTube"))
        assertEquals(emptyList<String>(), SearchText.queryTokens("  -  "))
    }

    @Test
    fun searchKeyExposesCompactFormAndInitials() {
        val key = SearchKey("Google Maps")
        assertEquals("googlemaps", key.compact)
        assertEquals("gm", key.initials)
        assertEquals(listOf(0, 6), key.wordStarts)
        assertEquals("yt", SearchKey("YouTube").initials)
    }
}
