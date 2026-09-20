package dev.apn7.shunya.feature.focus.schedules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.contract.FocusController
import dev.apn7.shunya.core.data.FocusConfigRepository
import dev.apn7.shunya.core.model.FocusConfig
import dev.apn7.shunya.core.model.FocusStatus
import dev.apn7.shunya.core.model.Schedule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SchedulesUiState(
    val schedules: List<Schedule> = emptyList(),
    /** Ids of the schedules that are running right now. */
    val activeIds: Set<String> = emptySet(),
)

class SchedulesViewModel(
    private val configRepository: FocusConfigRepository,
    focusController: FocusController,
) : ViewModel() {

    val state: StateFlow<SchedulesUiState> =
        combine(configRepository.config, focusController.status) { config, status -> uiState(config, status) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                uiState(configRepository.config.value, focusController.status.value),
            )

    /** Adds [schedule] once (a double tap on an example must not add it twice). */
    fun add(schedule: Schedule) {
        configRepository.edit { config ->
            if (config.schedules.any { it.id == schedule.id }) config else config.copy(schedules = config.schedules + schedule)
        }
    }

    private fun uiState(config: FocusConfig, status: FocusStatus) = SchedulesUiState(
        schedules = config.schedules,
        activeIds = status.activeSchedules.map { it.id }.toSet(),
    )
}
