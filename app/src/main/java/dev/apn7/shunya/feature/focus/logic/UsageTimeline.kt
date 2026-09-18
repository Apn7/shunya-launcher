package dev.apn7.shunya.feature.focus.logic

/**
 * The usage events screen time is computed from. The repository maps `UsageEvents.Event` types to
 * these and drops everything else before it gets here.
 */
enum class UsageEventType {
    /** An activity came to the foreground (`ACTIVITY_RESUMED`, formerly `MOVE_TO_FOREGROUND`). */
    Resumed,

    /** An activity left the foreground (`ACTIVITY_PAUSED`, formerly `MOVE_TO_BACKGROUND`). */
    Paused,

    /** An activity is no longer visible (`ACTIVITY_STOPPED`, Android 10+). Only closes a resume whose pause is missing. */
    Stopped,

    /**
     * Nothing can be in use any more: the screen turned off (`SCREEN_NON_INTERACTIVE`, Android 9+) or the
     * phone shut down or started (`DEVICE_SHUTDOWN` / `DEVICE_STARTUP`, Android 10+).
     */
    AllStopped,

    /** The phone was unlocked (`KEYGUARD_HIDDEN`, Android 9+). */
    Unlock,
}

/** One usage event. [className] is the activity; both names are empty for device-wide events. */
data class UsageEvent(
    val timeMillis: Long,
    val type: UsageEventType,
    val packageName: String = "",
    val className: String = "",
)

/** Foreground time of one package: [startMillis] inclusive, [endMillis] exclusive. */
data class ForegroundSpan(
    val packageName: String,
    val startMillis: Long,
    val endMillis: Long,
) {
    /** Millis of this span inside `[from, to)`. */
    fun overlapMillis(from: Long, to: Long): Long =
        (minOf(endMillis, to) - maxOf(startMillis, from)).coerceAtLeast(0L)
}

/** [packageName] came to the foreground from another app at [timeMillis]. */
data class AppOpen(
    val packageName: String,
    val timeMillis: Long,
)

/**
 * Foreground spans, opens and unlocks of every app inside one window `[windowStart, windowEnd)`.
 * Build it with [build]; then ask for totals of any sub-range (a day, an hour).
 *
 * Spans of one package never overlap, so no time is counted twice.
 */
class UsageTimeline(
    val windowStart: Long,
    val windowEnd: Long,
    /** Merged spans, sorted by start. */
    val spans: List<ForegroundSpan>,
    /** Opens in time order. */
    val opens: List<AppOpen>,
    /** Unlock times in order. */
    val unlocks: List<Long>,
) {

    /** Foreground millis per package inside `[from, to)`; packages without time are left out. */
    fun foregroundByPackage(from: Long = windowStart, to: Long = windowEnd): Map<String, Long> {
        val totals = HashMap<String, Long>()
        for (span in spans) {
            val overlap = span.overlapMillis(from, to)
            if (overlap > 0L) totals[span.packageName] = (totals[span.packageName] ?: 0L) + overlap
        }
        return totals
    }

    /** Foreground millis of [packageName] inside `[from, to)`. */
    fun foregroundOf(packageName: String, from: Long = windowStart, to: Long = windowEnd): Long {
        var total = 0L
        for (span in spans) {
            if (span.packageName == packageName) total += span.overlapMillis(from, to)
        }
        return total
    }

    /** Opens per package inside `[from, to)`. */
    fun opensByPackage(from: Long = windowStart, to: Long = windowEnd): Map<String, Int> {
        val counts = HashMap<String, Int>()
        for (open in opens) {
            if (open.timeMillis >= from && open.timeMillis < to) {
                counts[open.packageName] = (counts[open.packageName] ?: 0) + 1
            }
        }
        return counts
    }

    /** Opens of [packageName] inside `[from, to)`. */
    fun opensOf(packageName: String, from: Long = windowStart, to: Long = windowEnd): Int =
        opens.count { it.packageName == packageName && it.timeMillis >= from && it.timeMillis < to }

    /** Unlocks inside `[from, to)`. */
    fun unlockCount(from: Long = windowStart, to: Long = windowEnd): Int =
        unlocks.count { it >= from && it < to }

    /**
     * Foreground millis between each pair of consecutive [boundaries] (so `boundaries.size - 1`
     * values), for [packageName] or for all packages when it is null. Used for days and hours.
     */
    fun bucketMillis(boundaries: List<Long>, packageName: String? = null): List<Long> {
        if (boundaries.size < 2) return emptyList()
        val relevant = if (packageName == null) spans else spans.filter { it.packageName == packageName }
        return (0 until boundaries.size - 1).map { index ->
            var total = 0L
            for (span in relevant) total += span.overlapMillis(boundaries[index], boundaries[index + 1])
            total
        }
    }

    companion object {

        /**
         * Replays [events] (any order; equal times keep their order) and returns what happened inside
         * `[windowStart, windowEnd)`. Events before [windowStart] may be passed as look-back: they set
         * up state (what was already open) but never count. Events at or after [windowEnd] are ignored.
         *
         * Rules:
         * - A package is in the foreground while at least one of its activities is resumed.
         * - Paused or stopped closes that activity; a pause of an activity never seen resumed means it
         *   was already in the foreground when the events begin, so it counts from [windowStart]
         *   (until the first screen-off, if one came first). A leading stop is only the tail of an
         *   earlier pause and is ignored.
         * - Screen off, shutdown and startup close everything (nothing is in use with the screen off).
         * - Still open at the end: counted until [windowEnd] (pass "now" for today).
         * - An open is counted when a package comes to the foreground and the package in front before
         *   it was a different one (switching activities inside an app or turning the screen off and
         *   on again is not a new open).
         * - [ignoredPackages] (e.g. Shunya itself) are removed from the result but still count as
         *   "another app" for opens.
         */
        fun build(
            events: List<UsageEvent>,
            windowStart: Long,
            windowEnd: Long,
            ignoredPackages: Set<String> = emptySet(),
        ): UsageTimeline {
            val builder = TimelineBuilder(windowStart, windowEnd)
            for (event in events.sortedBy { it.timeMillis }) {
                if (event.timeMillis >= windowEnd) break
                builder.accept(event)
            }
            return builder.finish(ignoredPackages)
        }
    }
}

