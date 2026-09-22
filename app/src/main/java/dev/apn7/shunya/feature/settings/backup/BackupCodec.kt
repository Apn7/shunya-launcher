package dev.apn7.shunya.feature.settings.backup

import dev.apn7.shunya.core.data.ShunyaJson
import dev.apn7.shunya.core.model.AppOverrides
import dev.apn7.shunya.core.model.FocusConfig
import dev.apn7.shunya.core.model.LauncherSettings
import dev.apn7.shunya.core.model.LimitExtensions
import dev.apn7.shunya.feature.settings.backup.logic.BackupProblem
import dev.apn7.shunya.feature.settings.backup.logic.BackupRules
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

/** What a backup restores. Held notifications are deliberately not part of it (PRD 3.5). */
data class BackupContent(
    val settings: LauncherSettings,
    val overrides: AppOverrides,
    val focus: FocusConfig,
)

/** Result of reading a backup file. */
sealed interface BackupDecodeResult {
    data class Success(val content: BackupContent, val createdAt: String) : BackupDecodeResult

    data class Failure(val problem: BackupProblem) : BackupDecodeResult
}

/**
 * The backup file on disk. Never rename a field; add new ones with defaults. Sections are
 * nullable so a missing one is reported as [BackupProblem.Incomplete] instead of silently
 * becoming defaults.
 */
@Serializable
internal data class BackupFile(
    val format: String = "",
    val version: Int = 0,
    /** ISO local date of the export, e.g. "2026-09-22". */
    val createdAt: String = "",
    val appVersion: String = "",
    val settings: LauncherSettings? = null,
    val appOverrides: AppOverrides? = null,
    val focus: FocusConfig? = null,
)

/**
 * JSON encoding of backups with [ShunyaJson] (forgiving: unknown keys and enum names fall back to
 * defaults). No Android dependencies, so a plain JVM unit test covers the round trip.
 */
object BackupCodec {

    private val prettyJson: Json = Json(from = ShunyaJson) { prettyPrint = true }

    /** The file text for [content]. A running focus session and today's limit extensions are left out. */
    fun encode(content: BackupContent, createdAt: String, appVersion: String): String {
        val file = BackupFile(
            format = BackupRules.FORMAT,
            version = BackupRules.VERSION,
            createdAt = createdAt,
            appVersion = appVersion,
            settings = content.settings,
            appOverrides = content.overrides,
            focus = content.focus.copy(session = null, extensions = LimitExtensions()),
        )
        return prettyJson.encodeToString(BackupFile.serializer(), file)
    }

    /** Validates the header first, then decodes every section; nothing is applied here. */
    fun decode(text: String): BackupDecodeResult {
        val root = try {
            ShunyaJson.parseToJsonElement(text)
        } catch (e: IllegalArgumentException) {
            return BackupDecodeResult.Failure(BackupProblem.Unreadable)
        }
        val header = root as? JsonObject ?: return BackupDecodeResult.Failure(BackupProblem.NotABackup)
        val problem = BackupRules.check(
            format = (header["format"] as? JsonPrimitive)?.contentOrNull,
            version = (header["version"] as? JsonPrimitive)?.intOrNull,
            hasSettings = header["settings"] is JsonObject,
            hasOverrides = header["appOverrides"] is JsonObject,
            hasFocus = header["focus"] is JsonObject,
        )
        if (problem != null) return BackupDecodeResult.Failure(problem)

        val file = try {
            ShunyaJson.decodeFromJsonElement(BackupFile.serializer(), header)
        } catch (e: IllegalArgumentException) {
            return BackupDecodeResult.Failure(BackupProblem.Unreadable)
        } catch (e: IllegalStateException) {
            return BackupDecodeResult.Failure(BackupProblem.Unreadable)
        }
        val settings = file.settings
        val overrides = file.appOverrides
        val focus = file.focus
        if (settings == null || overrides == null || focus == null) {
            return BackupDecodeResult.Failure(BackupProblem.Incomplete)
        }
        return BackupDecodeResult.Success(BackupContent(settings, overrides, focus), file.createdAt)
    }
}
