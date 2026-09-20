package dev.apn7.shunya.feature.focus.schedules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.data.FocusConfigRepository
import dev.apn7.shunya.core.model.Schedule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The schedule being edited; saved only on "Save". */
data class ScheduleDraft(
    val id: String,
    val name: String,
    val days: Set<Int>,
    val startMinute: Int,
    val endMinute: Int,
    val enabled: Boolean,
    val grayscale: Boolean,
) {
    /** True when the window ends the next day (end at or before start). */
    val crossesMidnight: Boolean get() = endMinute <= startMinute

    val canSave: Boolean get() = days.isNotEmpty()
}

class ScheduleEditorViewModel(
    private val configRepository: FocusConfigRepository,
    private val scheduleId: String?,
) : ViewModel() {

    private val _draft = MutableStateFlow(initialDraft())
    val draft: StateFlow<ScheduleDraft> = _draft.asStateFlow()

    /** Editing a stored schedule (Delete is offered) rather than creating one. */
    val isExisting: Boolean = scheduleId != null

    init {
        // Normally the config is loaded long before; if not, reload the draft once it is.
        if (scheduleId != null && !configRepository.isLoaded.value) {
            viewModelScope.launch {
                configRepository.isLoaded.first { it }
                _draft.value = initialDraft()
            }
        }
    }

    fun update(transform: (ScheduleDraft) -> ScheduleDraft) {
        _draft.update(transform)
    }

    /** Stores the draft (replacing the schedule with the same id). A blank name becomes [defaultName]. */
    fun save(defaultName: String) {
        val draft = _draft.value
        if (!draft.canSave) return
        val schedule = Schedule(
            id = draft.id,
            name = draft.name.trim().ifEmpty { defaultName },
            days = draft.days,
            startMinute = draft.startMinute,
            endMinute = draft.endMinute,
            enabled = draft.enabled,
            grayscale = draft.grayscale,
        )
        configRepository.edit { config ->
            val index = config.schedules.indexOfFirst { it.id == schedule.id }
            val schedules = if (index >= 0) {
                config.schedules.mapIndexed { i, existing -> if (i == index) schedule else existing }
            } else {
                config.schedules + schedule
            }
            config.copy(schedules = schedules)
        }
    }

    fun delete() {
        val id = _draft.value.id
        configRepository.edit { config -> config.copy(schedules = config.schedules.filterNot { it.id == id }) }
    }

    private fun initialDraft(): ScheduleDraft {
        val existing = scheduleId?.let { id -> configRepository.config.value.schedules.firstOrNull { it.id == id } }
        return if (existing != null) {
            ScheduleDraft(
                id = existing.id,
                name = existing.name,
                days = existing.days,
                startMinute = existing.startMinute,
                endMinute = existing.endMinute,
                enabled = existing.enabled,
                grayscale = existing.grayscale,
            )
        } else {
            ScheduleDraft(
                id = scheduleId ?: SchedulePresets.newId(),
                name = "",
                days = Schedule.ALL_DAYS,
                startMinute = DEFAULT_START_MINUTE,
                endMinute = DEFAULT_END_MINUTE,
                enabled = true,
                grayscale = false,
            )
        }
    }

    private companion object {
        const val DEFAULT_START_MINUTE = 22 * 60
        const val DEFAULT_END_MINUTE = 7 * 60
    }
}
