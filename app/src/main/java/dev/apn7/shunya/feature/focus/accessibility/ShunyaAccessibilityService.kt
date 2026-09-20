package dev.apn7.shunya.feature.focus.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import dev.apn7.shunya.appContainer
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.BlockingMode
import dev.apn7.shunya.core.model.LaunchDecision
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.core.system.GateContract
import dev.apn7.shunya.core.system.ScreenLockBridge
import dev.apn7.shunya.feature.focus.gate.GatePasses
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Opt-in accessibility service (shown to the user only after the prominent disclosure):
 * 1. lock screen on double-tap through [ScreenLockBridge] (`GLOBAL_ACTION_LOCK_SCREEN`, Android 9+);
 * 2. system-wide blocking: when blocking mode is [BlockingMode.SystemWide] and an app that is
 *    blocked (focus) or over its daily limit comes to the front from anywhere, the gate opens.
 *
 * It only looks at which app a window-state change belongs to (`canRetrieveWindowContent` is false).
 */
class ShunyaAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var filter: ForegroundFilter? = null
    private var pendingCheck: Job? = null
    private var lastGatedPackage: String? = null
    private var lastGatedAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        ScreenLockBridge.register { action -> performGlobalAction(action) }
        filter = ForegroundFilter(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString() ?: return
        val kind = filter?.classify(packageName, event.className?.toString()) ?: return
        when (kind) {
            ForegroundFilter.Kind.Ignore -> Unit
            ForegroundFilter.Kind.Shunya -> {
                GatePasses.onForeground(packageName)
                pendingCheck?.cancel()
            }
            ForegroundFilter.Kind.App -> {
                GatePasses.onForeground(packageName)
                checkForeground(packageName)
            }
        }
    }

    /** Opens the gate when [packageName] is blocked or over its limit and system-wide blocking is on. */
    private fun checkForeground(packageName: String) {
        pendingCheck?.cancel()
        val container = applicationContext.appContainer
        if (container.settingsRepository.settings.value.blockingMode != BlockingMode.SystemWide) return
        if (GatePasses.has(packageName)) return
        val config = container.focusConfigRepository.config.value
        if (!config.isDistracting(packageName) && config.limitMinutesFor(packageName) == null) return
        val now: Long = SystemClock.elapsedRealtime()
        if (packageName == lastGatedPackage && now - lastGatedAt < DEBOUNCE_MILLIS) return
        val app = launchableApp(container.appsRepository.allApps.value, packageName) ?: return
        pendingCheck = scope.launch {
            val decision = container.launchPolicy.decide(app.key)
            if (decision is LaunchDecision.Blocked || decision is LaunchDecision.LimitReached) {
                lastGatedPackage = packageName
                lastGatedAt = SystemClock.elapsedRealtime()
                container.appLauncher.openGate(app.key, app.displayLabel, GateContract.Source.SystemWide)
            }
        }
    }

    /** The app's launcher entry (personal profile first), needed for the gate's "Open". */
    private fun launchableApp(apps: List<LauncherApp>, packageName: String): LauncherApp? {
        val candidates = apps.filter { it.packageName == packageName }
        return candidates.firstOrNull { it.key.userSerial == AppKey.MAIN_USER_SERIAL } ?: candidates.firstOrNull()
    }

    override fun onInterrupt() = Unit

    override fun onUnbind(intent: Intent?): Boolean {
        ScreenLockBridge.unregister()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        ScreenLockBridge.unregister()
        scope.cancel()
        super.onDestroy()
    }

    private companion object {
        /** A second window change of the same app right after its gate opened is not a new visit. */
        const val DEBOUNCE_MILLIS = 1_500L
    }
}
