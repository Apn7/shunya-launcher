package dev.apn7.shunya.feature.settings.backup

import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.AppOverrides
import dev.apn7.shunya.core.model.FocusConfig
import dev.apn7.shunya.core.model.FocusSession
import dev.apn7.shunya.core.model.GestureAction
import dev.apn7.shunya.core.model.GestureBinding
import dev.apn7.shunya.core.model.HomeGesture
import dev.apn7.shunya.core.model.LauncherSettings
import dev.apn7.shunya.core.model.LimitExtensions
import dev.apn7.shunya.core.model.NotificationFilterMode
import dev.apn7.shunya.core.model.NotificationPrefs
import dev.apn7.shunya.core.model.Schedule
import dev.apn7.shunya.core.model.ThemeChoice
import dev.apn7.shunya.feature.settings.backup.logic.BackupProblem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Backup JSON round trip (PRD 4). Needs the kotlinx.serialization compiler plugin, so it runs with
 * `gradlew testDebugUnitTest`.
 */
class BackupCodecTest {

    private val maps = AppKey("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")

    private val content = BackupContent(
        settings = LauncherSettings(
            gestures = LauncherSettings().gestures.withBinding(HomeGesture.SwipeRight, GestureBinding(GestureAction.OpenApp, maps)),
            notifications = NotificationPrefs(NotificationFilterMode.Hold, setOf("com.whatsapp")),
            onboardingDone = true,
        ).let { it.copy(appearance = it.appearance.copy(theme = ThemeChoice.Ink)) },
        overrides = AppOverrides().withLabel(maps, "Map").withFavorite(maps, favorite = true),
        focus = FocusConfig(
            distractingPackages = setOf("com.instagram.android"),
            dailyLimitMinutes = mapOf("com.google.android.youtube" to 30),
            schedules = listOf(Schedule(id = "bed", name = "Bedtime", startMinute = 23 * 60, endMinute = 7 * 60)),
            session = FocusSession(startedAt = 1L, endsAt = 2L),
            extensions = LimitExtensions("2026-09-22", mapOf("x" to 5)),
        ),
    )

    @Test
    fun roundTripKeepsEverythingButSessionAndExtensions() {
        val decoded = BackupCodec.decode(BackupCodec.encode(content, "2026-09-22", "0.1.0"))
        val expected = content.copy(focus = content.focus.copy(session = null, extensions = LimitExtensions()))
        assertEquals(BackupDecodeResult.Success(expected, "2026-09-22"), decoded)
    }

    @Test
    fun garbageIsUnreadable() {
        assertEquals(BackupDecodeResult.Failure(BackupProblem.Unreadable), BackupCodec.decode("{not json"))
    }

    @Test
    fun otherJsonIsNotABackup() {
        assertEquals(BackupDecodeResult.Failure(BackupProblem.NotABackup), BackupCodec.decode("[1, 2]"))
        assertEquals(BackupDecodeResult.Failure(BackupProblem.NotABackup), BackupCodec.decode("{\"hello\": 1}"))
    }

    @Test
    fun newerVersionAndMissingSectionsAreRefused() {
        val newer = BackupCodec.encode(content, "2026-09-22", "9.0").replace("\"version\": 1", "\"version\": 99")
        assertEquals(BackupDecodeResult.Failure(BackupProblem.NewerVersion), BackupCodec.decode(newer))
        val partial = "{\"format\": \"shunya-backup\", \"version\": 1, \"settings\": {}, \"appOverrides\": {}}"
        assertEquals(BackupDecodeResult.Failure(BackupProblem.Incomplete), BackupCodec.decode(partial))
    }

    @Test
    fun unknownKeysAreIgnored() {
        val withExtra = BackupCodec.encode(content, "2026-09-22", "0.1.0").replaceFirst("{", "{\"future\": true, ")
        assertTrue(BackupCodec.decode(withExtra) is BackupDecodeResult.Success)
    }
}
