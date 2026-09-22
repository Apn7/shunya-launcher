package dev.apn7.shunya.feature.notifications.inbox

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import dev.apn7.shunya.R
import dev.apn7.shunya.core.contract.LaunchPolicy
import dev.apn7.shunya.core.contract.NotificationInbox
import dev.apn7.shunya.core.data.AppsRepository
import dev.apn7.shunya.core.model.HeldNotification
import dev.apn7.shunya.core.model.LaunchDecision
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.core.model.ProductLimits
import dev.apn7.shunya.core.system.AppLauncher
import dev.apn7.shunya.feature.notifications.logic.InboxRules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

private typealias InboxEdit = (List<HeldNotification>) -> List<HeldNotification>

private val KeyOf: (HeldNotification) -> String = { it.key }
private val TimeOf: (HeldNotification) -> Long = { it.postedAt }

/**
 * [NotificationInbox] persisted in its own DataStore (`notification_inbox.json`, not part of
 * backups). `NotificationFilterService` adds items with [hold].
 *
 * Content intents cannot be persisted, so they live in memory only: while the process lives,
 * tapping an item opens exactly what the notification pointed to; after a restart it opens the app.
 * Edits are applied one at a time, in the order they were made (a single consumer coroutine).
 */
class HeldNotificationInbox(
    context: Context,
    private val store: DataStore<List<HeldNotification>>,
    private val scope: CoroutineScope,
    private val appsRepository: AppsRepository,
    private val appLauncher: AppLauncher,
    private val launchPolicy: LaunchPolicy,
    private val maxItems: Int = ProductLimits.INBOX_MAX_ITEMS,
) : NotificationInbox {

    private val appContext = context.applicationContext
    private val contentIntents = ConcurrentHashMap<String, PendingIntent>()
    private val edits = Channel<InboxEdit>(Channel.UNLIMITED)

    override val items: StateFlow<List<HeldNotification>> = store.data
        .catch { error -> if (error is IOException) emit(emptyList()) else throw error }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val count: StateFlow<Int> = items
        .map { it.size }
        .stateIn(scope, SharingStarted.Eagerly, 0)

    init {
        scope.launch {
            for (edit in edits) apply(edit)
        }
    }

    /** Saves [item] (replacing an older version with the same key) and remembers its [contentIntent]. */
    fun hold(item: HeldNotification, contentIntent: PendingIntent?) {
        if (contentIntent != null) contentIntents[item.key] = contentIntent else contentIntents.remove(item.key)
        enqueue { list -> InboxRules.added(list, item, maxItems, KeyOf, TimeOf) }
    }

    override fun dismiss(key: String) {
        contentIntents.remove(key)
        enqueue { list -> InboxRules.without(list, key, KeyOf) }
    }

    override fun clearAll() {
        contentIntents.clear()
        enqueue { emptyList() }
    }

    /**
     * Removes [item] and opens it. Focus rules still apply: when the app is paused, limited or
     * blocked, it goes through [AppLauncher] (which shows the gate) instead of the content intent.
     */
    override fun open(item: HeldNotification) {
        val contentIntent = contentIntents[item.key]
        dismiss(item.key)
        scope.launch(Dispatchers.Main.immediate) {
            val app = launchableApp(item)
            val allowed = app == null || launchPolicy.decide(app.key) == LaunchDecision.Allow
            val opened = allowed && contentIntent != null && send(contentIntent)
            when {
                opened -> Unit
                app != null -> appLauncher.launch(app)
                else -> Toast.makeText(appContext, R.string.core_error_open_app, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enqueue(edit: InboxEdit) {
        edits.trySend(edit) // Unlimited channel: never refuses while the process lives.
    }

    private suspend fun apply(edit: InboxEdit) {
        var dropped: Set<String> = emptySet()
        try {
            store.updateData { current ->
                val next = edit(current)
                dropped = current.map(KeyOf).toSet() - next.map(KeyOf).toSet()
                next
            }
        } catch (e: IOException) {
            return // Disk full or similar: the inbox keeps its previous content.
        }
        dropped.forEach { contentIntents.remove(it) }
    }

    /** The app that posted [item], preferably in the same profile. */
    private fun launchableApp(item: HeldNotification): LauncherApp? {
        val candidates = appsRepository.allApps.value.filter { it.packageName == item.packageName }
        return candidates.firstOrNull { it.key.userSerial == item.userSerial } ?: candidates.firstOrNull()
    }

    private fun send(intent: PendingIntent): Boolean =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                intent.send(allowBackgroundStart())
            } else {
                intent.send()
            }
            true
        } catch (e: PendingIntent.CanceledException) {
            false // The app withdrew it (updated or cancelled the notification).
        } catch (e: SecurityException) {
            false
        }

    /**
     * Android 14+: we send the intent while the inbox is on screen, so we lend it our right to
     * start an activity; without this opt-in the system may silently block the launch.
     */
    @Suppress("DEPRECATION") // MODE_BACKGROUND_ACTIVITY_START_ALLOWED: deprecated in API 36, still honoured.
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun allowBackgroundStart(): Bundle {
        val options = ActivityOptions.makeBasic()
        options.setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
        return options.toBundle()
    }
}
