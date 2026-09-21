package dev.apn7.shunya.feature.settings.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.apn7.shunya.BuildConfig
import dev.apn7.shunya.R
import dev.apn7.shunya.core.data.AppOverridesRepository
import dev.apn7.shunya.core.data.FocusConfigRepository
import dev.apn7.shunya.core.data.SettingsRepository
import dev.apn7.shunya.feature.settings.backup.logic.BackupProblem
import dev.apn7.shunya.feature.settings.backup.logic.BackupRules
import dev.apn7.shunya.feature.settings.language.AppLocales
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.LocalDate

/** A valid backup waiting for the user's "Replace". */
data class PendingImport(val content: BackupContent, val createdAt: String)

data class BackupUiState(
    val working: Boolean = false,
    /** String resource describing the last outcome, or null. */
    val messageRes: Int? = null,
    val pending: PendingImport? = null,
)

/**
 * Settings > Backup & restore (PRD 3.5). Files go through the Storage Access Framework; the
 * backup is validated completely before anything is written, and a failed restore rolls back.
 */
class BackupViewModel(
    private val appContext: Context,
    private val settings: SettingsRepository,
    private val overrides: AppOverridesRepository,
    private val focus: FocusConfigRepository,
    private val appScope: CoroutineScope,
) : ViewModel() {

    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    fun suggestedFileName(): String = BackupRules.fileName(LocalDate.now().toString())

    fun export(uri: Uri) {
        _state.update { it.copy(working = true, messageRes = null) }
        viewModelScope.launch {
            val content = BackupContent(settings.settings.value, overrides.overrides.value, focus.config.value)
            val text = BackupCodec.encode(content, createdAt = LocalDate.now().toString(), appVersion = BuildConfig.VERSION_NAME)
            val saved = withContext(Dispatchers.IO) { writeText(uri, text) }
            val message = if (saved) R.string.settings_backup_saved else R.string.settings_backup_error_write
            _state.update { it.copy(working = false, messageRes = message) }
        }
    }

    /** Reads and validates [uri]; a valid backup becomes [BackupUiState.pending] for confirmation. */
    fun read(uri: Uri) {
        _state.update { it.copy(working = true, messageRes = null, pending = null) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { readBackup(uri) }
            _state.update { current ->
                when (result) {
                    is BackupDecodeResult.Success -> current.copy(working = false, pending = PendingImport(result.content, result.createdAt))
                    is BackupDecodeResult.Failure -> current.copy(working = false, messageRes = messageFor(result.problem))
                }
            }
        }
    }

    fun cancelImport() {
        _state.update { it.copy(pending = null) }
    }

    /** Applies the pending backup on the app scope, so leaving the screen can't cut a restore in half. */
    fun confirmImport() {
        val pending = _state.value.pending ?: return
        _state.update { it.copy(pending = null, working = true) }
        appScope.launch {
            val restored = restore(pending.content)
            val message = if (restored) R.string.settings_backup_restored else R.string.settings_backup_error_apply
            _state.update { it.copy(working = false, messageRes = message) }
            val language = pending.content.settings.language
            if (restored && AppLocales.isSupported && AppLocales.current(appContext) != language) {
                withContext(Dispatchers.Main) { AppLocales.apply(appContext, language) }
            }
        }
    }

    /** No document picker on this phone (very rare). */
    fun reportNoFilePicker() {
        _state.update { it.copy(working = false, messageRes = R.string.core_error_no_handler) }
    }

    private suspend fun restore(content: BackupContent): Boolean {
        val before = BackupContent(settings.settings.value, overrides.overrides.value, focus.config.value)
        return try {
            settings.update { BackupSanitizer.settings(content.settings) }
            overrides.update { BackupSanitizer.overrides(content.overrides) }
            focus.update { current -> BackupSanitizer.focus(content.focus, current) }
            true
        } catch (e: IOException) {
            rollBack(before)
            false
        }
    }

    /** Best effort: puts back what was there before a restore that failed half way. */
    private suspend fun rollBack(before: BackupContent) {
        try {
            settings.update { before.settings }
            overrides.update { before.overrides }
            focus.update { current -> before.focus.copy(session = current.session, extensions = current.extensions) }
        } catch (e: IOException) {
            // Storage keeps failing; the error message is already on its way.
        }
    }

    private fun readBackup(uri: Uri): BackupDecodeResult {
        val bytes = try {
            appContext.contentResolver.openInputStream(uri)?.use { readAtMost(it, BackupRules.MAX_BYTES + 1) }
        } catch (e: IOException) {
            null
        } catch (e: SecurityException) {
            null
        }
        return when {
            bytes == null -> BackupDecodeResult.Failure(BackupProblem.Unreadable)
            bytes.size > BackupRules.MAX_BYTES -> BackupDecodeResult.Failure(BackupProblem.TooLarge)
            else -> BackupCodec.decode(bytes.decodeToString())
        }
    }

    private fun readAtMost(input: InputStream, limit: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val chunk = ByteArray(8 * 1024)
        while (out.size() < limit) {
            val read = input.read(chunk, 0, minOf(chunk.size, limit - out.size()))
            if (read < 0) break
            out.write(chunk, 0, read)
        }
        return out.toByteArray()
    }

    private fun writeText(uri: Uri, text: String): Boolean =
        try {
            val output = appContext.contentResolver.openOutputStream(uri)
            if (output == null) {
                false
            } else {
                output.use { it.write(text.encodeToByteArray()) }
                true
            }
        } catch (e: IOException) {
            false
        } catch (e: SecurityException) {
            false
        }

    private fun messageFor(problem: BackupProblem): Int = when (problem) {
        BackupProblem.Unreadable -> R.string.settings_backup_error_read
        BackupProblem.NotABackup -> R.string.settings_backup_error_not_backup
        BackupProblem.NewerVersion -> R.string.settings_backup_error_newer
        BackupProblem.Incomplete -> R.string.settings_backup_error_incomplete
        BackupProblem.TooLarge -> R.string.settings_backup_error_too_large
    }
}
