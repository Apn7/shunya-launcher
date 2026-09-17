package dev.apn7.shunya.feature.home.drawer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.theme.Motion
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.core.system.SystemIntents
import dev.apn7.shunya.core.system.startSafely
import dev.apn7.shunya.feature.home.logic.SwipeClassifier
import dev.apn7.shunya.feature.home.logic.SwipeDirection
import kotlinx.coroutines.launch

/** Extra dim over the wallpaper behind the drawer, so a long list stays readable on any wallpaper. */
private const val DRAWER_WALLPAPER_DIM = 60

/** A downward pull this long at the top of the list closes the drawer. */
private val PullToCloseDistance = 72.dp

/** Keeps the fast scroller clear of the search field. */
private val ScrollerTopInset = 72.dp

/**
 * The app drawer: an overlay inside the home route that slides up and fades in (PRD 3.2).
 * Holds the search field, the alphabetical list with the fast scroller, and search results.
 * Closes with Back, the Home button, launching an app, or a downward pull at the top of the list.
 */
@Composable
internal fun DrawerOverlay(
    visible: Boolean,
    animateExit: Boolean,
    state: DrawerUiState,
    query: String,
    showKeyboard: Boolean,
    autoLaunchSingleMatch: Boolean,
    wallpaperMode: Boolean,
    onQueryChange: (String) -> Unit,
    onOpened: () -> Unit,
    onClose: () -> Unit,
    onLaunch: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(Motion.MEDIUM_MILLIS)) +
            slideInVertically(tween(Motion.MEDIUM_MILLIS)) { height -> height / 10 },
        exit = if (animateExit) {
            fadeOut(tween(Motion.SHORT_MILLIS)) + slideOutVertically(tween(Motion.SHORT_MILLIS)) { height -> height / 10 }
        } else {
            ExitTransition.None
        },
    ) {
        DrawerContent(
            state = state,
            query = query,
            showKeyboard = showKeyboard,
            autoLaunchSingleMatch = autoLaunchSingleMatch,
            wallpaperMode = wallpaperMode,
            onQueryChange = onQueryChange,
            onOpened = onOpened,
            onClose = onClose,
            onLaunch = onLaunch,
            onAppLongClick = onAppLongClick,
        )
    }
}

@Composable
private fun DrawerContent(
    state: DrawerUiState,
    query: String,
    showKeyboard: Boolean,
    autoLaunchSingleMatch: Boolean,
    wallpaperMode: Boolean,
    onQueryChange: (String) -> Unit,
    onOpened: () -> Unit,
    onClose: () -> Unit,
    onLaunch: (LauncherApp) -> Unit,
    onAppLongClick: (LauncherApp) -> Unit,
) {
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val resultsState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val searching = query.isNotBlank()
    val backdrop = if (wallpaperMode) ShunyaTheme.colors.wallpaperScrim(DRAWER_WALLPAPER_DIM) else Color.Transparent
    val copyLabel = context.getString(R.string.search_calculator_label)

    val onGo: () -> Unit = {
        val top = state.results.firstOrNull()
        val calculation = state.calculation
        when {
            top != null -> onLaunch(top)
            calculation != null -> copyToClipboard(context, copyLabel, calculation)
            searching -> context.startSafely(SystemIntents.webSearch(query.trim()))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backdrop)
                .pullDownToClose(
                    isAtTop = { (if (query.isNotBlank()) resultsState else listState).isAtTop() },
                    onClose = onClose,
                )
                .safeDrawingPadding(),
        ) {
            SearchField(query = query, onQueryChange = onQueryChange, focusRequester = focusRequester, onGo = onGo)
            if (searching) {
                SearchResultsList(
                    state = state,
                    query = query.trim(),
                    listState = resultsState,
                    onLaunch = onLaunch,
                    onAppLongClick = onAppLongClick,
                    onCopy = { value -> copyToClipboard(context, copyLabel, value) },
                    onWebSearch = { q -> context.startSafely(SystemIntents.webSearch(q)) },
                    onStoreSearch = { q -> context.startSafely(SystemIntents.playStoreSearch(q)) },
                )
            } else {
                AppList(
                    state = state,
                    listState = listState,
                    reserveScrollerSpace = state.showScroller,
                    onLaunch = onLaunch,
                    onAppLongClick = onAppLongClick,
                )
            }
        }
        if (!searching && state.showScroller) {
            AlphabetScroller(
                sections = state.layout.sections,
                onSelect = { section ->
                    keyboard?.hide()
                    scope.launch { listState.scrollToItem(section.row) }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .safeDrawingPadding()
                    .padding(top = ScrollerTopInset, bottom = Spacing.m),
            )
        }
    }

    LaunchedEffect(Unit) {
        onOpened()
        if (showKeyboard) focusRequester.requestFocus()
    }
    // Scrolling the list means browsing, not typing: get the keyboard out of the way.
    LaunchedEffect(listState, resultsState) {
        snapshotFlow { listState.isScrollInProgress || resultsState.isScrollInProgress }
            .collect { scrolling -> if (scrolling) keyboard?.hide() }
    }
    LaunchedEffect(query) { resultsState.scrollToItem(0) }
    LaunchedEffect(state.query, state.results) {
        val single = state.results.singleOrNull()
        if (autoLaunchSingleMatch && single != null && state.calculation == null && state.query == query && query.isNotBlank()) {
            onLaunch(single)
        }
    }
}

private fun LazyListState.isAtTop(): Boolean = firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0

/**
 * Closes the drawer on a downward pull that starts while the list is at its top. Observes the
 * Initial pass without consuming, so the list still gets every event (and shows its overscroll).
 */
@Composable
private fun Modifier.pullDownToClose(isAtTop: () -> Boolean, onClose: () -> Unit): Modifier {
    val latestIsAtTop by rememberUpdatedState(isAtTop)
    val latestOnClose by rememberUpdatedState(onClose)
    return pointerInput(Unit) {
        val minDistance = PullToCloseDistance.toPx()
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            var tracking = latestIsAtTop()
            var total = Offset.Zero
            while (tracking) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id }
                if (change == null || !change.pressed) break
                total += change.position - change.previousPosition
                val direction = SwipeClassifier.classify(total.x, total.y, minDistance)
                if (direction != null) {
                    if (direction == SwipeDirection.Down) latestOnClose()
                    tracking = false
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    // Android 13+ confirms clipboard writes itself.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(context, R.string.common_copied, Toast.LENGTH_SHORT).show()
    }
}
