package dev.apn7.shunya.feature.settings.language.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LanguageTagsTest {

    private val supported = listOf("en", "bn")

    @Test
    fun matchesPrimarySubtag() {
        assertEquals("bn", LanguageTags.match("bn-BD", supported))
        assertEquals("bn", LanguageTags.match("bn", supported))
        assertEquals("en", LanguageTags.match("en-US", supported))
        assertEquals("en", LanguageTags.match("EN_us", supported))
    }

    @Test
    fun emptyOrUnsupportedIsNull() {
        assertNull(LanguageTags.match("", supported))
        assertNull(LanguageTags.match(null, supported))
        assertNull(LanguageTags.match("fr-FR", supported))
    }
}
