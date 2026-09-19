package dev.apn7.shunya.feature.focus.ui

import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.LauncherApp

/** One app as focus rules see it: rules and usage are per package, not per activity or profile. */
data class PackageRow(
    val packageName: String,
    val label: String,
    /** A launchable entry for "App info" and similar (personal profile preferred). */
    val key: AppKey,
)

/**
 * One row per package from the (already label-sorted) app list, keeping that order. When a package
 * has several entries (two launcher activities, or a work-profile twin), the personal one wins.
 */
fun packageRows(apps: List<LauncherApp>): List<PackageRow> =
    apps.groupBy { it.packageName }.map { (packageName, entries) ->
        val preferred = entries.firstOrNull { it.key.userSerial == AppKey.MAIN_USER_SERIAL } ?: entries.first()
        PackageRow(packageName = packageName, label = preferred.displayLabel, key = preferred.key)
    }

/** Rows whose label contains [query] (case-insensitive); all rows for a blank query. */
fun List<PackageRow>.matching(query: String): List<PackageRow> {
    val needle = query.trim()
    return if (needle.isEmpty()) this else filter { it.label.contains(needle, ignoreCase = true) }
}
