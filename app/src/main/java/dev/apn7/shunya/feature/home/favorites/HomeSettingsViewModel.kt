package dev.apn7.shunya.feature.home.favorites

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.data.AppOverridesRepository
import dev.apn7.shunya.core.data.AppsRepository
import dev.apn7.shunya.core.data.SettingsRepository
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.HomePrefs
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.core.model.ProductLimits
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Settings > Home: the favorites in order, the apps that can still be added, and the home prefs. */
@Immutable
data class HomeSettingsUiState(
    val favorites: List<LauncherApp> = emptyList(),
    /** Visible apps that are not on home yet (the "Add app" picker). */
    val candidates: List<LauncherApp> = emptyList(),
    val home: HomePrefs = HomePrefs(),
) {
    val isFull: Boolean get() = favorites.size >= home.maxFavorites
}

/** Favorites editor (PRD 3.5 Home): add, remove, reorder, rename, max favorites, intention line. */
class HomeSettingsViewModel(
    appsRepository: AppsRepository,
    private val overridesRepository: AppOverridesRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<HomeSettingsUiState> = combine(
        appsRepository.favorites,
        appsRepository.visibleApps,
        settingsRepository.settings,
    ) { favorites, visible, settings ->
        HomeSettingsUiState(favorites, visible.filter { !it.isFavorite }, settings.home)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeSettingsUiState(
            favorites = appsRepository.favorites.value,
            candidates = appsRepository.visibleApps.value.filter { !it.isFavorite },
            home = settingsRepository.settings.value.home,
        ),
    )

    fun add(app: LauncherApp) {
        val max = settingsRepository.settings.value.home.maxFavorites
        overridesRepository.edit { it.withFavorite(app.key, favorite = true, max = max) }
    }

    fun remove(app: LauncherApp) {
        overridesRepository.edit { it.withFavorite(app.key, favorite = false) }
    }

    /**
     * Moves the favorite at [index] by [delta] places (-1 up, +1 down). Rewrites the whole order
     * from what the user sees, which also drops entries of apps that were uninstalled.
     */
    fun move(index: Int, delta: Int) {
        val order = state.value.favorites.map { it.key }.toMutableList()
        val target = index + delta
        if (index !in order.indices || target !in order.indices) return
        val moved = order.removeAt(index)
        order.add(target, moved)
        overridesRepository.edit { it.withFavorites(order, ProductLimits.FAVORITES_MAX) }
    }

    fun rename(app: LauncherApp, label: String) {
        val clean = label.trim()
        overridesRepository.edit { it.withLabel(app.key, if (clean.isEmpty() || clean == app.label) null else clean) }
    }

    fun resetName(app: LauncherApp) {
        overridesRepository.edit { it.withLabel(app.key, null) }
    }

    /** Sets the maximum; favorites beyond it are removed from the end of the list. */
    fun setMaxFavorites(max: Int) {
        val clamped = max.coerceIn(0, ProductLimits.FAVORITES_MAX)
        settingsRepository.edit { it.copy(home = it.home.copy(maxFavorites = clamped)) }
        overridesRepository.edit { overrides ->
            if (overrides.favorites.size <= clamped) {
                overrides
            } else {
                overrides.withFavorites(overrides.favorites.mapNotNull { id -> AppKey.fromId(id) }, clamped)
            }
        }
    }

    fun setShowIntention(show: Boolean) {
        settingsRepository.edit { it.copy(home = it.home.copy(showIntention = show)) }
    }

    fun setIntention(text: String) {
        settingsRepository.edit { it.copy(home = it.home.copy(intention = text.trim())) }
    }

    fun setShowAppsButton(show: Boolean) {
        settingsRepository.edit { it.copy(home = it.home.copy(showAppsButton = show)) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
