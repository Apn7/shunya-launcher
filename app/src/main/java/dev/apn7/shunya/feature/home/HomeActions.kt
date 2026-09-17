package dev.apn7.shunya.feature.home

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import dev.apn7.shunya.AppContainer
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.Haptics
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.GestureAction
import dev.apn7.shunya.core.model.GestureBinding
import dev.apn7.shunya.core.model.HomeGesture
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.navigation.Route
import dev.apn7.shunya.core.system.SystemIntents
import dev.apn7.shunya.core.system.startSafely
import dev.apn7.shunya.feature.home.logic.SwipeDirection

/**
 * What the home screen does for gestures, taps and quick-menu choices. Lives in composition
 * (it needs the activity context for intents and toasts); data writes go through [viewModel].
 * Gesture bindings are read from the settings at the moment of the gesture.
 */
internal class HomeActions(
    private val context: Context,
    private val container: AppContainer,
    private val navigator: Navigator,
    private val overlays: HomeOverlayState,
    private val viewModel: HomeViewModel,
    private val haptics: Haptics,
) {

    fun openClock() {
        context.startSafely(SystemIntents.showAlarms())
    }

    fun openCalendar() {
        context.startSafely(SystemIntents.calendarToday())
    }

    fun onSwipe(direction: SwipeDirection) {
        val gestures = container.settingsRepository.settings.value.gestures
        when (direction) {
            SwipeDirection.Up -> overlays.openDrawer(search = false)
            SwipeDirection.Down -> perform(gestures.binding(HomeGesture.SwipeDown))
            SwipeDirection.Left -> perform(gestures.binding(HomeGesture.SwipeLeft))
            SwipeDirection.Right -> perform(gestures.binding(HomeGesture.SwipeRight))
        }
    }

    fun onDoubleTap() {
        perform(container.settingsRepository.settings.value.gestures.binding(HomeGesture.DoubleTap))
    }

    fun onLongPress() {
        haptics.longPress()
        overlays.quickMenuOpen = true
    }

    // Quick menu. Navigation closes the sheet at once: the route changes anyway.

    fun openSettings() = navigateFromMenu(Route.Settings)

    fun openFocusHub() = navigateFromMenu(Route.FocusHub)

    fun editHome() = navigateFromMenu(Route.HomeSettings)

    fun toggleWallpaper() = viewModel.toggleWallpaperMode()

    /** Toggles grayscale, or explains the one-time setup when Shunya may not change it yet. */
    fun toggleGrayscale() {
        val grayscale = container.grayscaleController
        if (grayscale.isAvailable()) {
            grayscale.setEnabled(!grayscale.isEnabled.value)
        } else {
            navigateFromMenu(Route.GrayscaleSetup)
        }
    }

    fun openAccessibilityDisclosure() {
        overlays.lockHintVisible = false
        navigator.navigate(Route.AccessibilityDisclosure)
    }

    private fun navigateFromMenu(route: Route) {
        overlays.quickMenuOpen = false
        navigator.navigate(route)
    }

    private fun perform(binding: GestureBinding) {
        when (binding.action) {
            GestureAction.None -> Unit
            GestureAction.NotificationShade ->
                if (!container.systemActions.expandNotificationShade()) toast(R.string.home_shade_unavailable)
            GestureAction.Search -> overlays.openDrawer(search = true)
            GestureAction.OpenApp -> openGestureApp(binding.app)
            GestureAction.ScreenTime -> navigator.navigate(Route.ScreenTime)
            GestureAction.FocusToggle -> toggleFocus()
            GestureAction.LockScreen -> lockScreen()
        }
    }

    private fun openGestureApp(key: AppKey?) {
        val app = key?.let { container.appsRepository.find(it) }
        if (app == null) {
            toast(R.string.home_gesture_app_missing)
        } else {
            container.appLauncher.launch(app)
        }
    }

    /** Ends a running session, else starts one (see `FocusController.toggleSession`). */
    private fun toggleFocus() {
        val focus = container.focusController
        val wasRunning = focus.status.value.session != null
        focus.toggleSession()
        haptics.confirm()
        toast(if (wasRunning) R.string.home_focus_ended else R.string.home_focus_started)
    }

    /** Locks through the accessibility service; otherwise explains it once, then a short toast. */
    private fun lockScreen() {
        if (container.systemActions.lockScreen()) return
        if (container.settingsRepository.settings.value.home.lockHintShown) {
            toast(R.string.home_lock_unavailable)
        } else {
            viewModel.markLockHintShown()
            overlays.lockHintVisible = true
        }
    }

    private fun toast(@StringRes message: Int) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
