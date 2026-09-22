package dev.apn7.shunya.feature.settings.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.data.SettingsRepository
import dev.apn7.shunya.core.model.AppearancePrefs
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** What the Appearance screen edits. The intention toggle lives in [dev.apn7.shunya.core.model.HomePrefs]. */
data class AppearanceUiState(
    val appearance: AppearancePrefs = AppearancePrefs(),
    val showIntention: Boolean = false,
)

/**
 * Settings > Appearance. Every change is written straight to [SettingsRepository]; `MainActivity`
 * observes it, so theme, font and size apply live on this very screen.
 */
class AppearanceViewModel(private val settings: SettingsRepository) : ViewModel() {

    val state: StateFlow<AppearanceUiState> = settings.settings
        .map { AppearanceUiState(it.appearance, it.home.showIntention) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AppearanceUiState(settings.settings.value.appearance, settings.settings.value.home.showIntention),
        )

    /** Applies [transform] to the latest stored appearance (never to a stale copy). */
    fun update(transform: (AppearancePrefs) -> AppearancePrefs) {
        settings.edit { it.copy(appearance = transform(it.appearance)) }
    }

    fun setShowIntention(show: Boolean) {
        settings.edit { it.copy(home = it.home.copy(showIntention = show)) }
    }
}
