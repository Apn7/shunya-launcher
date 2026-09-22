package dev.apn7.shunya.core.system

import android.app.Activity
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.UserHandle
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.Settings
import android.widget.Toast
import dev.apn7.shunya.R

/**
 * Intents for everything Shunya hands off to the system or other apps. Open them with
 * [startSafely]; for per-app screens that must respect work profiles use `AppLauncher`.
 */
object SystemIntents {

    /** The home screen (Shunya when it is the default launcher), e.g. for the gate's "Not now". */
    fun home(): Intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)

    /** The system alarm list (tap on the home clock). */
    fun showAlarms(): Intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)

    /** The calendar app at today (tap on the home date). */
    fun calendarToday(nowMillis: Long = System.currentTimeMillis()): Intent {
        val uri = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
        ContentUris.appendId(uri, nowMillis)
        return Intent(Intent.ACTION_VIEW).setData(uri.build())
    }

    /** "Search the web for …" in the default search/browser app. */
    fun webSearch(query: String): Intent = Intent(Intent.ACTION_WEB_SEARCH).putExtra(SearchManager.QUERY, query)

    /** "Search Play Store for …". */
    fun playStoreSearch(query: String): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=" + Uri.encode(query)))

    /** System "App info" page for [packageName] (personal profile; prefer `AppLauncher.openAppInfo`). */
    fun appDetails(packageName: String): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri(packageName))

    /** System uninstall dialog for [packageName]; pass the work profile's [user] for work apps. */
    fun uninstall(packageName: String, user: UserHandle? = null): Intent {
        val intent = Intent(Intent.ACTION_DELETE, packageUri(packageName))
        if (user != null) intent.putExtra(Intent.EXTRA_USER, user)
        return intent
    }

    /** System screen to pick the default home app (fallback when RoleManager is unavailable). */
    fun homeSettings(): Intent = Intent(Settings.ACTION_HOME_SETTINGS)

    /** Usage access list, opened on Shunya's entry where the OS supports it. */
    fun usageAccessSettings(context: Context): List<Intent> = listOf(
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS, packageUri(context.packageName)),
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
    )

    /** Notification access for Shunya's listener (Android 11+ opens its detail page directly). */
    fun notificationListenerSettings(context: Context): List<Intent> {
        val generic = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return listOf(generic)
        val detail = Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).putExtra(
            Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
            ShunyaComponents.notificationListener(context).flattenToString(),
        )
        return listOf(detail, generic)
    }

    /**
     * The system accessibility settings list, where the user turns on "Shunya focus". (The per-service
     * detail page is a system-only API, so apps can't open it directly.)
     */
    fun accessibilitySettings(): Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

    /** The system wallpaper picker chooser. */
    fun setWallpaper(): Intent = Intent.createChooser(Intent(Intent.ACTION_SET_WALLPAPER), null)

    private fun packageUri(packageName: String): Uri = Uri.fromParts("package", packageName, null)
}

/**
 * Starts [intent]; returns false (after a short toast) when no app can handle it or the target
 * refuses. Adds NEW_TASK when called with a non-activity context.
 */
fun Context.startSafely(intent: Intent): Boolean = startFirstAvailable(listOf(intent))

/**
 * Starts the first of [intents] that works (e.g. a detail settings page, then the generic list).
 * Shows a toast and returns false when none does.
 */
fun Context.startFirstAvailable(intents: List<Intent>): Boolean {
    for (intent in intents) {
        if (this !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
            return true
        } catch (e: ActivityNotFoundException) {
            // Try the next, more generic intent.
        } catch (e: SecurityException) {
            // Target not exported to us on this ROM; try the next one.
        }
    }
    Toast.makeText(this, R.string.core_error_no_handler, Toast.LENGTH_SHORT).show()
    return false
}
