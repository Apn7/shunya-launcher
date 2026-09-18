package dev.apn7.shunya.feature.focus.usage

import android.annotation.SuppressLint
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import dev.apn7.shunya.core.contract.UsageRepository
import dev.apn7.shunya.core.model.AppUsage
import dev.apn7.shunya.core.model.DayUsage
import dev.apn7.shunya.core.model.HourUsage
import dev.apn7.shunya.core.model.UsageSummary
import dev.apn7.shunya.core.system.ShunyaComponents
import dev.apn7.shunya.feature.focus.logic.TimeBuckets
import dev.apn7.shunya.feature.focus.logic.UsageEvent
import dev.apn7.shunya.feature.focus.logic.UsageEventType
import dev.apn7.shunya.feature.focus.logic.UsageTimeline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

/**
 * [UsageRepository] over `UsageStatsManager.queryEvents`. Foreground time and opens are replayed from
 * activity resume/pause events by [UsageTimeline], so days start at local midnight (the system's own
 * daily buckets do not). Shunya itself is left out of every number.
 *
 * One timeline covering "first requested day … now" is kept for [CACHE_MILLIS]: the launch policy
 * (every tap), the gate and the screen-time screens share one query. All reads run on [Dispatchers.IO].
 */
class UsageStatsRepository(context: Context) : UsageRepository {

    private val appContext = context.applicationContext
    private val ownPackage = appContext.packageName
    private val manager: UsageStatsManager? = appContext.getSystemService(UsageStatsManager::class.java)
    private val mutex = Mutex()
    private var cached: CachedTimeline? = null

    override fun hasAccess(): Boolean = UsageAccess.isGranted(appContext)

