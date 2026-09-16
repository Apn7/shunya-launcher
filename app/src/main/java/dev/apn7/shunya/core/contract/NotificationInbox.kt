package dev.apn7.shunya.core.contract

import dev.apn7.shunya.core.model.HeldNotification
import kotlinx.coroutines.flow.StateFlow

/**
 * Notifications held by the filter. Survives process death; holds at
 * most `ProductLimits.INBOX_MAX_ITEMS` items, dropping the oldest.
 */
interface NotificationInbox {

    /** Held notifications, newest first. */
    val items: StateFlow<List<HeldNotification>>

    /** Number of held notifications (home status line). */
    val count: StateFlow<Int>

    /** Removes one item. */
    fun dismiss(key: String)

    /** Removes everything. */
    fun clearAll()

    /**
     * Opens what [item] points to: its original PendingIntent while the listener process still has
     * it, otherwise the app itself. The item is removed from the inbox.
     */
    fun open(item: HeldNotification)
}
