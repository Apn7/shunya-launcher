package dev.apn7.shunya.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.rememberHaptics
import dev.apn7.shunya.core.designsystem.theme.Motion
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.feature.home.actions.AppActionSheetHost
import dev.apn7.shunya.feature.home.components.LockHintDialog
import dev.apn7.shunya.feature.home.components.TextInputDialog
import dev.apn7.shunya.feature.home.components.rememberClockFormatter
import dev.apn7.shunya.feature.home.components.rememberNow
import dev.apn7.shunya.feature.home.drawer.DrawerOverlay
import dev.apn7.shunya.feature.home.drawer.DrawerViewModel
import dev.apn7.shunya.feature.home.gestures.homeGestures
import dev.apn7.shunya.feature.home.quickmenu.QuickMenuSheet
import dev.apn7.shunya.feature.home.system.rememberBatteryState
import dev.apn7.shunya.feature.home.system.rememberNextAlarmMillis
import kotlinx.coroutines.delay

/**
 * Entry point of [dev.apn7.shunya.core.navigation.Route.Home] (signature fixed by `ShunyaNavHost`).
 *
 * Home is the root and never leaves: the drawer, search, quick menu and app actions are overlays
 * inside this route. Back closes the drawer (sheets and dialogs close themselves); the Home button
 * closes everything and resets search.
 */
@Composable
fun HomeEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val haptics = rememberHaptics()
    val homeViewModel = viewModel {
        HomeViewModel(
            settingsRepository = container.settingsRepository,
            appsRepository = container.appsRepository,
            focusController = container.focusController,
            notificationInbox = container.notificationInbox,
            usageRepository = container.usageRepository,
        )
    }
    val drawerViewModel = viewModel {
        DrawerViewModel(container.appsRepository, container.settingsRepository, container.usageRepository)
    }
    val state by homeViewModel.state.collectAsStateWithLifecycle()
    val drawerState by drawerViewModel.state.collectAsStateWithLifecycle()
    val overlays = remember { HomeOverlayState() }
    val actions = remember(context, container, navigator, overlays, homeViewModel, haptics) {
        HomeActions(context, container, navigator, overlays, homeViewModel, haptics)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { homeViewModel.refreshScreenTime() }
    val closeDrawer: () -> Unit = {
        keyboard?.hide()
        overlays.closeDrawer()
    }
    LaunchedEffect(container, overlays) {
        container.homeEvents.homePressed.collect { press ->
            keyboard?.hide()
            overlays.closeAll(animate = press.wasAlreadyHome)
        }
    }
    // Reset search once the drawer has finished closing, so its content does not change mid-animation.
    LaunchedEffect(overlays.drawerOpen) {
        if (!overlays.drawerOpen) {
            delay(Motion.SHORT_MILLIS.toLong())
            drawerViewModel.clearQuery()
        }
    }
    BackHandler(enabled = overlays.drawerOpen, onBack = closeDrawer)

    val appearance = state.settings.appearance
    val drawerPrefs = state.settings.drawer
    val now = rememberNow(appearance.showSeconds)
    val formatter = rememberClockFormatter(appearance.clockFormat, appearance.showSeconds)
    val battery = rememberBatteryState(appearance.showBattery)
    val nextAlarm = rememberNextAlarmMillis(appearance.showNextAlarm)
    val homeAlpha by animateFloatAsState(
        targetValue = if (overlays.drawerOpen) 0f else 1f,
        animationSpec = tween(Motion.MEDIUM_MILLIS),
        label = "homeAlpha",
    )
    val openDrawerLabel = stringResource(R.string.home_action_open_drawer)
    val menuLabel = stringResource(R.string.home_action_menu)
    val scrim = if (appearance.wallpaperMode) {
        Modifier.background(ShunyaTheme.colors.wallpaperScrim(appearance.wallpaperDimPercent))
    } else {
        Modifier
    }
    val launchFromDrawer: (LauncherApp) -> Unit = { app ->
        keyboard?.hide()
        container.appLauncher.launch(app)
        overlays.closeDrawer(animate = false)
    }

    Box(modifier = Modifier.fillMaxSize().then(scrim)) {
        HomeScreen(
            state = state,
            now = now,
            formatter = formatter,
            battery = battery,
            nextAlarmMillis = nextAlarm,
            onClockClick = { actions.openClock() },
            onDateClick = { actions.openCalendar() },
            onIntentionClick = { overlays.editingIntention = true },
            onAppClick = { app -> container.appLauncher.launch(app) },
            onAppLongClick = { app -> overlays.actionTarget = app.key },
            onAppsButtonClick = { overlays.openDrawer(search = false) },
            modifier = Modifier
                .graphicsLayer { alpha = homeAlpha }
                .homeGestures(
                    onSwipe = { direction -> actions.onSwipe(direction) },
                    onDoubleTap = { actions.onDoubleTap() },
                    onLongPress = { actions.onLongPress() },
                )
                .then(
                    if (overlays.drawerOpen) {
                        Modifier.clearAndSetSemantics { }
                    } else {
                        Modifier.semantics {
                            customActions = listOf(
                                CustomAccessibilityAction(openDrawerLabel) {
                                    overlays.openDrawer(search = false)
                                    true
                                },
                                CustomAccessibilityAction(menuLabel) {
                                    actions.onLongPress()
                                    true
                                },
                            )
                        }
                    },
                ),
        )
        DrawerOverlay(
            visible = overlays.drawerOpen,
            animateExit = overlays.animateDrawer,
            state = drawerState,
            query = drawerViewModel.query,
            showKeyboard = overlays.focusSearch || drawerPrefs.autoShowKeyboard,
            autoLaunchSingleMatch = drawerPrefs.autoLaunchSingleMatch,
            wallpaperMode = appearance.wallpaperMode,
            onQueryChange = { text -> drawerViewModel.onQueryChange(text) },
            onOpened = { drawerViewModel.onOpened() },
            onClose = closeDrawer,
            onLaunch = launchFromDrawer,
            onAppLongClick = { app -> overlays.actionTarget = app.key },
        )
    }

    if (overlays.quickMenuOpen) {
        val grayscaleOn by container.grayscaleController.isEnabled.collectAsStateWithLifecycle()
        QuickMenuSheet(
            wallpaperMode = appearance.wallpaperMode,
            grayscaleOn = grayscaleOn,
            grayscaleAvailable = container.grayscaleController.isAvailable(),
            onDismiss = { overlays.quickMenuOpen = false },
            onSettings = { actions.openSettings() },
            onFocusNow = { actions.openFocusHub() },
            onToggleWallpaper = { actions.toggleWallpaper() },
            onGrayscale = { actions.toggleGrayscale() },
            onEditHome = { actions.editHome() },
        )
    }
    overlays.actionTarget?.let { key ->
        AppActionSheetHost(appKey = key, onDone = { overlays.actionTarget = null })
    }
    if (overlays.editingIntention) {
        TextInputDialog(
            title = stringResource(R.string.home_intention_title),
            initial = state.settings.home.intention,
            placeholder = stringResource(R.string.home_intention_placeholder),
            onSave = { text ->
                homeViewModel.saveIntention(text)
                overlays.editingIntention = false
            },
            onDismiss = { overlays.editingIntention = false },
        )
    }
    if (overlays.lockHintVisible) {
        LockHintDialog(
            onEnable = { actions.openAccessibilityDisclosure() },
            onDismiss = { overlays.lockHintVisible = false },
        )
    }
}
