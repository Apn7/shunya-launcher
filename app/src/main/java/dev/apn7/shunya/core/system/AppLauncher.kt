package dev.apn7.shunya.core.system

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.UserHandle
import android.os.UserManager
import android.widget.Toast
import dev.apn7.shunya.R
import dev.apn7.shunya.core.contract.LaunchPolicy
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.LaunchDecision
import dev.apn7.shunya.core.model.LauncherApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The only way features open apps. [launch] asks the [LaunchPolicy] first and shows the
 * gate when the app is paused, limited or blocked; the gate itself uses [launchBypassingPolicy].
 * Handles work-profile apps through [LauncherApps], so every profile works the same way.
 */
class AppLauncher(
    context: Context,
    private val launchPolicy: LaunchPolicy,
    private val scope: CoroutineScope,
) {
    private val appContext = context.applicationContext
    private val launcherApps = appContext.getSystemService(LauncherApps::class.java)
    private val userManager = appContext.getSystemService(UserManager::class.java)

    /**
     * Opens [app] from Shunya's UI, honouring focus rules. Returns immediately; the decision and
     * the launch happen on the main thread a moment later. [sourceBounds] (optional) is the tapped
     * text's bounds on screen, used by the system for the opening animation.
     */
    fun launch(app: LauncherApp, sourceBounds: Rect? = null) {
        launch(app.key, app.displayLabel, sourceBounds)
    }

    /** Same as the [LauncherApp] overload, for callers that only store a key (gesture targets). */
    fun launch(app: AppKey, label: String, sourceBounds: Rect? = null) {
        scope.launch(Dispatchers.Main.immediate) {
            when (launchPolicy.decide(app)) {
                LaunchDecision.Allow -> start(app, sourceBounds)
                else -> openGate(app, label, GateContract.Source.Launcher)
            }
        }
    }

    /** Opens [app] without asking the policy: for the gate's "Open" button only. */
    fun launchBypassingPolicy(app: AppKey): Boolean = start(app, sourceBounds = null)

    /** Shows the gate for [app]; used by [launch] and by the accessibility service. */
    fun openGate(app: AppKey, label: String, source: GateContract.Source) {
        appContext.startSafely(GateContract.intent(appContext, app, label, source))
    }

    /** The system "App info" page for [app], in the right profile. */
    fun openAppInfo(app: AppKey): Boolean {
        val user = userFor(app) ?: return false
        return try {
            launcherApps.startAppDetailsActivity(componentOf(app), user, null, null)
            true
        } catch (e: SecurityException) {
            appContext.startSafely(SystemIntents.appDetails(app.packageName))
        } catch (e: ActivityNotFoundException) {
            appContext.startSafely(SystemIntents.appDetails(app.packageName))
        }
    }

    /** The profile [app] belongs to, or null when that profile no longer exists. */
    fun userFor(app: AppKey): UserHandle? = userManager.getUserForSerialNumber(app.userSerial)

    private fun start(app: AppKey, sourceBounds: Rect?): Boolean {
        val user = userFor(app)
        if (user == null) {
            showOpenError()
            return false
        }
        return try {
            launcherApps.startMainActivity(componentOf(app), user, sourceBounds, null)
            true
        } catch (e: ActivityNotFoundException) {
            showOpenError()
            false
        } catch (e: SecurityException) {
            // App disabled, profile paused or the activity no longer exported.
            showOpenError()
            false
        }
    }

    private fun showOpenError() {
        Toast.makeText(appContext, R.string.core_error_open_app, Toast.LENGTH_SHORT).show()
    }

    private fun componentOf(app: AppKey) = ComponentName(app.packageName, app.activityName)
}
