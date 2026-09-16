package dev.apn7.shunya.core.system

import android.content.ComponentName
import android.content.Context

/**
 * Manifest components that core code must address without depending on feature packages.
 * The class names are fixed by `AndroidManifest.xml`; do not move these classes.
 */
object ShunyaComponents {
    const val GATE_ACTIVITY = "dev.apn7.shunya.feature.focus.gate.GateActivity"
    const val ACCESSIBILITY_SERVICE = "dev.apn7.shunya.feature.focus.accessibility.ShunyaAccessibilityService"
    const val NOTIFICATION_LISTENER = "dev.apn7.shunya.feature.notifications.NotificationFilterService"

    fun gateActivity(context: Context): ComponentName = ComponentName(context, GATE_ACTIVITY)

    fun accessibilityService(context: Context): ComponentName = ComponentName(context, ACCESSIBILITY_SERVICE)

    fun notificationListener(context: Context): ComponentName = ComponentName(context, NOTIFICATION_LISTENER)
}
