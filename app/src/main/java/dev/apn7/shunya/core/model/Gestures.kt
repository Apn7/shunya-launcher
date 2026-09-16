package dev.apn7.shunya.core.model

import kotlinx.serialization.Serializable

/** Something a home-screen gesture can do. Which actions each gesture offers: [HomeGesture.options]. */
@Serializable
enum class GestureAction { None, NotificationShade, Search, OpenApp, ScreenTime, FocusToggle, LockScreen }

/**
 * The configurable home gestures. Swipe up (app drawer) and long-press (quick menu) are fixed
 * so the drawer and settings are always reachable.
 */
enum class HomeGesture(val options: List<GestureAction>) {
    SwipeDown(listOf(GestureAction.NotificationShade, GestureAction.Search, GestureAction.None)),
    SwipeLeft(listOf(GestureAction.OpenApp, GestureAction.ScreenTime, GestureAction.FocusToggle, GestureAction.None)),
    SwipeRight(listOf(GestureAction.OpenApp, GestureAction.ScreenTime, GestureAction.FocusToggle, GestureAction.None)),
    DoubleTap(listOf(GestureAction.LockScreen, GestureAction.None)),
}

/** What one gesture does. [app] is only used when [action] is [GestureAction.OpenApp]. */
@Serializable
data class GestureBinding(
    val action: GestureAction = GestureAction.None,
    val app: AppKey? = null,
)

/** Gesture settings (PRD 3.1). Read and change them by [HomeGesture] with [binding] / [withBinding]. */
@Serializable
data class GesturePrefs(
    val swipeDown: GestureBinding = GestureBinding(GestureAction.NotificationShade),
    val swipeLeft: GestureBinding = GestureBinding(GestureAction.ScreenTime),
    val swipeRight: GestureBinding = GestureBinding(GestureAction.None),
    val doubleTap: GestureBinding = GestureBinding(GestureAction.LockScreen),
) {
    fun binding(gesture: HomeGesture): GestureBinding = when (gesture) {
        HomeGesture.SwipeDown -> swipeDown
        HomeGesture.SwipeLeft -> swipeLeft
        HomeGesture.SwipeRight -> swipeRight
        HomeGesture.DoubleTap -> doubleTap
    }

    fun withBinding(gesture: HomeGesture, binding: GestureBinding): GesturePrefs = when (gesture) {
        HomeGesture.SwipeDown -> copy(swipeDown = binding)
        HomeGesture.SwipeLeft -> copy(swipeLeft = binding)
        HomeGesture.SwipeRight -> copy(swipeRight = binding)
        HomeGesture.DoubleTap -> copy(doubleTap = binding)
    }
}
