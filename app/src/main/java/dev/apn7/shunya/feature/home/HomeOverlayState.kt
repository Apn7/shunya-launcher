package dev.apn7.shunya.feature.home

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.apn7.shunya.core.model.AppKey

/**
 * What is open on top of the home screen: the drawer (with or without search focus), the quick
 * menu, the app action sheet and the small dialogs. Plain `remember`ed state on purpose: whenever
 * the user leaves the home route and comes back, home starts clean.
 */
@Stable
internal class HomeOverlayState {

    var drawerOpen by mutableStateOf(false)
        private set

    /** The drawer was opened to search (swipe-down "Search"): show the keyboard regardless of the setting. */
    var focusSearch by mutableStateOf(false)
        private set

    /** False when the drawer should vanish without animation (Home pressed from another app). */
    var animateDrawer by mutableStateOf(true)
        private set

    var quickMenuOpen by mutableStateOf(false)

    /** The app whose action sheet is open, or null. */
    var actionTarget by mutableStateOf<AppKey?>(null)

    var editingIntention by mutableStateOf(false)

    var lockHintVisible by mutableStateOf(false)

    fun openDrawer(search: Boolean) {
        animateDrawer = true
        focusSearch = search
        drawerOpen = true
    }

    fun closeDrawer(animate: Boolean = true) {
        animateDrawer = animate
        drawerOpen = false
        focusSearch = false
    }

    /** Home button: close everything; [animate] is false when Shunya was not in front. */
    fun closeAll(animate: Boolean) {
        closeDrawer(animate)
        quickMenuOpen = false
        actionTarget = null
        editingIntention = false
        lockHintVisible = false
    }
}
