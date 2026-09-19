package dev.apn7.shunya.feature.focus.accessibility

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.SystemClock
import android.view.inputmethod.InputMethodManager
import dev.apn7.shunya.core.system.ShunyaComponents

/**
 * Tells real "an app came to the front" window changes apart from noise: the keyboard, the system
 * UI, launchers and recents, popups and dialogs of the app already in front, and the gate itself.
 * All lookups are cached, since window events arrive often.
 */
internal class ForegroundFilter(context: Context) {

    enum class Kind {
        /** Not an app switch: ignore it. */
        Ignore,

        /** Shunya's own home or settings came to the front. */
        Shunya,

        /** Another app's activity came to the front. */
        App,
    }

    private val appContext = context.applicationContext
    private val ownPackage = appContext.packageName
    private val activityCache = HashMap<String, Boolean>()
    private var ignoredPackages: Set<String> = emptySet()
    private var ignoredLoadedAt = 0L

    fun classify(packageName: String, className: String?): Kind {
        if (packageName == ownPackage) {
            return if (className == ShunyaComponents.GATE_ACTIVITY) Kind.Ignore else Kind.Shunya
        }
        if (packageName in ignored()) return Kind.Ignore
        return if (isActivity(packageName, className)) Kind.App else Kind.Ignore
    }

    /** True when [className] is one of [packageName]'s activities (not a dialog, popup or toast window). */
    private fun isActivity(packageName: String, className: String?): Boolean {
        if (className.isNullOrEmpty()) return true
        val key = "$packageName/$className"
        val cached = activityCache[key]
        if (cached != null) return cached
        val result = try {
            @Suppress("DEPRECATION")
            appContext.packageManager.getActivityInfo(ComponentName(packageName, className), 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        if (activityCache.size >= CACHE_LIMIT) activityCache.clear()
        activityCache[key] = result
        return result
    }

    /** System UI, keyboards and home apps (their windows are never "an app the user opened"). */
    private fun ignored(): Set<String> {
        val now: Long = SystemClock.elapsedRealtime()
        if (ignoredLoadedAt == 0L || now - ignoredLoadedAt > IGNORED_REFRESH_MILLIS) {
            ignoredPackages = loadIgnored()
            ignoredLoadedAt = now
        }
        return ignoredPackages
    }

    private fun loadIgnored(): Set<String> {
        val result = HashSet<String>()
        result.add(SYSTEM_UI)
        try {
            val inputMethods = appContext.getSystemService(InputMethodManager::class.java)?.enabledInputMethodList.orEmpty()
            for (info in inputMethods) result.add(info.packageName)
        } catch (e: RuntimeException) {
            // Keyboards are only a filter optimisation.
        }
        try {
            val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            @Suppress("DEPRECATION")
            val homes: List<ResolveInfo> = appContext.packageManager.queryIntentActivities(home, PackageManager.MATCH_DEFAULT_ONLY)
            for (info in homes) result.add(info.activityInfo.packageName)
        } catch (e: RuntimeException) {
            // Launchers are only a filter optimisation.
        }
        return result
    }

    private companion object {
        const val SYSTEM_UI = "com.android.systemui"
        const val CACHE_LIMIT = 256
        const val IGNORED_REFRESH_MILLIS = 10 * 60 * 1000L
    }
}
