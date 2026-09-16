package dev.apn7.shunya.core.data

import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.LauncherApp
import kotlinx.coroutines.flow.StateFlow

/**
 * Every launchable app on the phone (all profiles), merged with the user's overrides and kept
 * live as apps are installed, updated or removed. Lists are sorted by display label with a
 * locale-aware collator, so Bangla and English names sort naturally.
 */
interface AppsRepository {

    /** Every app including hidden ones (Settings > Hidden apps, favorites editor). */
    val allApps: StateFlow<List<LauncherApp>>

    /** What the drawer and search show: not hidden, and work apps only when enabled. */
    val visibleApps: StateFlow<List<LauncherApp>>

    /** Home favorites in the user's order; favorites that are uninstalled or hidden are skipped. */
    val favorites: StateFlow<List<LauncherApp>>

    /** False only during the very first start, before the cached or live list is available. */
    val isLoaded: StateFlow<Boolean>

    /** The app with [key] in the current list, or null. */
    fun find(key: AppKey): LauncherApp?

    /** Reloads the list from the system in the background. Rarely needed: changes are live. */
    fun refresh()
}
