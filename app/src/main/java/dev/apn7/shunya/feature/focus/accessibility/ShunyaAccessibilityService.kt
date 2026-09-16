package dev.apn7.shunya.feature.focus.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import dev.apn7.shunya.core.system.ScreenLockBridge

/**
 * Opt-in accessibility service: lock screen on double-tap and system-wide blocking.
 * Stub behaviour: only registers the lock-screen bridge.
 */
class ShunyaAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        ScreenLockBridge.register { action -> performGlobalAction(action) }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        ScreenLockBridge.unregister()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        ScreenLockBridge.unregister()
        super.onDestroy()
    }
}
