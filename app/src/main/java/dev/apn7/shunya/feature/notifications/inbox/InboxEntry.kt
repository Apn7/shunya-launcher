package dev.apn7.shunya.feature.notifications.inbox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.navigation.Route

/** Entry point of [Route.Inbox]. */
@Composable
fun InboxEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val viewModel = viewModel { InboxViewModel(container.notificationInbox, container.settingsRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    InboxScreen(
        state = state,
        onBack = navigator::back,
        onOpen = { item -> viewModel.open(item) },
        onDismiss = { key -> viewModel.dismiss(key) },
        onClearAll = { viewModel.clearAll() },
        onOpenSettings = {
            // Came from Notification settings? Go back instead of stacking a second copy.
            val stack = navigator.backStack
            if (stack.size >= 2 && stack[stack.size - 2] == Route.NotificationSettings) {
                navigator.back()
            } else {
                navigator.navigate(Route.NotificationSettings)
            }
        },
    )
}
