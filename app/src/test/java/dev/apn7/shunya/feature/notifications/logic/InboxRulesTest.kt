package dev.apn7.shunya.feature.notifications.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class InboxRulesTest {

    private data class Item(val key: String, val pkg: String, val time: Long)

    private val key: (Item) -> String = { it.key }
    private val time: (Item) -> Long = { it.time }
    private val pkg: (Item) -> String = { it.pkg }

    @Test
    fun addedKeepsNewestFirst() {
        val list = InboxRules.added(listOf(Item("a", "x", 10)), Item("b", "x", 20), max = 300, key = key, time = time)
        assertEquals(listOf("b", "a"), list.map { it.key })
    }

    @Test
    fun olderItemArrivingLateIsSortedIn() {
        val list = listOf(Item("new", "x", 50), Item("old", "x", 10))
        val result = InboxRules.added(list, Item("mid", "y", 30), max = 300, key = key, time = time)
        assertEquals(listOf("new", "mid", "old"), result.map { it.key })
    }

    @Test
    fun sameKeyReplacesTheOldEntry() {
        val list = listOf(Item("a", "x", 10), Item("b", "x", 5))
        val result = InboxRules.added(list, Item("b", "x", 30), max = 300, key = key, time = time)
        assertEquals(listOf(Item("b", "x", 30), Item("a", "x", 10)), result)
    }

    @Test
    fun oldestAreDroppedBeyondMax() {
        var list = emptyList<Item>()
        for (i in 1..305) list = InboxRules.added(list, Item("k$i", "x", i.toLong()), max = 300, key = key, time = time)
        assertEquals(300, list.size)
        assertEquals("k305", list.first().key)
        assertEquals("k6", list.last().key)
    }

    @Test
    fun trimmedHandlesZeroAndNegativeMax() {
        val list = listOf(Item("a", "x", 1))
        assertEquals(emptyList<Item>(), InboxRules.trimmed(list, 0, time))
        assertEquals(emptyList<Item>(), InboxRules.trimmed(list, -1, time))
    }

    @Test
    fun withoutRemovesOnlyThatKey() {
        val list = listOf(Item("a", "x", 2), Item("b", "x", 1))
        assertEquals(listOf(Item("b", "x", 1)), InboxRules.without(list, "a", key))
        assertEquals(list, InboxRules.without(list, "zzz", key))
    }

    @Test
    fun groupedByAppNewestGroupFirst() {
        val items = listOf(
            Item("1", "mail", 10),
            Item("2", "chat", 40),
            Item("3", "mail", 30),
            Item("4", "news", 20),
            Item("5", "chat", 5),
        )
        val groups = InboxRules.grouped(items, pkg, time)
        assertEquals(listOf("chat", "mail", "news"), groups.map { it.packageName })
        assertEquals(listOf("2", "5"), groups[0].items.map { it.key })
        assertEquals(listOf("3", "1"), groups[1].items.map { it.key })
        assertEquals(40L, groups[0].newest(time))
    }

    @Test
    fun groupedEmptyListIsEmpty() {
        assertEquals(emptyList<InboxGroup<Item>>(), InboxRules.grouped(emptyList(), pkg, time))
    }
}
