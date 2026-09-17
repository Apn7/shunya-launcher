package dev.apn7.shunya.feature.home.drawer.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SectionIndexTest {

    @Test
    fun letterForUsesTheFirstLetterOrHash() {
        assertEquals("C", SectionIndex.letterFor("calendar"))
        assertEquals("E", SectionIndex.letterFor("Éclair"))
        assertEquals("#", SectionIndex.letterFor("2048"))
        assertEquals("#", SectionIndex.letterFor("(Beta) App"))
        assertEquals("#", SectionIndex.letterFor(""))
        assertEquals("M", SectionIndex.letterFor("  maps"))
        assertEquals("ক", SectionIndex.letterFor("কুমার"))
    }

    @Test
    fun layoutWithoutHeadersPointsSectionsAtAppRows() {
        val layout = SectionIndex.layout(listOf("2048", "Alarm", "Apps", "Browser", "Clock"), withHeaders = false)
        assertEquals(5, layout.rows.size)
        assertTrue(layout.rows.all { it is DrawerRow.App })
        assertEquals(
            listOf(Section("#", 0), Section("A", 1), Section("B", 3), Section("C", 4)),
            layout.sections,
        )
    }

    @Test
    fun layoutWithHeadersInsertsOneHeaderPerSection() {
        val layout = SectionIndex.layout(listOf("Alarm", "Apps", "Browser"), withHeaders = true)
        assertEquals(
            listOf(
                DrawerRow.Header("A"), DrawerRow.App(0), DrawerRow.App(1),
                DrawerRow.Header("B"), DrawerRow.App(2),
            ),
            layout.rows,
        )
        assertEquals(listOf(Section("A", 0), Section("B", 3)), layout.sections)
    }

    @Test
    fun repeatedLetterKeepsItsFirstSection() {
        val layout = SectionIndex.layout(listOf("Alpha", "Beta", "Ångström"), withHeaders = false)
        assertEquals(listOf(Section("A", 0), Section("B", 1)), layout.sections)
    }

    @Test
    fun emptyListHasNoSections() {
        val layout = SectionIndex.layout(emptyList(), withHeaders = true)
        assertTrue(layout.rows.isEmpty())
        assertTrue(layout.sections.isEmpty())
    }

    @Test
    fun letterIndexAtMapsPositionsAndClamps() {
        assertEquals(0, SectionIndex.letterIndexAt(0f, 100f, 4))
        assertEquals(1, SectionIndex.letterIndexAt(25f, 100f, 4))
        assertEquals(3, SectionIndex.letterIndexAt(99f, 100f, 4))
        assertEquals(3, SectionIndex.letterIndexAt(250f, 100f, 4))
        assertEquals(0, SectionIndex.letterIndexAt(-30f, 100f, 4))
        assertEquals(-1, SectionIndex.letterIndexAt(10f, 100f, 0))
        assertEquals(-1, SectionIndex.letterIndexAt(10f, 0f, 4))
    }
}