    override suspend fun today(): UsageSummary {
        val zone = ZoneId.systemDefault()
        val date = LocalDate.now(zone)
        val timeline = recentTimeline(date, zone) ?: return UsageSummary.empty(date)
        val from = TimeBuckets.startOfDay(date, zone)
        val apps = appUsages(timeline, from)
        val unlocks = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) timeline.unlockCount(from, timeline.windowEnd) else null
        return UsageSummary(date = date, totalMillis = apps.sumOf { it.foregroundMillis }, unlockCount = unlocks, apps = apps)
    }

    override suspend fun appToday(packageName: String): AppUsage {
        val zone = ZoneId.systemDefault()
        val date = LocalDate.now(zone)
        val timeline = recentTimeline(date, zone) ?: return AppUsage.none(packageName)
        val from = TimeBuckets.startOfDay(date, zone)
        return AppUsage(
            packageName = packageName,
            foregroundMillis = timeline.foregroundOf(packageName, from, timeline.windowEnd),
            launchCount = timeline.opensOf(packageName, from, timeline.windowEnd),
        )
    }

    override suspend fun ranking(days: Int): List<AppUsage> {
        val zone = ZoneId.systemDefault()
        val first = LocalDate.now(zone).minusDays((days.coerceAtLeast(1) - 1).toLong())
        val timeline = recentTimeline(first, zone) ?: return emptyList()
        return appUsages(timeline, TimeBuckets.startOfDay(first, zone))
    }

    override suspend fun lastDays(days: Int): List<DayUsage> {
        val count = days.coerceAtLeast(1)
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val first = today.minusDays((count - 1).toLong())
        val timeline = recentTimeline(first, zone) ?: return emptyList()
        val totals = timeline.bucketMillis(TimeBuckets.dayBoundaries(today, count, zone))
        return totals.mapIndexed { index, millis -> DayUsage(first.plusDays(index.toLong()), millis) }
    }

    override suspend fun hourly(packageName: String, date: LocalDate): List<HourUsage> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val timeline = when {
            date.isAfter(today) -> null
            date == today -> recentTimeline(date, zone)
            else -> pastDayTimeline(date, zone)
        } ?: return emptyList()
        val buckets = timeline.bucketMillis(TimeBuckets.hourBoundaries(date, zone), packageName)
        return buckets.mapIndexed { hour, millis -> HourUsage(hour, millis) }
    }

    /** Per-app usage from [from] to the end of [timeline], most used first. */
    private fun appUsages(timeline: UsageTimeline, from: Long): List<AppUsage> {
        val opens = timeline.opensByPackage(from, timeline.windowEnd)
        return timeline.foregroundByPackage(from, timeline.windowEnd)
            .map { (pkg, millis) -> AppUsage(pkg, millis, opens[pkg] ?: 0) }
            .sortedByDescending { it.foregroundMillis }
    }

    /** Timeline from the start of [firstDay] until now; reused while a fresh cached one covers it. */
    private suspend fun recentTimeline(firstDay: LocalDate, zone: ZoneId): UsageTimeline? {
        if (!hasAccess()) return null
        val from = TimeBuckets.startOfDay(firstDay, zone)
        return mutex.withLock {
            val now = System.currentTimeMillis()
            val hit = cached?.takeIf { it.covers(from, now) }
            if (hit != null) {
                hit.timeline
            } else {
                val fresh = withContext(Dispatchers.IO) { load(from, now) }
                cached = fresh?.let { CachedTimeline(it, builtAt = now) }
                fresh
            }
        }
    }

    /** A whole past day; not cached (only the app detail asks for it). */
    private suspend fun pastDayTimeline(date: LocalDate, zone: ZoneId): UsageTimeline? {
        if (!hasAccess()) return null
        val from = TimeBuckets.startOfDay(date, zone)
        val to = TimeBuckets.startOfDay(date.plusDays(1), zone)
        return withContext(Dispatchers.IO) { load(from, to) }
    }

    /** Reads the events (plus a look-back for apps already open at [from]) and builds `[from, to)`. */
    private fun load(from: Long, to: Long): UsageTimeline? {
        if (to <= from) return UsageTimeline.build(emptyList(), from, from)
        val events = readEvents(from - LOOK_BACK_MILLIS, to) ?: return null
        return UsageTimeline.build(events, from, to, ignoredPackages = setOf(ownPackage))
    }

    /** Raw events in `[from, to)`, or null when the system refuses (access revoked, user locked). */
    private fun readEvents(from: Long, to: Long): List<UsageEvent>? {
        val usageStats = manager ?: return null
        return try {
            val events = usageStats.queryEvents(from, to) ?: return null
            val out = ArrayList<UsageEvent>()
            val event = UsageEvents.Event()
            while (events.hasNextEvent() && events.getNextEvent(event)) {
                val type = eventType(event.eventType) ?: continue
                val pkg = event.packageName.orEmpty()
                val cls = event.className.orEmpty()
                // The gate is an interstitial in front of the app, not a destination of its own.
                if (pkg == ownPackage && cls == ShunyaComponents.GATE_ACTIVITY) continue
                out.add(UsageEvent(event.timeStamp, type, pkg, cls))
            }
            out
        } catch (e: SecurityException) {
            null
        } catch (e: IllegalStateException) {
            null
        }
    }

    /**
     * Maps a `UsageEvents.Event` type. The named constants are compile-time ints (inlined), so this
     * is safe on Android 8: older phones simply never send the newer types. 1 and 2 are the same
     * values as the old MOVE_TO_FOREGROUND / MOVE_TO_BACKGROUND.
     */
    @SuppressLint("InlinedApi")
    private fun eventType(type: Int): UsageEventType? = when (type) {
        UsageEvents.Event.ACTIVITY_RESUMED -> UsageEventType.Resumed
        UsageEvents.Event.ACTIVITY_PAUSED -> UsageEventType.Paused
        UsageEvents.Event.ACTIVITY_STOPPED -> UsageEventType.Stopped
        UsageEvents.Event.SCREEN_NON_INTERACTIVE,
        UsageEvents.Event.DEVICE_SHUTDOWN,
        UsageEvents.Event.DEVICE_STARTUP -> UsageEventType.AllStopped
        UsageEvents.Event.KEYGUARD_HIDDEN -> UsageEventType.Unlock
        else -> null
    }

    private class CachedTimeline(val timeline: UsageTimeline, val builtAt: Long) {
        fun covers(from: Long, now: Long): Boolean =
            timeline.windowStart <= from && now >= builtAt && now - builtAt < CACHE_MILLIS
    }

    private companion object {
        /** How long a timeline is reused: the policy may see usage up to this much behind. */
        const val CACHE_MILLIS = 20_000L

        /** Events read before the window, so an app opened shortly before midnight counts from midnight. */
        const val LOOK_BACK_MILLIS = 3 * 60 * 60 * 1000L
    }
}
