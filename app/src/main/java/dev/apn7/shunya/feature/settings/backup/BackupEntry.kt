package dev.apn7.shunya.feature.settings.backup

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.core.navigation.Navigator

/** MIME types offered when importing: some file managers label .json files as plain text or binary. */
private val IMPORT_TYPES = arrayOf("application/json", "text/plain", "application/octet-stream")

/** Entry point of [dev.apn7.shunya.core.navigation.Route.Backup]. */
@Composable
fun BackupEntry(navigator: Navigator) {
    val container = LocalAppContainer.current
    val viewModel = viewModel {
        BackupViewModel(
            appContext = container.appContext,
            settings = container.settingsRepository,
            overrides = container.appOverridesRepository,
            focus = container.focusConfigRepository,
            appScope = container.appScope,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) viewModel.export(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.read(uri)
    }

    BackupScreen(
        state = state,
        onBack = navigator::back,
        onExport = {
            try {
                exportLauncher.launch(viewModel.suggestedFileName())
            } catch (e: ActivityNotFoundException) {
                viewModel.reportNoFilePicker()
            }
        },
        onImport = {
            try {
                importLauncher.launch(IMPORT_TYPES)
            } catch (e: ActivityNotFoundException) {
                viewModel.reportNoFilePicker()
            }
        },
        onConfirmImport = { viewModel.confirmImport() },
        onCancelImport = { viewModel.cancelImport() },
    )
}
