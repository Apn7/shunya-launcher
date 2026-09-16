package dev.apn7.shunya.feature.focus.screentime

import androidx.compose.runtime.Composable
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.core.navigation.PlaceholderScreen

/** Entry point of [dev.apn7.shunya.core.navigation.Route.ScreenTimeAppDetail] (signature fixed by `ShunyaNavHost`). [packageName] comes from the route. */
@Composable
fun ScreenTimeAppDetailEntry(navigator: Navigator, packageName: String) {
    PlaceholderScreen(navigator)
}
