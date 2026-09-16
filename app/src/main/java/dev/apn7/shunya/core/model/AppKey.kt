package dev.apn7.shunya.core.model

import kotlinx.serialization.Serializable

/**
 * Identity of one launchable activity: package + activity class + the user profile it lives in.
 *
 * [userSerial] is `UserManager.getSerialNumberForUser(user)`, which stays stable across reboots
 * (unlike a `UserHandle` id). The personal profile is usually [MAIN_USER_SERIAL]; a work profile
 * has its own serial. Use [id] wherever a key must be a string (JSON map keys, Compose list keys).
 */
@Serializable
data class AppKey(
    val packageName: String,
    val activityName: String,
    val userSerial: Long = MAIN_USER_SERIAL,
) {
    /** Stable string form, e.g. `com.example/com.example.MainActivity#0`. Parse with [fromId]. */
    val id: String get() = "$packageName/$activityName#$userSerial"

    companion object {
        /** Serial number of the device's personal profile on virtually every phone. */
        const val MAIN_USER_SERIAL: Long = 0L

        /** Parses an [id]; returns null when [id] is not in the `package/activity#serial` form. */
        fun fromId(id: String): AppKey? {
            val slash = id.indexOf('/')
            val hash = id.lastIndexOf('#')
            if (slash <= 0 || hash <= slash + 1) return null
            val serial = id.substring(hash + 1).toLongOrNull() ?: return null
            return AppKey(id.substring(0, slash), id.substring(slash + 1, hash), serial)
        }
    }
}
