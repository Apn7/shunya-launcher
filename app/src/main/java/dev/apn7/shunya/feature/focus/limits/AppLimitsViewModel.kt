package dev.apn7.shunya.feature.focus.limits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.contract.UsageRepository
import dev.apn7.shunya.core.data.AppsRepository
import dev.apn7.shunya.core.data.FocusConfigRepository
import dev.apn7.shunya.core.model.FocusConfig
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.feature.focus.ui.PackageRow
import dev.apn7.shunya.feature.focus.ui.packageRows
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** One app with a daily limit and today's use of it. */
data class LimitRow(
    val packageName: String,
    val label: String,
    val limitMinutes: Int,
    val usedTodayMillis: Long,
)

data class AppLimitsUiState(
    val limits: List<LimitRow> = emptyList(),
    /** Apps that can still get a limit (for the picker). */
    val candidates: List<PackageRow> = emptyList(),
)

class AppLimitsViewModel(
    appsRepository: AppsRepository,
    private val configRepository: FocusConfigRepository,
    private val usageRepository: UsageRepository,
) : ViewModel() {

    private val usedToday = MutableStateFlow<Map<String, Long>>(emptyMap())

    val state: StateFlow<AppLimitsUiState> =
        combine(appsRepository.allApps, configRepository.config, usedToday) { apps, config, used -> uiState(apps, config, used) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                uiState(appsRepository.allApps.value, configRepository.config.value, emptyMap()),
            )

    init {
        refreshUsage()
    }

    /** Re-reads today's usage (on screen resume). */
    fun refreshUsage() {
        viewModelScope.launch {
            usedToday.value = usageRepository.today().apps.associate { it.packageName to it.foregroundMillis }
        }
    }

    fun setLimit(packageName: String, minutes: Int) {
        configRepository.edit { it.copy(dailyLimitMinutes = it.dailyLimitMinutes + (packageName to minutes)) }
    }

    fun removeLimit(packageName: String) {
        configRepository.edit { it.copy(dailyLimitMinutes = it.dailyLimitMinutes - packageName) }
    }

    private fun uiState(apps: List<LauncherApp>, config: FocusConfig, used: Map<String, Long>): AppLimitsUiState {
        val rows = packageRows(apps)
        val labels = rows.associate { it.packageName to it.label }
        val limits = config.dailyLimitMinutes
            .map { (pkg, minutes) -> LimitRow(pkg, labels[pkg] ?: pkg, minutes, used[pkg] ?: 0L) }
            .sortedBy { it.label.lowercase() }
        return AppLimitsUiState(
            limits = limits,
            candidates = rows.filter { it.packageName !in config.dailyLimitMinutes },
        )
    }
}
