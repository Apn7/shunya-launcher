package dev.apn7.shunya.feature.focus.grayscale

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import dev.apn7.shunya.core.contract.GrayscaleController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [GrayscaleController] over the system colour-correction ("daltonizer") secure settings: grayscale
 * is the daltonizer switched on in monochromacy mode (0). Writing needs WRITE_SECURE_SETTINGS,
 * granted once over ADB ([adbGrantCommand]); reading works without it.
 *
 * Someone who already uses colour correction for colour blindness gets their own setup back when
 * grayscale is turned off (the previous values are remembered while grayscale is on).
 */
class SecureSettingsGrayscale(context: Context, scope: CoroutineScope) : GrayscaleController {

    private val appContext = context.applicationContext
    private val resolver: ContentResolver = appContext.contentResolver
    private val memory = GrayscaleMemory(appContext, scope)
    private val enabled = MutableStateFlow(readEnabled())

    override val adbGrantCommand: String =
        "adb shell pm grant ${appContext.packageName} android.permission.WRITE_SECURE_SETTINGS"

    override val isEnabled: StateFlow<Boolean> = enabled.asStateFlow()

    init {
        // Also follows changes made in system settings or by the quick-settings tile.
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                enabled.value = readEnabled()
            }
        }
        try {
            resolver.registerContentObserver(Settings.Secure.getUriFor(KEY_ENABLED), false, observer)
            resolver.registerContentObserver(Settings.Secure.getUriFor(KEY_MODE), false, observer)
        } catch (e: SecurityException) {
            // Some ROMs refuse observers on secure settings; the state is re-read after every change we make.
        }
    }

    override fun isAvailable(): Boolean =
        appContext.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

    /** The user's own toggle; it also forgets that a schedule turned grayscale on. */
    override fun setEnabled(enabled: Boolean): Boolean {
        if (!isAvailable()) return false
        return try {
            if (enabled) turnOn() else turnOff()
            memory.write(memory.read().copy(ownedBySchedule = false))
            this.enabled.value = readEnabled()
            true
        } catch (e: SecurityException) {
            false
        }
    }

    /**
     * Called by the focus controller when a "grayscale during this schedule" schedule starts
     * ([wanted] = true) or the last one ends. Turning on is remembered as the schedule's doing (also
     * across restarts); turning off happens only then, so the user's own grayscale is left alone.
     */
    fun applySchedule(wanted: Boolean) {
        if (!isAvailable()) return
        try {
            if (wanted) {
                if (!readEnabled()) {
                    turnOn()
                    memory.write(memory.read().copy(ownedBySchedule = true))
                }
            } else if (memory.read().ownedBySchedule) {
                turnOff()
                memory.write(memory.read().copy(ownedBySchedule = false))
            }
            enabled.value = readEnabled()
        } catch (e: SecurityException) {
            // Permission revoked in the meantime: nothing to do.
        }
    }

    private fun turnOn() {
        if (readEnabled()) return
        memory.write(
            memory.read().copy(
                previousEnabled = Settings.Secure.getInt(resolver, KEY_ENABLED, 0),
                previousMode = Settings.Secure.getInt(resolver, KEY_MODE, GrayscaleState.UNSET),
            ),
        )
        Settings.Secure.putInt(resolver, KEY_MODE, MODE_MONOCHROMACY)
        Settings.Secure.putInt(resolver, KEY_ENABLED, 1)
    }

    private fun turnOff() {
        val saved = memory.read()
        val ownMode = saved.previousMode != GrayscaleState.UNSET && saved.previousMode != MODE_MONOCHROMACY
        if (ownMode) Settings.Secure.putInt(resolver, KEY_MODE, saved.previousMode)
        Settings.Secure.putInt(resolver, KEY_ENABLED, if (ownMode && saved.previousEnabled == 1) 1 else 0)
        memory.write(saved.copy(previousEnabled = GrayscaleState.UNSET, previousMode = GrayscaleState.UNSET))
    }

    private fun readEnabled(): Boolean = try {
        Settings.Secure.getInt(resolver, KEY_ENABLED, 0) == 1 &&
            Settings.Secure.getInt(resolver, KEY_MODE, GrayscaleState.UNSET) == MODE_MONOCHROMACY
    } catch (e: SecurityException) {
        false
    }

    private companion object {
        /** Hidden `Settings.Secure` keys behind "Colour correction" in accessibility settings. */
        const val KEY_ENABLED = "accessibility_display_daltonizer_enabled"
        const val KEY_MODE = "accessibility_display_daltonizer"

        /** Daltonizer mode "monochromacy", i.e. grayscale. */
        const val MODE_MONOCHROMACY = 0
    }
}
