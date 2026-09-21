package dev.apn7.shunya.feature.settings.gestures

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.navigation.Route

/** Entry point of [Route.Gestures]. */
@Composable
fun GesturesEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val viewModel = viewModel {
        GesturesViewModel(container.settingsRepository, container.appsRepository, container.permissionsRepository)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    GesturesScreen(
        state = state,
        canLockScreen = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P,
        onBack = navigator::back,
        onSetAction = { gesture, action -> viewModel.setAction(gesture, action) },
        onSetApp = { gesture, app -> viewModel.setApp(gesture, app.key) },
        onSetUpLock = { navigator.navigate(Route.AccessibilityDisclosure) },
    )
}
