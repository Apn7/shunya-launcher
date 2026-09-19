package dev.apn7.shunya.feature.focus.distracting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.data.AppsRepository
import dev.apn7.shunya.core.data.FocusConfigRepository
import dev.apn7.shunya.core.model.FocusConfig
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.core.model.ProductLimits
import dev.apn7.shunya.feature.focus.ui.PackageRow
import dev.apn7.shunya.feature.focus.ui.packageRows
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DistractingAppsUiState(
    val apps: List<PackageRow> = emptyList(),
    val distracting: Set<String> = emptySet(),
    val pauseSeconds: Int = ProductLimits.PAUSE_SECONDS_DEFAULT,
)

class DistractingAppsViewModel(
    appsRepository: AppsRepository,
    private val configRepository: FocusConfigRepository,
) : ViewModel() {

    val state: StateFlow<DistractingAppsUiState> =
        combine(appsRepository.allApps, configRepository.config) { apps, config -> uiState(apps, config) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                uiState(appsRepository.allApps.value, configRepository.config.value),
            )

    fun setDistracting(packageName: String, distracting: Boolean) {
        configRepository.edit {
            val packages = if (distracting) it.distractingPackages + packageName else it.distractingPackages - packageName
            it.copy(distractingPackages = packages)
        }
    }

    fun setPauseSeconds(seconds: Int) {
        val clamped = seconds.coerceIn(ProductLimits.PAUSE_SECONDS_MIN, ProductLimits.PAUSE_SECONDS_MAX)
        configRepository.edit { it.copy(defaultPauseSeconds = clamped) }
    }

    private fun uiState(apps: List<LauncherApp>, config: FocusConfig) = DistractingAppsUiState(
        apps = packageRows(apps),
        distracting = config.distractingPackages,
        pauseSeconds = config.defaultPauseSeconds,
    )
}
