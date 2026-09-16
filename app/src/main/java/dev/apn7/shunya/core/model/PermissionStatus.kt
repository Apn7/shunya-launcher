package dev.apn7.shunya.core.model

/**
 * Snapshot of every special access Shunya can use. Every feature must work (degraded) when any
 * of these is false. Refreshed by `PermissionsRepository.refresh()` on every resume.
 */
data class PermissionStatus(
    val isDefaultLauncher: Boolean = false,
    /** Usage access: screen time, daily limits, "most used" sort. */
    val hasUsageAccess: Boolean = false,
    /** Notification access: the notification filter and Inbox. */
    val hasNotificationAccess: Boolean = false,
    /** Shunya's accessibility service: double-tap to lock, system-wide blocking. */
    val isAccessibilityEnabled: Boolean = false,
    /** WRITE_SECURE_SETTINGS (granted over ADB): grayscale. */
    val canWriteSecureSettings: Boolean = false,
)
