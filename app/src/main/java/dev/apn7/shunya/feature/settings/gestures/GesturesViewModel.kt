package dev.apn7.shunya.feature.settings.gestures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.data.AppsRepository
import dev.apn7.shunya.core.data.SettingsRepository
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.GestureAction
import dev.apn7.shunya.core.model.GestureBinding
import dev.apn7.shunya.core.model.GesturePrefs
import dev.apn7.shunya.core.model.HomeGesture
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.core.system.PermissionsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class GesturesUiState(
    val gestures: GesturePrefs = GesturePrefs(),
    /** Every app, to show the name of an app bound to a gesture (even a hidden one). */
    val allApps: List<LauncherApp> = emptyList(),
    /** What the "Open app…" picker offers: the drawer's apps. */
    val pickerApps: List<LauncherApp> = emptyList(),
    val accessibilityEnabled: Boolean = false,
) {
    /** Label of the app bound to [gesture], or null when none (or it was uninstalled). */
    fun boundAppLabel(gesture: HomeGesture): String? {
        val key = gestures.binding(gesture).app ?: return null
        return allApps.firstOrNull { it.key == key }?.displayLabel
    }
}

/** Settings > Gestures: maps swipe down/left/right and double-tap to actions (PRD 3.1). */
class GesturesViewModel(
    private val settings: SettingsRepository,
    apps: AppsRepository,
    permissions: PermissionsRepository,
) : ViewModel() {

    val state: StateFlow<GesturesUiState> = combine(
        settings.settings,
        apps.allApps,
        apps.visibleApps,
        permissions.status,
    ) { current, all, visible, status ->
        GesturesUiState(current.gestures, all, visible, status.isAccessibilityEnabled)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        GesturesUiState(gestures = settings.settings.value.gestures),
    )

    /** Binds [gesture] to [action] (any previously chosen app is forgotten). */
    fun setAction(gesture: HomeGesture, action: GestureAction) {
        settings.edit { it.copy(gestures = it.gestures.withBinding(gesture, GestureBinding(action))) }
    }

    /** Binds [gesture] to opening [app]. */
    fun setApp(gesture: HomeGesture, app: AppKey) {
        settings.edit { it.copy(gestures = it.gestures.withBinding(gesture, GestureBinding(GestureAction.OpenApp, app))) }
    }
}
