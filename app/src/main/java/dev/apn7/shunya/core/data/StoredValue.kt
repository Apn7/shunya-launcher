package dev.apn7.shunya.core.data

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * A persisted value exposed as an always-hot [StateFlow] plus atomic read-modify-write.
 * Base of the settings, overrides and focus repositories.
 *
 * Reads never block: [state] starts at [defaultValue] and switches to the stored value as soon
 * as the file is read (a few milliseconds after process start; see [isLoaded]).
 */
open class StoredValue<T>(
    private val store: DataStore<T>,
    private val scope: CoroutineScope,
    defaultValue: T,
) {
    private val loaded = MutableStateFlow(false)

    /** True once the stored value has been read at least once. */
    val isLoaded: StateFlow<Boolean> = loaded.asStateFlow()

    /** The current value; collect it in UI with `collectAsStateWithLifecycle()`. */
    val state: StateFlow<T> = store.data
        .catch { error -> if (error is IOException) emit(defaultValue) else throw error }
        .onEach { loaded.value = true }
        .stateIn(scope, SharingStarted.Eagerly, defaultValue)

    /** Atomically transforms and persists the value; suspends until it is written. */
    suspend fun update(transform: (T) -> T) {
        store.updateData { current -> transform(current) }
    }

    /**
     * Fire-and-forget [update] on the app scope. The write completes even if the calling screen
     * closes right away, so this is the right choice for toggles and pickers in UI code.
     */
    fun edit(transform: (T) -> T) {
        scope.launch { update(transform) }
    }
}
