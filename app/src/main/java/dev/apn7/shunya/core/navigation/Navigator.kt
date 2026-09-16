package dev.apn7.shunya.core.navigation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * Shunya's in-house back stack. [Route.Home] is always the root: Back pops one screen and never
 * leaves home; the Home button pops to the root. Owned by `MainActivity` (it survives
 * configuration changes); features receive it as a parameter of their entry composable.
 */
@Stable
class Navigator(root: Route = Route.Home) {

    private var nextEntryId = 0
    private val entries = mutableStateListOf(newEntry(root))

    /** Routes from root to top. Observable from composition. */
    val backStack: List<Route> get() = entries.map { it.route }

    /** The route on screen. */
    val current: Route get() = entries.last().route

    /** True when [back] would pop something. */
    val canGoBack: Boolean get() = entries.size > 1

    internal val currentEntry: NavEntry get() = entries.last()

    /** Opens [route] on top. Opening the route that is already on top does nothing. */
    fun navigate(route: Route) {
        if (current != route) entries.add(newEntry(route))
    }

    /** Closes the top screen; does nothing on the root. Pass it as `onBack = navigator::back`. */
    fun back() {
        if (entries.size <= 1) return
        // The visible entry is cleared by the nav host once its exit animation is over.
        entries.removeAt(entries.lastIndex)
    }

    /** Back to [Route.Home] (Home button). */
    fun popToRoot() {
        if (entries.size <= 1) return
        val hidden = entries.subList(1, entries.lastIndex).toList()
        while (entries.size > 1) entries.removeAt(entries.lastIndex)
        hidden.forEach { it.clear() }
    }

    /** Replaces the whole stack with [root] (rarely needed; e.g. after restoring a backup). */
    fun replaceAll(root: Route) {
        val old = entries.toList()
        entries.clear()
        entries.add(newEntry(root))
        old.dropLast(1).forEach { it.clear() }
    }

    internal fun isInBackStack(entry: NavEntry): Boolean = entries.contains(entry)

    /** Releases every screen's ViewModels (the activity is finishing). */
    internal fun clearAll() {
        entries.forEach { it.clear() }
    }

    private fun newEntry(route: Route) = NavEntry(route, id = "${nextEntryId++}:$route")
}

/**
 * One screen instance on the back stack. It owns the ViewModels created on that screen, so
 * `viewModel { … }` in an entry composable lives exactly as long as the screen is on the stack.
 */
@Stable
class NavEntry internal constructor(
    val route: Route,
    /** Unique key for saved UI state. */
    val id: String,
) : ViewModelStoreOwner {

    override val viewModelStore: ViewModelStore = ViewModelStore()

    internal fun clear() {
        viewModelStore.clear()
    }
}
