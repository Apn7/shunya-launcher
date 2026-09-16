package dev.apn7.shunya.core.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.AppOverrides
import dev.apn7.shunya.core.model.LauncherApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.Collator
import java.util.Locale

/**
 * [AppsRepository] backed by [LauncherApps] (all user profiles), with a disk cache for an instant
 * cold start and live updates from [LauncherApps.Callback] and profile/locale broadcasts.
 * Never loads icons: Shunya is text only.
 */
internal class LauncherAppsRepository(
    context: Context,
    private val cache: DataStore<InstalledAppsCache>,
    overridesRepository: AppOverridesRepository,
    settingsRepository: SettingsRepository,
    private val scope: CoroutineScope,
) : AppsRepository {

    private val appContext = context.applicationContext
    private val launcherApps = appContext.getSystemService(LauncherApps::class.java)
    private val userManager = appContext.getSystemService(UserManager::class.java)

    /** Raw system list; null until the cache or the first live query delivers. */
    private val installed = MutableStateFlow<List<InstalledApp>?>(null)
    private val reloadRequests = Channel<Unit>(Channel.CONFLATED)

    override val allApps: StateFlow<List<LauncherApp>> =
        combine(installed.filterNotNull(), overridesRepository.overrides) { apps, overrides ->
            merge(apps, overrides)
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val visibleApps: StateFlow<List<LauncherApp>> =
        combine(allApps, settingsRepository.settings.map { it.drawer.showWorkApps }.distinctUntilChanged()) { apps, showWork ->
            apps.filter { !it.isHidden && (showWork || !it.isWork) }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val favorites: StateFlow<List<LauncherApp>> =
        combine(allApps, overridesRepository.overrides) { apps, overrides ->
            val byId = apps.associateBy { it.key.id }
            overrides.favorites.mapNotNull { id -> byId[id]?.takeUnless { it.isHidden } }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val isLoaded: StateFlow<Boolean> =
        installed.map { it != null }.stateIn(scope, SharingStarted.Eagerly, false)

    init {
        scope.launch(Dispatchers.IO) {
            val cached = readCache()
            if (installed.value == null && cached.isNotEmpty()) installed.value = cached
            for (request in reloadRequests) reload()
        }
        refresh()
        launcherApps.registerCallback(PackageCallback(), Handler(Looper.getMainLooper()))
        registerSystemReceiver()
    }

    override fun find(key: AppKey): LauncherApp? = allApps.value.firstOrNull { it.key == key }

    override fun refresh() {
        reloadRequests.trySend(Unit)
    }

    private suspend fun reload() {
        val fresh = queryInstalledApps()
        installed.value = fresh
        try {
            cache.updateData { current -> if (current.apps == fresh) current else InstalledAppsCache(fresh) }
        } catch (e: IOException) {
            // The cache is only an optimisation; the live list is already published.
        }
    }

    private suspend fun readCache(): List<InstalledApp> =
        try {
            cache.data.first().apps
        } catch (e: IOException) {
            emptyList()
        }

    private fun queryInstalledApps(): List<InstalledApp> {
        val ownPackage = appContext.packageName
        val myUser = Process.myUserHandle()
        return userManager.userProfiles.flatMap { user ->
            val serial = userManager.getSerialNumberForUser(user)
            val activities = try {
                launcherApps.getActivityList(null, user)
            } catch (e: SecurityException) {
                emptyList() // Profile being removed or locked down by policy.
            }
            activities
                .filter { it.componentName.packageName != ownPackage }
                .map { info ->
                    InstalledApp(
                        key = AppKey(info.componentName.packageName, info.componentName.className, serial),
                        label = info.label?.toString().orEmpty().ifBlank { info.componentName.packageName },
                        isWork = user != myUser,
                        isSystem = (info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                        installTime = info.firstInstallTime,
                    )
                }
        }
    }

    private fun merge(apps: List<InstalledApp>, overrides: AppOverrides): List<LauncherApp> {
        val collator = Collator.getInstance(Locale.getDefault()).apply { strength = Collator.PRIMARY }
        return apps
            .map { app ->
                LauncherApp(
                    key = app.key,
                    label = app.label,
                    customLabel = overrides.labelFor(app.key),
                    isWork = app.isWork,
                    isSystem = app.isSystem,
                    isHidden = overrides.isHidden(app.key),
                    isFavorite = overrides.isFavorite(app.key),
                    installTime = app.installTime,
                )
            }
            .sortedWith(compareBy<LauncherApp, String>(collator) { it.displayLabel }.thenBy { it.key.id })
    }

    private fun registerSystemReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_LOCALE_CHANGED)
            addAction(Intent.ACTION_MANAGED_PROFILE_ADDED)
            addAction(Intent.ACTION_MANAGED_PROFILE_REMOVED)
            addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE)
            addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = refresh()
        }
        // Protected system broadcasts only; "exported" is what lets the system deliver them on every API level.
        ContextCompat.registerReceiver(appContext, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
    }

    /** Any package change in any profile: reload (requests are conflated, so bursts cost one query). */
    private inner class PackageCallback : LauncherApps.Callback() {
        override fun onPackageRemoved(packageName: String, user: UserHandle) = refresh()
        override fun onPackageAdded(packageName: String, user: UserHandle) = refresh()
        override fun onPackageChanged(packageName: String, user: UserHandle) = refresh()
        override fun onPackagesAvailable(packageNames: Array<String>, user: UserHandle, replacing: Boolean) = refresh()
        override fun onPackagesUnavailable(packageNames: Array<String>, user: UserHandle, replacing: Boolean) = refresh()
    }
}
