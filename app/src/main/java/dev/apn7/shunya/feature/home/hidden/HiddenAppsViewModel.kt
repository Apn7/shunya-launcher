package dev.apn7.shunya.feature.home.hidden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.data.AppOverridesRepository
import dev.apn7.shunya.core.data.AppsRepository
import dev.apn7.shunya.core.model.LauncherApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Settings > Hidden apps: the hidden apps (alphabetical, from the repository) and unhiding. */
class HiddenAppsViewModel(
    appsRepository: AppsRepository,
    private val overridesRepository: AppOverridesRepository,
) : ViewModel() {

    val hiddenApps: StateFlow<List<LauncherApp>> = appsRepository.allApps
        .map { apps -> apps.filter { it.isHidden } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = appsRepository.allApps.value.filter { it.isHidden },
        )

    fun unhide(app: LauncherApp) {
        overridesRepository.edit { it.withHidden(app.key, hidden = false) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
