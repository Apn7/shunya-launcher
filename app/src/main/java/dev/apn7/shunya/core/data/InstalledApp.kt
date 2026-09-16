package dev.apn7.shunya.core.data

import dev.apn7.shunya.core.model.AppKey
import kotlinx.serialization.Serializable

/** One launcher activity as reported by the system, before user overrides. Also the disk-cache row. */
@Serializable
internal data class InstalledApp(
    val key: AppKey,
    val label: String,
    val isWork: Boolean = false,
    val isSystem: Boolean = false,
    val installTime: Long = 0L,
)

/** Disk cache of the last known app list, shown instantly on cold start. */
@Serializable
internal data class InstalledAppsCache(
    val apps: List<InstalledApp> = emptyList(),
)
