package dev.apn7.shunya.feature.settings.backup

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.SettingsRow
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaDialog
import dev.apn7.shunya.core.designsystem.component.ShunyaScreen
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.feature.settings.SettingsNote

/** Settings > Backup & restore: export and import one JSON file; import asks before replacing. */
@Composable
internal fun BackupScreen(
    state: BackupUiState,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onConfirmImport: () -> Unit,
    onCancelImport: () -> Unit,
) {
    ShunyaScreen(title = stringResource(R.string.settings_backup), onBack = onBack) {
        SettingsNote(stringResource(R.string.settings_backup_intro))
        SettingsRow(
            title = stringResource(R.string.settings_backup_export),
            summary = stringResource(R.string.settings_backup_export_summary),
            enabled = !state.working,
            onClick = onExport,
        )
        SettingsRow(
            title = stringResource(R.string.settings_backup_import),
            summary = stringResource(R.string.settings_backup_import_summary),
            enabled = !state.working,
            onClick = onImport,
        )
        val messageRes = state.messageRes
        if (state.working) {
            SettingsNote(stringResource(R.string.settings_backup_working))
        } else if (messageRes != null) {
            SettingsNote(stringResource(messageRes))
        }
    }
    val pending = state.pending
    if (pending != null) {
        ShunyaDialog(
            onDismiss = onCancelImport,
            title = stringResource(R.string.settings_backup_confirm_title),
            confirmText = stringResource(R.string.settings_backup_confirm_action),
            onConfirm = onConfirmImport,
            confirmStyle = ShunyaButtonStyle.Danger,
            dismissText = stringResource(R.string.common_cancel),
        ) {
            Text(
                text = stringResource(R.string.settings_backup_confirm_message, pending.createdAt.ifBlank { "?" }),
                style = ShunyaTheme.typography.body,
                color = ShunyaTheme.colors.secondary,
            )
        }
    }
}
