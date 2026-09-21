package dev.apn7.shunya.feature.notifications.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.core.contract.NotificationInbox
import dev.apn7.shunya.core.data.SettingsRepository
import dev.apn7.shunya.core.model.HeldNotification
import dev.apn7.shunya.core.model.NotificationFilterMode
import dev.apn7.shunya.feature.notifications.logic.InboxGroup
import dev.apn7.shunya.feature.notifications.logic.InboxRules
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class InboxUiState(
    /** Held notifications grouped by app: newest group first, newest item first. */
    val groups: List<InboxGroup<HeldNotification>> = emptyList(),
    val holdOn: Boolean = false,
) {
    val total: Int get() = groups.sumOf { it.items.size }
}

/** The notification Inbox (PRD 3.4). */
class InboxViewModel(
    private val inbox: NotificationInbox,
    settings: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<InboxUiState> = combine(inbox.items, settings.settings) { items, current ->
        InboxUiState(
            groups = InboxRules.grouped(items, { it.packageName }, { it.postedAt }),
            holdOn = current.notifications.filterMode == NotificationFilterMode.Hold,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InboxUiState())

    fun open(item: HeldNotification) {
        inbox.open(item)
    }

    fun dismiss(key: String) {
        inbox.dismiss(key)
    }

    fun clearAll() {
        inbox.clearAll()
    }
}
