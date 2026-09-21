package dev.apn7.shunya.feature.settings.backup.logic

/** Why a backup file was refused. */
enum class BackupProblem {
    /** Not JSON at all, or broken JSON. */
    Unreadable,

    /** Valid JSON, but not a Shunya backup. */
    NotABackup,

    /** Written by a newer Shunya with a format this version doesn't know. */
    NewerVersion,

    /** A Shunya backup with a section missing. */
    Incomplete,

    /** Far larger than any real backup. */
    TooLarge,
}

/**
 * Pure rules of the backup file (PRD 3.5): the header check done before anything is decoded,
 * and the clean-up applied to every restored value so a hand-edited file can't break the app.
 */
object BackupRules {

    /** Value of the "format" field of every Shunya backup. */
    const val FORMAT = "shunya-backup"

    /** Current format version; bump it (and keep reading older ones) when the layout changes. */
    const val VERSION = 1

    /** Largest file we read: real backups are a few kilobytes. */
    const val MAX_BYTES = 1_000_000

    private const val MINUTES_PER_DAY = 24 * 60

    /** Checks the header and sections; null means the file can be decoded. */
    fun check(format: String?, version: Int?, hasSettings: Boolean, hasOverrides: Boolean, hasFocus: Boolean): BackupProblem? =
        when {
            format != FORMAT || version == null || version < 1 -> BackupProblem.NotABackup
            version > VERSION -> BackupProblem.NewerVersion
            !hasSettings || !hasOverrides || !hasFocus -> BackupProblem.Incomplete
            else -> null
        }

    /** Suggested file name, e.g. `shunya-backup-2026-09-22.json` for [isoDate] "2026-09-22". */
    fun fileName(isoDate: String): String = "$FORMAT-$isoDate.json"

    fun clamp(value: Int, min: Int, max: Int): Int = value.coerceIn(min, max)

    /** Per-app values clamped into [min]..[max]; blank package names dropped. */
    fun clampedValues(values: Map<String, Int>, min: Int, max: Int): Map<String, Int> =
        values.filterKeys { it.isNotBlank() }.mapValues { (_, value) -> value.coerceIn(min, max) }

    /** Daily limits: only 1 minute .. 24 hours per app survive; blank package names dropped. */
    fun dailyLimits(limits: Map<String, Int>): Map<String, Int> =
        limits.filter { (pkg, minutes) -> pkg.isNotBlank() && minutes in 1..MINUTES_PER_DAY }

    /** Focus durations: 1 minute .. 24 hours, no duplicates, ascending; [fallback] when none is valid. */
    fun durations(minutes: List<Int>, fallback: List<Int>): List<Int> =
        minutes.filter { it in 1..MINUTES_PER_DAY }.distinct().sorted().ifEmpty { fallback }

    /** Favorite ids without blanks or duplicates, at most [max], order kept. */
    fun favoriteIds(ids: List<String>, max: Int): List<String> =
        ids.filter { it.isNotBlank() }.distinct().take(max.coerceAtLeast(0))

    /** True for a minute after midnight (0..1439). */
    fun isMinuteOfDay(minute: Int): Boolean = minute in 0 until MINUTES_PER_DAY

    /** ISO days of week (1 = Monday … 7 = Sunday) only. */
    fun validDays(days: Set<Int>): Set<Int> = days.filter { it in 1..7 }.toSet()
}
