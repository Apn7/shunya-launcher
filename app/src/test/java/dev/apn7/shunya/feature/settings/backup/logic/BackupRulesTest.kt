package dev.apn7.shunya.feature.settings.backup.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRulesTest {

    private fun check(format: String? = BackupRules.FORMAT, version: Int? = 1, all: Boolean = true) =
        BackupRules.check(format, version, hasSettings = all, hasOverrides = true, hasFocus = true)

    @Test
    fun currentBackupPasses() {
        assertNull(check())
    }

    @Test
    fun wrongOrMissingFormatIsNotABackup() {
        assertEquals(BackupProblem.NotABackup, check(format = "something-else"))
        assertEquals(BackupProblem.NotABackup, check(format = null))
        assertEquals(BackupProblem.NotABackup, check(version = null))
        assertEquals(BackupProblem.NotABackup, check(version = 0))
    }

    @Test
    fun newerVersionIsRefused() {
        assertEquals(BackupProblem.NewerVersion, check(version = BackupRules.VERSION + 1))
    }

    @Test
    fun missingSectionIsIncomplete() {
        assertEquals(BackupProblem.Incomplete, check(all = false))
        assertEquals(
            BackupProblem.Incomplete,
            BackupRules.check(BackupRules.FORMAT, 1, hasSettings = true, hasOverrides = true, hasFocus = false),
        )
    }

    @Test
    fun fileNameCarriesTheDate() {
        assertEquals("shunya-backup-2026-09-22.json", BackupRules.fileName("2026-09-22"))
    }

    @Test
    fun valuesAreClamped() {
        assertEquals(80, BackupRules.clamp(500, 0, 80))
        assertEquals(0, BackupRules.clamp(-3, 0, 80))
        assertEquals(mapOf("a" to 3, "b" to 30), BackupRules.clampedValues(mapOf("a" to 1, "b" to 99, " " to 10), 3, 30))
    }

    @Test
    fun dailyLimitsDropNonsense() {
        val limits = mapOf("yt" to 30, "zero" to 0, "neg" to -5, "huge" to 5000, "" to 20)
        assertEquals(mapOf("yt" to 30), BackupRules.dailyLimits(limits))
    }

    @Test
    fun durationsAreCleanedWithFallback() {
        assertEquals(listOf(25, 45), BackupRules.durations(listOf(45, 25, 45, 0, 2000), listOf(60)))
        assertEquals(listOf(60), BackupRules.durations(listOf(-1, 0), listOf(60)))
    }

    @Test
    fun favoriteIdsAreDistinctAndCapped() {
        assertEquals(listOf("a", "b"), BackupRules.favoriteIds(listOf("a", "", "a", "b", "c"), 2))
        assertEquals(emptyList<String>(), BackupRules.favoriteIds(listOf("a"), -1))
    }

    @Test
    fun minutesOfDayAndDays() {
        assertTrue(BackupRules.isMinuteOfDay(0))
        assertTrue(BackupRules.isMinuteOfDay(1439))
        assertFalse(BackupRules.isMinuteOfDay(1440))
        assertFalse(BackupRules.isMinuteOfDay(-1))
        assertEquals(setOf(1, 7), BackupRules.validDays(setOf(0, 1, 7, 8)))
    }
}
