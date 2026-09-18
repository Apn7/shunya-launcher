package dev.apn7.shunya.feature.home.favorites

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.R
import dev.apn7.shunya.core.navigation.Navigator
import dev.apn7.shunya.feature.home.components.TextInputDialog

/**
 * Entry point of [dev.apn7.shunya.core.navigation.Route.HomeSettings] (signature fixed by
 * `ShunyaNavHost`): the favorites editor. "Add app" swaps in a searchable picker within the
 * same route; Back closes the picker first.
 */
@Composable
fun HomeSettingsEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val viewModel = viewModel {
        HomeSettingsViewModel(container.appsRepository, container.appOverridesRepository, container.settingsRepository)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    var picking by rememberSaveable { mutableStateOf(false) }
    var renamingId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingIntention by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = picking) { picking = false }

    if (picking) {
        FavoritePicker(
            candidates = state.candidates,
            onPick = { app ->
                viewModel.add(app)
                picking = false
            },
            onBack = { picking = false },
        )
    } else {
        HomeSettingsScreen(
            state = state,
            onBack = { navigator.back() },
            onAdd = { picking = true },
            onRename = { app -> renamingId = app.key.id },
            onRemove = { app -> viewModel.remove(app) },
            onMove = { index, delta -> viewModel.move(index, delta) },
            onMaxChange = { max -> viewModel.setMaxFavorites(max) },
            onShowIntentionChange = { show -> viewModel.setShowIntention(show) },
            onEditIntention = { editingIntention = true },
            onShowAppsButtonChange = { show -> viewModel.setShowAppsButton(show) },
        )
    }

    val renaming = state.favorites.firstOrNull { it.key.id == renamingId }
    if (renaming != null) {
        TextInputDialog(
            title = stringResource(R.string.apps_rename_title, renaming.displayLabel),
            initial = renaming.displayLabel,
            placeholder = renaming.label,
            summary = if (renaming.customLabel != null) stringResource(R.string.apps_original_name, renaming.label) else null,
            capitalization = KeyboardCapitalization.Words,
            neutralText = if (renaming.customLabel != null) stringResource(R.string.apps_rename_reset) else null,
            onNeutral = {
                viewModel.resetName(renaming)
                renamingId = null
            },
            onSave = { text ->
                viewModel.rename(renaming, text)
                renamingId = null
            },
            onDismiss = { renamingId = null },
        )
    }
    if (editingIntention) {
        TextInputDialog(
            title = stringResource(R.string.home_intention_title),
            initial = state.home.intention,
            placeholder = stringResource(R.string.home_intention_placeholder),
            onSave = { text ->
                viewModel.setIntention(text)
                editingIntention = false
            },
            onDismiss = { editingIntention = false },
        )
    }
}
