package dev.apn7.shunya.feature.focus.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.contract.FocusController
import dev.apn7.shunya.core.contract.GrayscaleController
import dev.apn7.shunya.core.data.FocusConfigRepository
import dev.apn7.shunya.core.data.SettingsRepository
import dev.apn7.shunya.core.model.BlockingMode
import dev.apn7.shunya.core.model.FocusConfig
import dev.apn7.shunya.core.model.FocusStatus
import dev.apn7.shunya.core.model.LauncherSettings
import dev.apn7.shunya.core.model.ProductLimits
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Everything the focus hub shows. */
data class FocusHubUiState(
    val status: FocusStatus = FocusStatus.Inactive,
    val durations: List<Int> = ProductLimits.FOCUS_DURATIONS_DEFAULT,
    val distractingCount: Int = 0,
    val limitsCount: Int = 0,
    val schedulesOn: Int = 0,
    val blockingMode: BlockingMode = BlockingMode.LauncherOnly,
    val grayscaleOn: Boolean = false,
)

class FocusHubViewModel(
    configRepository: FocusConfigRepository,
    private val settingsRepository: SettingsRepository,
    private val focusController: FocusController,
    private val grayscale: GrayscaleController,
) : ViewModel() {

    val state: StateFlow<FocusHubUiState> = combine(
        configRepository.config,
        focusController.status,
        settingsRepository.settings,
        grayscale.isEnabled,
    ) { config, status, settings, grayscaleOn ->
        uiState(config, status, settings, grayscaleOn)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        uiState(configRepository.config.value, focusController.status.value, settingsRepository.settings.value, grayscale.isEnabled.value),
    )

    fun startSession(minutes: Int?) {
        focusController.startSession(minutes)
    }

    fun endSession() {
        focusController.endSession()
    }

    fun setBlockingMode(mode: BlockingMode) {
        settingsRepository.edit { it.copy(blockingMode = mode) }
    }

    fun setGrayscale(on: Boolean) {
        grayscale.setEnabled(on)
    }

    private fun uiState(config: FocusConfig, status: FocusStatus, settings: LauncherSettings, grayscaleOn: Boolean) = FocusHubUiState(
        status = status,
        durations = config.sessionDurationsMinutes.ifEmpty { ProductLimits.FOCUS_DURATIONS_DEFAULT },
        distractingCount = config.distractingPackages.size,
        limitsCount = config.dailyLimitMinutes.size,
        schedulesOn = config.schedules.count { it.enabled },
        blockingMode = settings.blockingMode,
        grayscaleOn = grayscaleOn,
    )
}
