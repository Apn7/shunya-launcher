package dev.apn7.shunya.feature.notifications.logic

/** One app's section in the Inbox: its items newest first. */
data class InboxGroup<T>(
    val packageName: String,
    val items: List<T>,
) {
    /** Time of the newest item (the group's sort key). */
    fun newest(time: (T) -> Long): Long = items.maxOfOrNull(time) ?: 0L
}

/**
 * Pure list rules of the notification Inbox (PRD 3.4): newest first, one entry per key, at most
 * `max` entries (the oldest are dropped), grouped by app. Generic so the logic can be tested
 * without the Android-side model; the inbox passes `HeldNotification` with its key/time/package.
 */
object InboxRules {

    /**
     * Adds [item] to [items] (newest first). An item with the same key replaces the old one (an app
     * updated its notification). The result is sorted newest first and holds at most [max] items.
     */
    fun <T> added(items: List<T>, item: T, max: Int, key: (T) -> String, time: (T) -> Long): List<T> {
        val newKey = key(item)
        val others = items.filter { key(it) != newKey }
        return trimmed(others + item, max, time)
    }

    /** [items] sorted newest first (stable for equal times) and cut to the newest [max]. */
    fun <T> trimmed(items: List<T>, max: Int, time: (T) -> Long): List<T> =
        items.sortedByDescending(time).take(max.coerceAtLeast(0))

    /** [items] without the one with [removedKey]. */
    fun <T> without(items: List<T>, removedKey: String, key: (T) -> String): List<T> =
        items.filter { key(it) != removedKey }

    /**
     * Groups [items] by app: groups ordered by their newest item, items inside newest first.
     * Apps with the same newest time keep their first-seen order.
     */
    fun <T> grouped(items: List<T>, packageName: (T) -> String, time: (T) -> Long): List<InboxGroup<T>> {
        val byApp = LinkedHashMap<String, MutableList<T>>()
        for (item in items.sortedByDescending(time)) {
            byApp.getOrPut(packageName(item)) { mutableListOf() }.add(item)
        }
        return byApp.map { (pkg, list) -> InboxGroup(pkg, list) }
            .sortedByDescending { group -> group.newest(time) }
    }
}
