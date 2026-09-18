package dev.apn7.shunya.feature.home.gestures

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.feature.home.logic.SwipeClassifier
import dev.apn7.shunya.feature.home.logic.SwipeDirection

/** How far a finger must travel before a swipe counts. */
private val SwipeDistance = 48.dp

/**
 * The home gestures: swipes in four directions, double-tap and long-press on empty space.
 *
 * A swipe fires while the finger is still moving (the drawer opens at once) and at most once per
 * touch. Swipes are observed in the Initial pass, so they also work when they start on a favorite;
 * once the finger passes the touch slop the events are consumed, which cancels the favorite's tap
 * and the long-press. Taps on favorites are handled by the favorites themselves.
 */
@Composable
internal fun Modifier.homeGestures(
    onSwipe: (SwipeDirection) -> Unit,
    onDoubleTap: () -> Unit,
    onLongPress: () -> Unit,
): Modifier {
    val latestSwipe by rememberUpdatedState(onSwipe)
    val latestDoubleTap by rememberUpdatedState(onDoubleTap)
    val latestLongPress by rememberUpdatedState(onLongPress)
    return this
        .pointerInput(Unit) { detectSwipes { direction -> latestSwipe(direction) } }
        .pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = { latestDoubleTap() },
                onLongPress = { latestLongPress() },
            )
        }
}

private suspend fun PointerInputScope.detectSwipes(onSwipe: (SwipeDirection) -> Unit) {
    val minDistance = SwipeDistance.toPx()
    val touchSlop = viewConfiguration.touchSlop
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        var total = Offset.Zero
        var dragging = false
        var fired = false
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id }
            if (change == null || !change.pressed) break
            total += change.position - change.previousPosition
            if (!dragging && total.getDistance() > touchSlop) dragging = true
            if (dragging) {
                change.consume()
                if (!fired) {
                    val direction = SwipeClassifier.classify(total.x, total.y, minDistance)
                    if (direction != null) {
                        fired = true
                        onSwipe(direction)
                    }
                }
            }
        }
    }
}
