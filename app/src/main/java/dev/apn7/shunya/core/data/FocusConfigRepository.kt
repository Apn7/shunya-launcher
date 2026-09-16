package dev.apn7.shunya.core.data

import androidx.datastore.core.DataStore
import dev.apn7.shunya.core.model.FocusConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Focus rules ([FocusConfig]): pure persistence. The rules themselves (what is blocked when)
 * live in the `FocusController` / `LaunchPolicy` implementations.
 */
class FocusConfigRepository(
    store: DataStore<FocusConfig>,
    scope: CoroutineScope,
) : StoredValue<FocusConfig>(store, scope, FocusConfig()) {

    val config: StateFlow<FocusConfig> get() = state
}
