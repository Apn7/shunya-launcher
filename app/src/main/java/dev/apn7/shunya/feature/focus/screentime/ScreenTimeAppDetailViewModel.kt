package dev.apn7.shunya.feature.focus.screentime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.contract.UsageRepository
import dev.apn7.shunya.core.data.AppsRepository
import dev.apn7.shunya.core.data.FocusConfigRepository
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.FocusConfig
import dev.apn7.shunya.core.model.HourUsage
import dev.apn7.shunya.feature.focus.ui.packageRows
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Usage numbers for one app, loaded from the usage repository. */
data class AppUsageDetail(
    val loading: Boolean = true,
    val hasAccess: Boolean = true,
    val label: String = "",
    val todayMillis: Long = 0L,
    val opensToday: Int = 0,
    val weekMillis: Long = 0L,
    /** 24 buckets for today (empty without access). */
    val hours: List<HourUsage> = emptyList(),
)

data class AppDetailUiState(
    val usage: AppUsageDetail = AppUsageDetail(),
    val distracting: Boolean = false,
    val limitMinutes: Int? = null,
    /** Launchable entry for "App info"; null for packages without a launcher entry. */
    val appKey: AppKey? = null,
)

class ScreenTimeAppDetailViewModel(
    private val packageName: String,
    private val usageRepository: UsageRepository,
    private val configRepository: FocusConfigRepository,
    private val appsRepository: AppsRepository,
    private val labels: AppLabels,
) : ViewModel() {

    private val usage = MutableStateFlow(AppUsageDetail(label = packageName))
    private var loading: Job? = null

    val state: StateFlow<AppDetailUiState> =
        combine(usage, configRepository.config) { detail, config -> uiState(detail, config) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), uiState(usage.value, configRepository.config.value))

    init {
        refresh()
    }

    fun refresh() {
        loading?.cancel()
        loading = viewModelScope.launch {
            val label = labels.labelsFor(listOf(packageName))[packageName] ?: packageName
            if (!usageRepository.hasAccess()) {
                usage.value = AppUsageDetail(loading = false, hasAccess = false, label = label)
                return@launch
            }
            val today = usageRepository.appToday(packageName)
            val hours = usageRepository.hourly(packageName, LocalDate.now())
            val week = usageRepository.ranking(WEEK_DAYS).firstOrNull { it.packageName == packageName }?.foregroundMillis ?: 0L
            usage.value = AppUsageDetail(
                loading = false,
                hasAccess = true,
                label = label,
                todayMillis = today.foregroundMillis,
                opensToday = today.launchCount,
                weekMillis = week,
                hours = hours,
            )
        }
    }

    fun setDistracting(distracting: Boolean) {
        configRepository.edit {
            val packages = if (distracting) it.distractingPackages + packageName else it.distractingPackages - packageName
            it.copy(distractingPackages = packages)
        }
    }

    fun setLimit(minutes: Int) {
        configRepository.edit { it.copy(dailyLimitMinutes = it.dailyLimitMinutes + (packageName to minutes)) }
    }

    fun removeLimit() {
        configRepository.edit { it.copy(dailyLimitMinutes = it.dailyLimitMinutes - packageName) }
    }

    private fun uiState(detail: AppUsageDetail, config: FocusConfig) = AppDetailUiState(
        usage = detail,
        distracting = config.isDistracting(packageName),
        limitMinutes = config.limitMinutesFor(packageName),
        appKey = packageRows(appsRepository.allApps.value).firstOrNull { it.packageName == packageName }?.key,
    )

    private companion object {
        const val WEEK_DAYS = 7
    }
}
