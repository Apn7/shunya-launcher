package dev.apn7.shunya.feature.notifications

import android.app.Notification
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.UserManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import dev.apn7.shunya.appContainer
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.HeldNotification
import dev.apn7.shunya.core.model.NotificationFilterMode
import dev.apn7.shunya.core.model.NotificationPrefs
import dev.apn7.shunya.feature.notifications.logic.HoldDecision
import dev.apn7.shunya.feature.notifications.logic.HoldRules
import dev.apn7.shunya.feature.notifications.logic.HoldSettings
import dev.apn7.shunya.feature.notifications.logic.NotificationFacts
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Longest text kept per held notification (the inbox shows two lines). */
private const val MAX_TEXT_LENGTH = 400

/**
 * Opt-in notification filter (PRD 3.4). In Hold mode, notifications from apps that are not on the
 * allowed list are removed from the shade and saved to the Inbox; ongoing, foreground-service,
 * group-summary, call/alarm and media notifications, and Shunya's own, are never touched (see
 * [HoldRules]). Also sweeps the shade when it connects and whenever the filter settings change.
 *
 * Runs in the app process, so it shares the [dev.apn7.shunya.AppContainer]. Every system call is
 * guarded: access can be revoked at any moment, and a listener must never crash the launcher.
 */
class NotificationFilterService : NotificationListenerService() {

    private var scope: CoroutineScope? = null

    /** Built-in allowed packages, resolved once per connection. */
    @Volatile
    private var defaults: Set<String>? = null

    override fun onListenerConnected() {
        super.onListenerConnected()
        defaults = DefaultAllowedApps.resolve(this)
        scope?.cancel()
        val connected = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        scope = connected
        connected.launch {
            appContainer.settingsRepository.settings
                .map { it.notifications }
                .distinctUntilChanged()
                .collect { prefs -> if (prefs.filterMode == NotificationFilterMode.Hold) sweep() }
        }
    }

    override fun onListenerDisconnected() {
        stopWatching()
        super.onListenerDisconnected()
        // Ask to be bound again (e.g. after the system trimmed us). Ignored when access was revoked.
        safely { NotificationListenerService.requestRebind(ComponentName(this, NotificationFilterService::class.java)) }
    }

    override fun onDestroy() {
        stopWatching()
        super.onDestroy()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        safely { filter(sbn, appContainer.settingsRepository.settings.value.notifications) }
    }

    private fun stopWatching() {
        scope?.cancel()
        scope = null
    }

    /** Applies the filter to everything already in the shade. */
    private fun sweep() {
        val prefs = appContainer.settingsRepository.settings.value.notifications
        var active: Array<StatusBarNotification>? = null
        safely { active = activeNotifications }
        active?.forEach { sbn -> safely { filter(sbn, prefs) } }
    }

    private fun filter(sbn: StatusBarNotification, prefs: NotificationPrefs) {
        val settings = HoldSettings(
            holdEnabled = prefs.filterMode == NotificationFilterMode.Hold,
            allowedPackages = HoldRules.effectiveAllowed(prefs.allowedPackages, builtInDefaults()),
            ownPackage = packageName,
        )
        if (HoldRules.decide(factsOf(sbn), settings) != HoldDecision.Hold) return
        cancelNotification(sbn.key)
        appContainer.heldNotificationInbox.hold(heldFrom(sbn), sbn.notification?.contentIntent)
    }

    private fun builtInDefaults(): Set<String> =
        defaults ?: DefaultAllowedApps.resolve(this).also { defaults = it }

    private fun factsOf(sbn: StatusBarNotification): NotificationFacts {
        val notification: Notification? = sbn.notification
        val flags = notification?.flags ?: 0
        val category = notification?.category
        return NotificationFacts(
            packageName = sbn.packageName,
            isOngoing = sbn.isOngoing || (flags and Notification.FLAG_ONGOING_EVENT) != 0,
            isClearable = sbn.isClearable,
            isForegroundService = (flags and Notification.FLAG_FOREGROUND_SERVICE) != 0,
            isGroupSummary = (flags and Notification.FLAG_GROUP_SUMMARY) != 0,
            isTimeCritical = category == Notification.CATEGORY_CALL ||
                category == Notification.CATEGORY_ALARM ||
                notification?.fullScreenIntent != null,
            isMedia = category == Notification.CATEGORY_TRANSPORT ||
                notification?.extras?.containsKey(Notification.EXTRA_MEDIA_SESSION) == true,
        )
    }

    private fun heldFrom(sbn: StatusBarNotification): HeldNotification {
        val notification: Notification? = sbn.notification
        val extras = notification?.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (
            extras?.getCharSequence(Notification.EXTRA_TEXT)
                ?: extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: notification?.tickerText
            )?.toString().orEmpty()
        val serial = serialOf(sbn)
        return HeldNotification(
            key = sbn.key,
            packageName = sbn.packageName,
            appLabel = labelOf(sbn.packageName, serial),
            title = title.trim().take(MAX_TEXT_LENGTH),
            text = text.trim().take(MAX_TEXT_LENGTH),
            postedAt = sbn.postTime,
            userSerial = serial,
        )
    }

    private fun serialOf(sbn: StatusBarNotification): Long {
        val serial = getSystemService(UserManager::class.java)?.getSerialNumberForUser(sbn.user) ?: -1L
        return if (serial >= 0L) serial else AppKey.MAIN_USER_SERIAL
    }

    /** The label Shunya shows for the app (custom label included), else the system's, else the package. */
    private fun labelOf(packageName: String, serial: Long): String {
        val candidates = appContainer.appsRepository.allApps.value.filter { it.packageName == packageName }
        val app = candidates.firstOrNull { it.key.userSerial == serial } ?: candidates.firstOrNull()
        if (app != null) return app.displayLabel
        return try {
            @Suppress("DEPRECATION") // getApplicationInfo(String, Int): the flags overload needs API 33.
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    /**
     * Runs [block] and swallows runtime errors: the listener calls can throw (for example a
     * SecurityException when access is revoked mid-call), and the launcher process must survive.
     */
    private inline fun safely(block: () -> Unit) {
        try {
            block()
        } catch (e: RuntimeException) {
            // Nothing sensible to do; the next notification or connection tries again.
        }
    }
}
