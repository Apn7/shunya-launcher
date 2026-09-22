package dev.apn7.shunya.feature.settings.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.data.AppsRepository
import dev.apn7.shunya.core.data.SettingsRepository
import dev.apn7.shunya.core.model.DrawerPrefs
import dev.apn7.shunya.core.system.PermissionsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DrawerSettingsUiState(
    val drawer: DrawerPrefs = DrawerPrefs(),
    /** The home "apps" text button (stored in `HomePrefs`), offered here as another way into the drawer. */
    val showAppsButton: Boolean = false,
    val hasUsageAccess: Boolean = false,
    val hiddenCount: Int = 0,
)

/** Settings > App drawer (PRD 3.5). */
class DrawerSettingsViewModel(
    private val settings: SettingsRepository,
    apps: AppsRepository,
    permissions: PermissionsRepository,
) : ViewModel() {

    val state: StateFlow<DrawerSettingsUiState> = combine(
        settings.settings,
        apps.allApps,
        permissions.status,
    ) { current, all, status ->
        DrawerSettingsUiState(
            drawer = current.drawer,
            showAppsButton = current.home.showAppsButton,
            hasUsageAccess = status.hasUsageAccess,
            hiddenCount = all.count { it.isHidden },
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DrawerSettingsUiState(drawer = settings.settings.value.drawer, showAppsButton = settings.settings.value.home.showAppsButton),
    )

    /** Applies [transform] to the latest stored drawer settings. */
    fun update(transform: (DrawerPrefs) -> DrawerPrefs) {
        settings.edit { it.copy(drawer = transform(it.drawer)) }
    }

    fun setShowAppsButton(show: Boolean) {
        settings.edit { it.copy(home = it.home.copy(showAppsButton = show)) }
    }
}
