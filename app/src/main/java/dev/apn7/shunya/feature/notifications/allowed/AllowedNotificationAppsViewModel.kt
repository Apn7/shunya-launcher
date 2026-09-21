package dev.apn7.shunya.feature.notifications.allowed

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.data.AppsRepository
import dev.apn7.shunya.core.data.SettingsRepository
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.feature.notifications.DefaultAllowedApps
import dev.apn7.shunya.feature.notifications.logic.HoldRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AllowedAppsUiState(
    /** One row per package (the allowed list is per package, not per activity or profile). */
    val apps: List<LauncherApp> = emptyList(),
    /** The user's own list, or null while the built-in defaults are in force. */
    val customised: Set<String>? = null,
    val defaults: Set<String> = emptySet(),
    /** False until the built-in list is known; ticking is disabled until then. */
    val defaultsReady: Boolean = false,
) {
    val allowed: Set<String> get() = HoldRules.effectiveAllowed(customised, defaults)
}

/** Settings > Notifications > Allowed apps. */
class AllowedNotificationAppsViewModel(
    appContext: Context,
    private val settings: SettingsRepository,
    apps: AppsRepository,
) : ViewModel() {

    private val defaults = MutableStateFlow<Set<String>?>(null)

    val state: StateFlow<AllowedAppsUiState> = combine(apps.allApps, settings.settings, defaults) { all, current, resolved ->
        AllowedAppsUiState(
            apps = all.distinctBy { it.packageName },
            customised = current.notifications.allowedPackages,
            defaults = resolved.orEmpty(),
            defaultsReady = resolved != null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AllowedAppsUiState())

    init {
        viewModelScope.launch {
            defaults.value = withContext(Dispatchers.Default) { DefaultAllowedApps.resolve(appContext) }
        }
    }

    /** Ticks or unticks [packageName]; the first change turns the built-in list into the user's own. */
    fun setAllowed(packageName: String, allowed: Boolean) {
        val builtIn = defaults.value ?: return
        settings.edit {
            val next = HoldRules.toggled(it.notifications.allowedPackages, builtIn, packageName, allowed)
            it.copy(notifications = it.notifications.copy(allowedPackages = next))
        }
    }

    /** Back to the built-in list (and to future changes of it). */
    fun resetToDefaults() {
        settings.edit { it.copy(notifications = it.notifications.copy(allowedPackages = null)) }
    }
}
