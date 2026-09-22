package dev.apn7.shunya.feature.settings.gestures

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ChoiceRow
import dev.apn7.shunya.core.designsystem.component.SectionHeader
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.GestureAction
import dev.apn7.shunya.core.model.HomeGesture
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.feature.settings.SettingsNote
import dev.apn7.shunya.feature.settings.gestureActionLabel
import dev.apn7.shunya.feature.settings.gestureLabel

/**
 * Settings > Gestures. Picking "Open an app" shows a full-screen app picker in place of the
 * list (Back closes it); the gesture only changes once an app is chosen.
 */
@Composable
internal fun GesturesScreen(
    state: GesturesUiState,
    canLockScreen: Boolean,
    onBack: () -> Unit,
    onSetAction: (HomeGesture, GestureAction) -> Unit,
    onSetApp: (HomeGesture, LauncherApp) -> Unit,
    onSetUpLock: () -> Unit,
) {
    var pickingFor by rememberSaveable { mutableStateOf<String?>(null) }
    val picking = HomeGesture.entries.firstOrNull { it.name == pickingFor }
    if (picking != null) {
        BackHandler { pickingFor = null }
        AppPickerScreen(
            apps = state.pickerApps,
            onPick = { app ->
                onSetApp(picking, app)
                pickingFor = null
            },
            onBack = { pickingFor = null },
        )
    } else {
        GestureList(
            state = state,
            canLockScreen = canLockScreen,
            onBack = onBack,
            onChoose = { gesture, action ->
                if (action == GestureAction.OpenApp) pickingFor = gesture.name else onSetAction(gesture, action)
            },
            onSetUpLock = onSetUpLock,
        )
    }
}

@Composable
private fun GestureList(
    state: GesturesUiState,
    canLockScreen: Boolean,
    onBack: () -> Unit,
    onChoose: (HomeGesture, GestureAction) -> Unit,
    onSetUpLock: () -> Unit,
) {
    ShunyaScreen(title = stringResource(R.string.settings_gestures), onBack = onBack) {
        HomeGesture.entries.forEach { gesture ->
            val binding = state.gestures.binding(gesture)
            val appSummary = if (binding.action == GestureAction.OpenApp) {
                val label = state.boundAppLabel(gesture)
                if (label != null) {
                    stringResource(R.string.settings_action_open_named, label)
                } else {
                    stringResource(R.string.settings_no_app_chosen)
                }
            } else {
                null
            }
            ChoiceRow(
                title = gestureLabel(gesture),
                options = gesture.options,
                selected = binding.action,
                optionLabel = { gestureActionLabel(it) },
                onSelect = { action -> onChoose(gesture, action) },
                summary = appSummary,
            )
            if (gesture == HomeGesture.DoubleTap && binding.action == GestureAction.LockScreen) {
                LockNote(canLockScreen, state.accessibilityEnabled, onSetUpLock)
            }
        }

        SectionHeader(stringResource(R.string.settings_gestures_fixed))
        SettingsRow(
            title = stringResource(R.string.settings_gesture_swipe_up),
            value = stringResource(R.string.settings_gesture_swipe_up_value),
        )
        SettingsRow(
            title = stringResource(R.string.settings_gesture_long_press),
            value = stringResource(R.string.settings_gesture_long_press_value),
        )
    }
}

/** Lock screen works only through the accessibility service, on Android 9+. Says so honestly. */
@Composable
private fun LockNote(canLockScreen: Boolean, accessibilityEnabled: Boolean, onSetUp: () -> Unit) {
    if (!canLockScreen) {
        SettingsNote(stringResource(R.string.settings_gestures_lock_needs_android9))
    } else if (!accessibilityEnabled) {
        SettingsNote(stringResource(R.string.settings_gestures_lock_needs_service))
        ShunyaTextButton(
            text = stringResource(R.string.settings_set_up),
            onClick = onSetUp,
            modifier = Modifier.padding(horizontal = Spacing.s),
        )
    }
}
