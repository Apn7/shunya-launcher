package dev.apn7.shunya.feature.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.contract.FocusController
import dev.apn7.shunya.core.contract.NotificationInbox
import dev.apn7.shunya.core.contract.UsageRepository
import dev.apn7.shunya.core.data.AppsRepository
import dev.apn7.shunya.core.data.SettingsRepository
import dev.apn7.shunya.core.model.FocusStatus
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.core.model.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Everything the home screen shows, except the ticking clock and battery (read in the UI). */
@Immutable
data class HomeUiState(
    val settings: LauncherSettings = LauncherSettings(),
    val favorites: List<LauncherApp> = emptyList(),
    val focus: FocusStatus = FocusStatus.Inactive,
    val heldCount: Int = 0,
    /** Today's screen time, or null without usage access. */
    val screenTimeMillis: Long? = null,
)

/**
 * Home screen state. Every source is an always-hot StateFlow, so the first frame already has the
 * real favorites and settings: nothing on the home path waits on I/O.
 */
class HomeViewModel(
    private val settingsRepository: SettingsRepository,
    appsRepository: AppsRepository,
    focusController: FocusController,
    notificationInbox: NotificationInbox,
    private val usageRepository: UsageRepository,
) : ViewModel() {

    private val screenTime = MutableStateFlow<Long?>(null)

    val state: StateFlow<HomeUiState> = combine(
        settingsRepository.settings,
        appsRepository.favorites,
        focusController.status,
        notificationInbox.count,
        screenTime,
    ) { settings, favorites, focus, held, screen ->
        HomeUiState(settings, favorites, focus, held, screen)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeUiState(
            settings = settingsRepository.settings.value,
            favorites = appsRepository.favorites.value,
            focus = focusController.status.value,
            heldCount = notificationInbox.count.value,
        ),
    )

    /** Re-reads today's screen time for the status line (call when home becomes visible). */
    fun refreshScreenTime() {
        if (!settingsRepository.settings.value.appearance.showStatusLine) return
        viewModelScope.launch {
            screenTime.value = if (usageRepository.hasAccess()) usageRepository.today().totalMillis else null
        }
    }

    fun saveIntention(text: String) {
        settingsRepository.edit { it.copy(home = it.home.copy(intention = text.trim())) }
    }

    fun markLockHintShown() {
        settingsRepository.edit { it.copy(home = it.home.copy(lockHintShown = true)) }
    }

    fun toggleWallpaperMode() {
        settingsRepository.edit { it.copy(appearance = it.appearance.copy(wallpaperMode = !it.appearance.wallpaperMode)) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
