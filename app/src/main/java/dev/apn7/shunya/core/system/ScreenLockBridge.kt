package dev.apn7.shunya.core.system

import android.accessibilityservice.AccessibilityService
import android.os.Build

/**
 * Lets the UI ask Shunya's accessibility service for global actions.
 *
 * The service calls [register] in `onServiceConnected` with `{ action -> performGlobalAction(action) }`
 * and MUST call [unregister] in `onUnbind`/`onDestroy` (the object would otherwise keep the dead
 * service alive). Everything here is a no-op returning false while the service is off.
 */
object ScreenLockBridge {

    @Volatile
    private var performer: ((Int) -> Boolean)? = null

    fun register(performGlobalAction: (Int) -> Boolean) {
        performer = performGlobalAction
    }

    fun unregister() {
        performer = null
    }

    /** True while the accessibility service is running and connected. */
    val isConnected: Boolean get() = performer != null

    /** Locks the screen (Android 9+). False when the service is off or the OS is too old. */
    fun lockScreen(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        return performer?.invoke(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN) ?: false
    }

    /** Opens the notification shade through the service (fallback for [SystemActions]). */
    fun openNotifications(): Boolean =
        performer?.invoke(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS) ?: false
}
