package dev.apn7.shunya.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.contract.NotificationInbox
import dev.apn7.shunya.core.data.SettingsRepository
import dev.apn7.shunya.core.model.NotificationFilterMode
import dev.apn7.shunya.core.model.NotificationPrefs
import dev.apn7.shunya.core.system.PermissionsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class NotificationSettingsUiState(
    val prefs: NotificationPrefs = NotificationPrefs(),
    val hasAccess: Boolean = false,
    val heldCount: Int = 0,
)

/** Settings > Notifications: filter mode, access, allowed apps, inbox (PRD 3.4). */
class NotificationSettingsViewModel(
    private val settings: SettingsRepository,
    permissions: PermissionsRepository,
    inbox: NotificationInbox,
) : ViewModel() {

    val state: StateFlow<NotificationSettingsUiState> = combine(
        settings.settings,
        permissions.status,
        inbox.count,
    ) { current, status, count ->
        NotificationSettingsUiState(current.notifications, status.hasNotificationAccess, count)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        NotificationSettingsUiState(prefs = settings.settings.value.notifications),
    )

    fun setMode(mode: NotificationFilterMode) {
        settings.edit { it.copy(notifications = it.notifications.copy(filterMode = mode)) }
    }
}
