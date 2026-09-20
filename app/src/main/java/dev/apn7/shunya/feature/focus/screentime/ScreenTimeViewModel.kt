package dev.apn7.shunya.feature.focus.screentime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.contract.UsageRepository
import dev.apn7.shunya.core.model.DayUsage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** One app in today's list. */
data class AppUsageRow(
    val packageName: String,
    val label: String,
    val foregroundMillis: Long,
    val opens: Int,
)

data class ScreenTimeUiState(
    val loading: Boolean = true,
    val hasAccess: Boolean = true,
    val totalMillis: Long = 0L,
    /** Null when unknown (Android 8). */
    val unlockCount: Int? = null,
    /** Most used first. */
    val apps: List<AppUsageRow> = emptyList(),
    /** Oldest first, today last. */
    val days: List<DayUsage> = emptyList(),
) {
    /** Average of the days that have any use (a new install does not drag the average down). */
    val dailyAverageMillis: Long
        get() {
            val used = days.filter { it.totalMillis > 0L }
            return if (used.isEmpty()) 0L else used.sumOf { it.totalMillis } / used.size
        }
}

class ScreenTimeViewModel(
    private val usage: UsageRepository,
    private val labels: AppLabels,
) : ViewModel() {

    private val _state = MutableStateFlow(ScreenTimeUiState())
    val state: StateFlow<ScreenTimeUiState> = _state.asStateFlow()
    private var loading: Job? = null

    init {
        refresh()
    }

    /** Reloads everything (on resume: usage access may just have been granted). */
    fun refresh() {
        loading?.cancel()
        loading = viewModelScope.launch {
            if (!usage.hasAccess()) {
                _state.value = ScreenTimeUiState(loading = false, hasAccess = false)
                return@launch
            }
            val today = usage.today()
            val days = usage.lastDays(DAYS)
            val names = labels.labelsFor(today.apps.map { it.packageName })
            _state.value = ScreenTimeUiState(
                loading = false,
                hasAccess = true,
                totalMillis = today.totalMillis,
                unlockCount = today.unlockCount,
                apps = today.apps.map { AppUsageRow(it.packageName, names[it.packageName] ?: it.packageName, it.foregroundMillis, it.launchCount) },
                days = days,
            )
        }
    }

    private companion object {
        const val DAYS = 7
    }
}
