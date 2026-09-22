package dev.apn7.shunya.feature.home.actions

import android.content.Context
import android.widget.Toast
import dev.apn7.shunya.AppContainer
import dev.apn7.shunya.R
import dev.apn7.shunya.core.model.AppUsage
import dev.apn7.shunya.core.model.LaunchDecision
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.core.model.ProductLimits
import dev.apn7.shunya.core.system.SystemIntents
import dev.apn7.shunya.core.system.startSafely
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Daily limit presets offered in the action sheet, minutes (0 = off). */
internal val DailyLimitPresets: List<Int> = listOf(0) + ProductLimits.DAILY_LIMIT_PRESETS_MINUTES

/**
 * Everything the app action sheet can do to one app (PRD 3.2). Writes go through the core
 * repositories, so Settings and Focus screens see the same data.
 */
internal class AppActionsController(
    private val context: Context,
    private val container: AppContainer,
) {
    private val shortcuts = AppShortcuts(context)

    fun rename(app: LauncherApp, label: String?) {
        container.appOverridesRepository.edit { it.withLabel(app.key, label) }
    }

    /** Adds to or removes from home; adding is a no-op when home is full (the sheet disables it). */
    fun setFavorite(app: LauncherApp, favorite: Boolean) {
        val max = container.settingsRepository.settings.value.home.maxFavorites
        container.appOverridesRepository.edit { it.withFavorite(app.key, favorite, max) }
        if (favorite) toast(context.getString(R.string.apps_home_added, app.displayLabel))
    }

    /** Hides the app from drawer and search (also removes it from home). */
    fun hide(app: LauncherApp) {
        container.appOverridesRepository.edit { it.withHidden(app.key, hidden = true) }
        toast(context.getString(R.string.apps_hidden_toast, app.displayLabel))
    }

    fun setDistracting(app: LauncherApp, distracting: Boolean) {
        val pkg = app.packageName
        container.focusConfigRepository.edit {
            it.copy(distractingPackages = if (distracting) it.distractingPackages + pkg else it.distractingPackages - pkg)
        }
    }

    /** Sets the daily limit in minutes; 0 removes it. */
    fun setDailyLimit(app: LauncherApp, minutes: Int) {
        val pkg = app.packageName
        container.focusConfigRepository.edit {
            it.copy(dailyLimitMinutes = if (minutes <= 0) it.dailyLimitMinutes - pkg else it.dailyLimitMinutes + (pkg to minutes))
        }
    }

    fun openAppInfo(app: LauncherApp) {
        container.appLauncher.openAppInfo(app.key)
    }

    /** System uninstall dialog; work-profile apps name their profile so the right copy is removed. */
    fun uninstall(app: LauncherApp) {
        val user = if (app.isWork) container.appLauncher.userFor(app.key) else null
        context.startSafely(SystemIntents.uninstall(app.packageName, user))
    }

    /** Today's usage, or null without usage access. */
    suspend fun usageToday(app: LauncherApp): AppUsage? {
        val usage = container.usageRepository
        return if (usage.hasAccess()) usage.appToday(app.packageName) else null
    }

    /** The app's shortcuts (empty unless Shunya is the default launcher). */
    suspend fun shortcutsOf(app: LauncherApp): List<AppShortcut> {
        val user = container.appLauncher.userFor(app.key) ?: return emptyList()
        return withContext(Dispatchers.IO) { shortcuts.query(app.key, user) }
    }

    /**
     * Opens [shortcut], but only when focus rules allow the app right now; otherwise the normal
     * launch shows the gate (pause, limit or blocked), exactly like tapping the app.
     */
    fun openShortcut(app: LauncherApp, shortcut: AppShortcut) {
        container.appScope.launch(Dispatchers.Main.immediate) {
            val allowed = container.launchPolicy.decide(app.key) == LaunchDecision.Allow
            if (!allowed || !shortcuts.start(shortcut)) container.appLauncher.launch(app)
        }
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