/** Mutable replay state behind [UsageTimeline.build]. */
private class TimelineBuilder(
    private val windowStart: Long,
    private val windowEnd: Long,
) {
    private data class ActivityId(val packageName: String, val className: String)

    /** Resumed activities and since when. */
    private val open = LinkedHashMap<ActivityId, Long>()

    /** Activities with at least one event so far. */
    private val seen = HashSet<ActivityId>()

    /** The first screen-off/shutdown in the events, if any. */
    private var firstStopAllAt: Long? = null

    /** The package most recently brought to the foreground. */
    private var lastForeground: String? = null

    private val rawSpans = HashMap<String, MutableList<ForegroundSpan>>()
    private val opens = ArrayList<AppOpen>()
    private val unlocks = ArrayList<Long>()

    fun accept(event: UsageEvent) {
        when (event.type) {
            UsageEventType.Resumed -> onResumed(event)
            UsageEventType.Paused -> onClosed(event, isPause = true)
            UsageEventType.Stopped -> onClosed(event, isPause = false)
            UsageEventType.AllStopped -> stopAll(event.timeMillis)
            UsageEventType.Unlock -> if (event.timeMillis >= windowStart) unlocks.add(event.timeMillis)
        }
    }

    private fun onResumed(event: UsageEvent) {
        val pkg = event.packageName
        if (pkg.isEmpty()) return
        val id = ActivityId(pkg, event.className)
        val packageWasOpen = open.keys.any { it.packageName == pkg }
        if (id !in open) open[id] = event.timeMillis
        if (!packageWasOpen && lastForeground != pkg && event.timeMillis >= windowStart) {
            opens.add(AppOpen(pkg, event.timeMillis))
        }
        lastForeground = pkg
        seen.add(id)
    }

    private fun onClosed(event: UsageEvent, isPause: Boolean) {
        val pkg = event.packageName
        if (pkg.isEmpty()) return
        val id = ActivityId(pkg, event.className)
        val since = open.remove(id)
        if (since != null) {
            addSpan(pkg, since, event.timeMillis)
        } else if (isPause && id !in seen) {
            // Resumed before the first event we were given: in front since the window start at the latest.
            val stopAll = firstStopAllAt
            val end = if (stopAll != null) minOf(stopAll, event.timeMillis) else event.timeMillis
            addSpan(pkg, windowStart, end)
            if (lastForeground == null) lastForeground = pkg
        }
        seen.add(id)
    }

    private fun stopAll(timeMillis: Long) {
        closeOpen(timeMillis)
        if (firstStopAllAt == null) firstStopAllAt = timeMillis
    }

    private fun closeOpen(timeMillis: Long) {
        for ((id, since) in open) addSpan(id.packageName, since, timeMillis)
        open.clear()
    }

    private fun addSpan(packageName: String, start: Long, end: Long) {
        val clampedStart = maxOf(start, windowStart)
        val clampedEnd = minOf(end, windowEnd)
        if (clampedEnd > clampedStart) {
            rawSpans.getOrPut(packageName) { ArrayList() }.add(ForegroundSpan(packageName, clampedStart, clampedEnd))
        }
    }

    fun finish(ignoredPackages: Set<String>): UsageTimeline {
        closeOpen(windowEnd)
        val spans = rawSpans
            .filterKeys { it !in ignoredPackages }
            .values
            .flatMap { mergeSpans(it) }
            .sortedBy { it.startMillis }
        return UsageTimeline(
            windowStart = windowStart,
            windowEnd = windowEnd,
            spans = spans,
            opens = opens.filter { it.packageName !in ignoredPackages },
            unlocks = unlocks.toList(),
        )
    }

    /** Union of one package's spans: overlapping or touching spans become one. */
    private fun mergeSpans(spans: List<ForegroundSpan>): List<ForegroundSpan> {
        if (spans.size < 2) return spans
        val sorted = spans.sortedBy { it.startMillis }
        val merged = ArrayList<ForegroundSpan>(sorted.size)
        var current = sorted[0]
        for (index in 1 until sorted.size) {
            val next = sorted[index]
            if (next.startMillis <= current.endMillis) {
                if (next.endMillis > current.endMillis) current = current.copy(endMillis = next.endMillis)
            } else {
                merged.add(current)
                current = next
            }
        }
        merged.add(current)
        return merged
    }
}
