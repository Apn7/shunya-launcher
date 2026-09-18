package dev.apn7.shunya.feature.home.hidden

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.EmptyState
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.LauncherApp
import dev.apn7.shunya.core.navigation.Navigator

/**
 * Entry point of [dev.apn7.shunya.core.navigation.Route.HiddenApps] (signature fixed by
 * `ShunyaNavHost`): hidden apps with Open (through the focus policy) and Unhide.
 */
@Composable
fun HiddenAppsEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val viewModel = viewModel { HiddenAppsViewModel(container.appsRepository, container.appOverridesRepository) }
    val hiddenApps by viewModel.hiddenApps.collectAsStateWithLifecycle()
    HiddenAppsScreen(
        apps = hiddenApps,
        onBack = { navigator.back() },
        onOpen = { app -> container.appLauncher.launch(app) },
        onUnhide = { app -> viewModel.unhide(app) },
    )
}

@Composable
internal fun HiddenAppsScreen(
    apps: List<LauncherApp>,
    onBack: () -> Unit,
    onOpen: (LauncherApp) -> Unit,
    onUnhide: (LauncherApp) -> Unit,
) {
    ShunyaScreen(title = stringResource(R.string.apps_hidden_title), onBack = onBack, scrollable = false) {
        if (apps.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.apps_hidden_empty),
                message = stringResource(R.string.apps_hidden_empty_message),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(count = apps.size, key = { index -> apps[index].key.id }) { index ->
                    val app = apps[index]
                    HiddenAppRow(app = app, onOpen = { onOpen(app) }, onUnhide = { onUnhide(app) })
                }
            }
        }
    }
}

@Composable
private fun HiddenAppRow(app: LauncherApp, onOpen: () -> Unit, onUnhide: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(start = Spacing.screenHorizontal, end = Spacing.s),
    ) {
        Text(
            text = app.displayLabel,
            style = ShunyaTheme.typography.body,
            color = ShunyaTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        ShunyaTextButton(text = stringResource(R.string.common_open), onClick = onOpen, style = ShunyaButtonStyle.Secondary)
        ShunyaTextButton(text = stringResource(R.string.apps_unhide), onClick = onUnhide)
    }
}
