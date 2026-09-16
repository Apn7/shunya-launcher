package dev.apn7.shunya.core.data

import androidx.datastore.core.DataStore
import dev.apn7.shunya.core.model.AppOverrides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Renames, hidden apps and ordered favorites ([AppOverrides]). Change them through the model's
 * `with…` functions: `overridesRepository.edit { it.withFavorite(app.key, favorite = true, max) }`.
 */
class AppOverridesRepository(
    store: DataStore<AppOverrides>,
    scope: CoroutineScope,
) : StoredValue<AppOverrides>(store, scope, AppOverrides()) {

    val overrides: StateFlow<AppOverrides> get() = state
}
