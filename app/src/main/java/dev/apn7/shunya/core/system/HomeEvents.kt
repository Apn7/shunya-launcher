package dev.apn7.shunya.core.system

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** One press of the Home button that reached Shunya. */
data class HomePress(
    /** True when Shunya was already in front: animate back to the default state. */
    val wasAlreadyHome: Boolean,
)

/**
 * App-wide home-button events, emitted by `MainActivity.onNewIntent`. The navigator has already
 * popped to [dev.apn7.shunya.core.navigation.Route.Home]; the home screen should close the
 * drawer, search and sheets and scroll to the top.
 */
class HomeEvents {

    private val presses = MutableSharedFlow<HomePress>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** Hot stream of presses; collect it in a `LaunchedEffect` on the home screen. */
    val homePressed: SharedFlow<HomePress> = presses.asSharedFlow()

    fun onHomePressed(wasAlreadyHome: Boolean) {
        presses.tryEmit(HomePress(wasAlreadyHome))
    }
}
