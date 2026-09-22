package dev.apn7.shunya.feature.settings.backup

import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.AppOverrides
import dev.apn7.shunya.core.model.FocusConfig
import dev.apn7.shunya.core.model.LauncherSettings
import dev.apn7.shunya.core.model.ProductLimits
import dev.apn7.shunya.feature.settings.backup.logic.BackupRules

/**
 * Brings restored values into the ranges the app relies on (a backup may be hand-edited or come
 * from another version). Applied right before a restore is written.
 */
internal object BackupSanitizer {

    /** Restored settings; onboarding is marked done so it doesn't start again after a restore. */
    fun settings(restored: LauncherSettings): LauncherSettings = restored.copy(
        appearance = restored.appearance.copy(
            wallpaperDimPercent = BackupRules.clamp(restored.appearance.wallpaperDimPercent, 0, ProductLimits.WALLPAPER_DIM_MAX),
        ),
        home = restored.home.copy(
            maxFavorites = BackupRules.clamp(restored.home.maxFavorites, 0, ProductLimits.FAVORITES_MAX),
        ),
        onboardingDone = true,
    )

    /** Only well-formed app ids survive; at most [ProductLimits.FAVORITES_MAX] favorites. */
    fun overrides(restored: AppOverrides): AppOverrides = restored.copy(
        customLabels = restored.customLabels.filter { (id, label) -> AppKey.fromId(id) != null && label.isNotBlank() },
        hidden = restored.hidden.filter { AppKey.fromId(it) != null }.toSet(),
        favorites = BackupRules.favoriteIds(restored.favorites.filter { AppKey.fromId(it) != null }, ProductLimits.FAVORITES_MAX),
    )

    /** Restored focus rules; the running session and today's extensions stay as they are now. */
    fun focus(restored: FocusConfig, current: FocusConfig): FocusConfig = restored.copy(
        defaultPauseSeconds = BackupRules.clamp(
            restored.defaultPauseSeconds,
            ProductLimits.PAUSE_SECONDS_MIN,
            ProductLimits.PAUSE_SECONDS_MAX,
        ),
        pauseSecondsByPackage = BackupRules.clampedValues(
            restored.pauseSecondsByPackage,
            ProductLimits.PAUSE_SECONDS_MIN,
            ProductLimits.PAUSE_SECONDS_MAX,
        ),
        dailyLimitMinutes = BackupRules.dailyLimits(restored.dailyLimitMinutes),
        extensions = current.extensions,
        session = current.session,
        schedules = restored.schedules
            .filter { it.id.isNotBlank() && BackupRules.isMinuteOfDay(it.startMinute) && BackupRules.isMinuteOfDay(it.endMinute) }
            .distinctBy { it.id }
            .map { it.copy(days = BackupRules.validDays(it.days)) },
        sessionDurationsMinutes = BackupRules.durations(restored.sessionDurationsMinutes, ProductLimits.FOCUS_DURATIONS_DEFAULT),
    )
}
