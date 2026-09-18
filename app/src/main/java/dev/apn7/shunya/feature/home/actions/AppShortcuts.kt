package dev.apn7.shunya.feature.home.actions

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.UserHandle
import androidx.compose.runtime.Immutable
import dev.apn7.shunya.core.model.AppKey

/** One app shortcut ("New message", "Scan QR code"), text only. */
@Immutable
class AppShortcut internal constructor(
    val id: String,
    val label: String,
    internal val info: ShortcutInfo,
)

/**
 * App shortcuts through [LauncherApps] (P1). The system only shares them with the default home
 * app, so everything here returns nothing until Shunya is the default launcher. All shortcut APIs
 * used exist since Android 7.1 (API 25), below our minSdk 26.
 */
internal class AppShortcuts(context: Context) {

    private val launcherApps: LauncherApps? = context.applicationContext.getSystemService(LauncherApps::class.java)

    /** Up to [MAX_SHORTCUTS] enabled shortcuts of [app] (manifest ones first), or empty. Does binder I/O: call off the main thread. */
    fun query(app: AppKey, user: UserHandle): List<AppShortcut> {
        val apps = launcherApps ?: return emptyList()
        return try {
            if (!apps.hasShortcutHostPermission()) return emptyList()
            val query = LauncherApps.ShortcutQuery()
                .setPackage(app.packageName)
                .setActivity(ComponentName(app.packageName, app.activityName))
                .setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC)
            apps.getShortcuts(query, user).orEmpty()
                .filter { it.isEnabled }
                .sortedWith(compareBy<ShortcutInfo>({ if (it.isDeclaredInManifest) 0 else 1 }, { it.rank }))
                .map { info -> AppShortcut(info.id, (info.shortLabel ?: info.longLabel)?.toString().orEmpty(), info) }
                .filter { it.label.isNotBlank() }
                .take(MAX_SHORTCUTS)
        } catch (e: SecurityException) {
            emptyList() // Lost the default-launcher role meanwhile.
        } catch (e: IllegalStateException) {
            emptyList() // The profile is locked or being removed.
        }
    }

    /** Starts [shortcut]; false when the app or the system refuses. */
    fun start(shortcut: AppShortcut): Boolean {
        val apps = launcherApps ?: return false
        return try {
            apps.startShortcut(shortcut.info, null, null)
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        } catch (e: IllegalStateException) {
            false
        }
    }

    private companion object {
        const val MAX_SHORTCUTS = 4
    }
}
