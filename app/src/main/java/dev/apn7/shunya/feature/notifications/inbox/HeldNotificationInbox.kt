package dev.apn7.shunya.feature.notifications.inbox

import dev.apn7.shunya.core.contract.NotificationInbox
import dev.apn7.shunya.core.model.HeldNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** [NotificationInbox] persisted in its own DataStore. Stub behaviour: always empty. */
class HeldNotificationInbox : NotificationInbox {

    override val items: StateFlow<List<HeldNotification>> = MutableStateFlow(emptyList())

    override val count: StateFlow<Int> = MutableStateFlow(0)

    override fun dismiss(key: String) = Unit

    override fun clearAll() = Unit

    override fun open(item: HeldNotification) = Unit
}
