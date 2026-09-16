package dev.apn7.shunya.core.system

import android.annotation.SuppressLint
import android.content.Context

/** Device actions triggered from home gestures and the quick menu. */
class SystemActions(context: Context) {

    private val appContext = context.applicationContext

    /**
     * Pulls down the notification shade. Uses the long-standing `StatusBarManager` API that every
     * third-party launcher relies on (EXPAND_STATUS_BAR permission), falling back to the
     * accessibility service when the ROM refuses. Returns false when neither works.
     */
    @SuppressLint("WrongConstant")
    fun expandNotificationShade(): Boolean {
        val expanded = try {
            val statusBar = appContext.getSystemService("statusbar")
            if (statusBar == null) {
                false
            } else {
                Class.forName("android.app.StatusBarManager").getMethod("expandNotificationsPanel").invoke(statusBar)
                true
            }
        } catch (e: Exception) {
            // Reflection blocked or method missing on this ROM.
            false
        }
        return expanded || ScreenLockBridge.openNotifications()
    }

    /** Locks the screen through the accessibility service; false when the service is off. */
    fun lockScreen(): Boolean = ScreenLockBridge.lockScreen()

    /** True when [lockScreen] can work right now. */
    val canLockScreen: Boolean get() = ScreenLockBridge.isConnected
}
