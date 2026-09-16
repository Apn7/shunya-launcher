package dev.apn7.shunya.core.contract

import kotlinx.coroutines.flow.StateFlow

/**
 * Whole-screen grayscale through the system colour-correction setting.
 * Works only after `WRITE_SECURE_SETTINGS` was granted once over ADB ([adbGrantCommand]).
 */
interface GrayscaleController {

    /** True when Shunya may change the setting (permission granted). */
    fun isAvailable(): Boolean

    /** Whether grayscale is on right now (as the system reports it). */
    val isEnabled: StateFlow<Boolean>

    /** Turns grayscale on or off. Returns false (and changes nothing) when not [isAvailable]. */
    fun setEnabled(enabled: Boolean): Boolean

    /** The exact command the user runs once on a computer, shown with a copy button. */
    val adbGrantCommand: String
}
