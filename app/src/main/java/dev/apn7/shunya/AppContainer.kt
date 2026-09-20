package dev.apn7.shunya

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import dev.apn7.shunya.core.contract.FocusController
import dev.apn7.shunya.core.contract.GrayscaleController
import dev.apn7.shunya.core.contract.LaunchPolicy
import dev.apn7.shunya.core.contract.NotificationInbox
import dev.apn7.shunya.core.contract.UsageRepository
import dev.apn7.shunya.core.data.AppOverridesRepository
import dev.apn7.shunya.core.data.AppsRepository
import dev.apn7.shunya.core.data.FocusConfigRepository
import dev.apn7.shunya.core.data.InstalledAppsCache
import dev.apn7.shunya.core.data.LauncherAppsRepository
import dev.apn7.shunya.core.data.SettingsRepository
import dev.apn7.shunya.core.data.jsonDataStore
import dev.apn7.shunya.core.model.AppOverrides
import dev.apn7.shunya.core.model.FocusConfig
import dev.apn7.shunya.core.model.LauncherSettings
import dev.apn7.shunya.core.system.AppLauncher
import dev.apn7.shunya.core.system.HomeEvents
import dev.apn7.shunya.core.system.PermissionsRepository
import dev.apn7.shunya.core.system.SystemActions
import dev.apn7.shunya.feature.focus.grayscale.SecureSettingsGrayscale
import dev.apn7.shunya.feature.focus.policy.FocusLaunchPolicy
import dev.apn7.shunya.feature.focus.session.FocusSessionController
import dev.apn7.shunya.feature.focus.usage.UsageStatsRepository
import dev.apn7.shunya.feature.notifications.inbox.HeldNotificationInbox
import kotlinx.coroutines.CoroutineScope

/**
 * Manual dependency injection: one instance of everything, created on first use.
 *
 * Features depend on the interfaces declared here (from `core/`), never on another feature's
 * classes. Each feature wires its implementations inside its own `region` below;
 * the regions are far apart so parallel branches merge without conflicts.
 *
 * Get it in composables with `LocalAppContainer.current`, elsewhere with `context.appContainer`.
 */
class AppContainer(context: Context, val appScope: CoroutineScope) {

    val appContext: Context = context.applicationContext

    // Persistence. Settings, overrides and the app list are created eagerly: the first frame needs them.

    val settingsRepository: SettingsRepository =
        SettingsRepository(appContext.jsonDataStore("settings.json", LauncherSettings.serializer(), LauncherSettings()), appScope)

    val appOverridesRepository: AppOverridesRepository =
        AppOverridesRepository(appContext.jsonDataStore("app_overrides.json", AppOverrides.serializer(), AppOverrides()), appScope)

    val appsRepository: AppsRepository = LauncherAppsRepository(
        context = appContext,
        cache = appContext.jsonDataStore("apps_cache.json", InstalledAppsCache.serializer(), InstalledAppsCache()),
        overridesRepository = appOverridesRepository,
        settingsRepository = settingsRepository,
        scope = appScope,
    )

    val focusConfigRepository: FocusConfigRepository by lazy {
        FocusConfigRepository(appContext.jsonDataStore("focus_config.json", FocusConfig.serializer(), FocusConfig()), appScope)
    }

    // System.

    val permissionsRepository: PermissionsRepository by lazy { PermissionsRepository(appContext) }

    val systemActions: SystemActions by lazy { SystemActions(appContext) }

    val homeEvents: HomeEvents = HomeEvents()

    val appLauncher: AppLauncher by lazy { AppLauncher(appContext, launchPolicy, appScope) }

    // region home — home, drawer, search. Add feature singletons here if needed.
    // endregion home

    // ------------------------------------------------------------------------------------------
    // Focus region below: launch policy, focus, usage, grayscale.
    // Notifications region further below: notification inbox.
    // Keep every edit inside its region; the lines between regions never change.
    // ------------------------------------------------------------------------------------------

    // region focus — focus & wellbeing
    private val secureSettingsGrayscale: SecureSettingsGrayscale by lazy { SecureSettingsGrayscale(appContext, appScope) }

    private val focusSessionController: FocusSessionController by lazy {
        FocusSessionController(
            configRepository = focusConfigRepository,
            scope = appScope,
            onScheduleGrayscale = { wanted -> secureSettingsGrayscale.applySchedule(wanted) },
        )
    }

    val launchPolicy: LaunchPolicy by lazy {
        FocusLaunchPolicy(
            configRepository = focusConfigRepository,
            usageRepository = usageRepository,
            focusNow = { focusSessionController.statusNow() },
        )
    }

    val focusController: FocusController by lazy { focusSessionController }

    val usageRepository: UsageRepository by lazy { UsageStatsRepository(appContext) }

    val grayscaleController: GrayscaleController by lazy { secureSettingsGrayscale }
    // endregion focus

    // ------------------------------------------------------------------------------------------
    // Notifications region below. Focus region is above.
    // Keep every edit inside its region; the lines between regions never change.
    // ------------------------------------------------------------------------------------------

    // region notifications — notifications, settings
    val notificationInbox: NotificationInbox by lazy { HeldNotificationInbox() }
    // endregion notifications
}

/** The [AppContainer] in composition, provided by `MainActivity` (and the gate). */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("LocalAppContainer is not provided: wrap the content in CompositionLocalProvider(LocalAppContainer provides container)")
}
