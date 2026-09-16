package dev.apn7.shunya.core.model

/**
 * One launchable app as Shunya shows it: the system's entry merged with the user's [AppOverrides].
 * Produced by `AppsRepository`; UI code never builds these by hand (except in previews/tests).
 */
data class LauncherApp(
    val key: AppKey,
    /** Label from the system, in the phone's language. */
    val label: String,
    /** The user's rename, or null. Prefer [displayLabel] for display. */
    val customLabel: String? = null,
    /** True for apps in a work profile (show a small "work" tag). */
    val isWork: Boolean = false,
    /** True for preinstalled system apps (cannot be uninstalled, only disabled). */
    val isSystem: Boolean = false,
    val isHidden: Boolean = false,
    val isFavorite: Boolean = false,
    /** First install time, epoch millis (0 when unknown). */
    val installTime: Long = 0L,
) {
    /** What to show: the custom label when set, otherwise the system label. */
    val displayLabel: String get() = customLabel ?: label

    val packageName: String get() = key.packageName
}
