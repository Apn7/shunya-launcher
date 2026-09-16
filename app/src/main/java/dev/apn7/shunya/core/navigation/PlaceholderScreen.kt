package dev.apn7.shunya.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.EmptyState
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen

/**
 * Stand-in for an entry whose feature has not landed yet, so every route is navigable from day
 * one. Feature engineers replace their stub's body; this goes away once no stub uses it.
 */
@Composable
fun PlaceholderScreen(navigator: Navigator) {
    ShunyaScreen(
        title = stringResource(R.string.core_placeholder_title),
        onBack = if (navigator.canGoBack) navigator::back else null,
    ) {
        EmptyState(title = stringResource(R.string.core_placeholder_message))
    }
}
