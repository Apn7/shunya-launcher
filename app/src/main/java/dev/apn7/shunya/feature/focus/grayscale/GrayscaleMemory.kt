package dev.apn7.shunya.feature.focus.grayscale

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** What [SecureSettingsGrayscale] must remember across restarts. */
internal data class GrayscaleState(
    /** A schedule turned grayscale on (so the schedule's end may turn it off again). */
    val ownedBySchedule: Boolean = false,
    /** Colour-correction values before grayscale was turned on; [UNSET] when unknown. */
    val previousEnabled: Int = UNSET,
    val previousMode: Int = UNSET,
) {
    companion object {
        const val UNSET = -1
    }
}

/**
 * [GrayscaleState] kept in memory (read once from a small preferences file) and saved in the
 * background, so callers on the main thread never wait for the disk. Every save writes the latest
 * state, so saves finishing out of order still leave the newest value on disk.
 */
internal class GrayscaleMemory(context: Context, private val scope: CoroutineScope) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences by lazy { appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    private val writeLock = Mutex()

    @Volatile
    private var current: GrayscaleState? = null

    fun read(): GrayscaleState = current ?: load().also { current = it }

    fun write(state: GrayscaleState) {
        current = state
        scope.launch(Dispatchers.IO) {
            writeLock.withLock {
                val latest = current ?: state
                val editor = prefs.edit()
                editor.putBoolean(KEY_OWNED, latest.ownedBySchedule)
                editor.putInt(KEY_PREVIOUS_ENABLED, latest.previousEnabled)
                editor.putInt(KEY_PREVIOUS_MODE, latest.previousMode)
                editor.commit()
            }
        }
    }

    private fun load(): GrayscaleState = GrayscaleState(
        ownedBySchedule = prefs.getBoolean(KEY_OWNED, false),
        previousEnabled = prefs.getInt(KEY_PREVIOUS_ENABLED, GrayscaleState.UNSET),
        previousMode = prefs.getInt(KEY_PREVIOUS_MODE, GrayscaleState.UNSET),
    )

    private companion object {
        const val PREFS_NAME = "focus_grayscale"
        const val KEY_OWNED = "owned_by_schedule"
        const val KEY_PREVIOUS_ENABLED = "previous_enabled"
        const val KEY_PREVIOUS_MODE = "previous_mode"
    }
}
