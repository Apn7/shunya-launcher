package dev.apn7.shunya.feature.settings.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.navigation.Route
import dev.apn7.shunya.core.system.SystemIntents
import dev.apn7.shunya.core.system.rememberDefaultLauncherRequest
import dev.apn7.shunya.core.system.startFirstAvailable

/** Entry point of [Route.Permissions]. Re-reads every access whenever the screen resumes. */
@Composable
fun PermissionsEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val permissions = container.permissionsRepository
    val status by permissions.status.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { permissions.refresh() }
    val requestDefaultLauncher = rememberDefaultLauncherRequest { permissions.refresh() }

    PermissionsScreen(
        status = status,
        actions = PermissionActions(
            setDefaultLauncher = requestDefaultLauncher,
            openUsageAccess = { context.startFirstAvailable(SystemIntents.usageAccessSettings(context)) },
            openNotificationAccess = { context.startFirstAvailable(SystemIntents.notificationListenerSettings(context)) },
            openAccessibilityDisclosure = { navigator.navigate(Route.AccessibilityDisclosure) },
            openGrayscaleSetup = { navigator.navigate(Route.GrayscaleSetup) },
        ),
        onBack = navigator::back,
    )
}
