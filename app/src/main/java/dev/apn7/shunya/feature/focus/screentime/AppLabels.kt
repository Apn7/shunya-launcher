package dev.apn7.shunya.feature.focus.screentime

import android.content.Context
import android.content.pm.PackageManager
import dev.apn7.shunya.core.data.AppsRepository
import dev.apn7.shunya.feature.focus.ui.packageRows
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Display names for packages in usage data. Launcher apps use Shunya's list (with the user's
 * renames); other packages (e.g. system screens) ask the package manager off the main thread and
 * fall back to the package name.
 */
class AppLabels(context: Context, private val appsRepository: AppsRepository) {

    private val packageManager: PackageManager = context.applicationContext.packageManager

    suspend fun labelsFor(packages: Collection<String>): Map<String, String> {
        val launcherLabels = packageRows(appsRepository.allApps.value).associate { it.packageName to it.label }
        val missing = packages.filter { it !in launcherLabels }
        val systemLabels: Map<String, String?> = if (missing.isEmpty()) {
            emptyMap()
        } else {
            withContext(Dispatchers.IO) { missing.associateWith { systemLabel(it) } }
        }
        return packages.associateWith { pkg -> launcherLabels[pkg] ?: systemLabels[pkg] ?: pkg }
    }

    private fun systemLabel(packageName: String): String? = try {
        @Suppress("DEPRECATION")
        val info = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(info).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
}
