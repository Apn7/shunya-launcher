package dev.apn7.shunya.core.model

import kotlinx.serialization.Serializable

/**
 * A notification the filter removed from the shade and kept for the Inbox (PRD 3.4).
 * Persisted by the inbox store; not part of backups.
 */
@Serializable
data class HeldNotification(
    /** `StatusBarNotification.key`: unique per notification. */
    val key: String,
    val packageName: String,
    /** App label at the time it was held (the app may be uninstalled later). */
    val appLabel: String,
    val title: String,
    val text: String,
    /** Epoch millis when the app posted it. */
    val postedAt: Long,
    /** Profile of the posting app, see [AppKey.userSerial]. */
    val userSerial: Long = AppKey.MAIN_USER_SERIAL,
)
