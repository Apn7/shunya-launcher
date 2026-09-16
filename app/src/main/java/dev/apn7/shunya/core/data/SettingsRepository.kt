package dev.apn7.shunya.core.data

import androidx.datastore.core.DataStore
import dev.apn7.shunya.core.model.LauncherSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * All user settings ([LauncherSettings]). Example:
 * `settingsRepository.edit { it.copy(drawer = it.drawer.copy(autoShowKeyboard = false)) }`.
 */
class SettingsRepository(
    store: DataStore<LauncherSettings>,
    scope: CoroutineScope,
) : StoredValue<LauncherSettings>(store, scope, LauncherSettings()) {

    /** Same as [state], under a more readable name. */
    val settings: StateFlow<LauncherSettings> get() = state
}
