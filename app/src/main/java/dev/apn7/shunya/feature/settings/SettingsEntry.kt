package dev.apn7.shunya.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.apn7.shunya.BuildConfig
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.core.navigation.Navigator

/** Entry point of [dev.apn7.shunya.core.navigation.Route.Settings]: the sectioned settings root. */
@Composable
fun SettingsEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val settings by container.settingsRepository.settings.collectAsStateWithLifecycle()
    val permissions by container.permissionsRepository.status.collectAsStateWithLifecycle()
    val heldCount by container.notificationInbox.count.collectAsStateWithLifecycle()
    val apps by container.appsRepository.allApps.collectAsStateWithLifecycle()
    val focus by container.focusConfigRepository.config.collectAsStateWithLifecycle()
    val grayscaleOn by container.grayscaleController.isEnabled.collectAsStateWithLifecycle()
    val hiddenCount = remember(apps) { apps.count { it.isHidden } }

    SettingsScreen(
        overview = SettingsOverview(
            settings = settings,
            permissions = permissions,
            heldCount = heldCount,
            hiddenCount = hiddenCount,
            distractingCount = focus.distractingPackages.size,
            grayscaleOn = grayscaleOn,
            version = BuildConfig.VERSION_NAME,
        ),
        onBack = navigator::back,
        onOpen = { route -> navigator.navigate(route) },
    )
}
