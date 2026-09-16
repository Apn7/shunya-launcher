package dev.apn7.shunya.feature.focus.grayscale

import android.content.Context
import dev.apn7.shunya.core.contract.GrayscaleController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** [GrayscaleController] over the secure colour-correction settings. Stub behaviour: unavailable. */
class SecureSettingsGrayscale(context: Context) : GrayscaleController {

    override val adbGrantCommand: String =
        "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"

    override val isEnabled: StateFlow<Boolean> = MutableStateFlow(false)

    override fun isAvailable(): Boolean = false

    override fun setEnabled(enabled: Boolean): Boolean = false
}
