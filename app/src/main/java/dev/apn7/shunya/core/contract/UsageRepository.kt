package dev.apn7.shunya.core.contract

import dev.apn7.shunya.core.model.AppUsage
import dev.apn7.shunya.core.model.DayUsage
import dev.apn7.shunya.core.model.HourUsage
import dev.apn7.shunya.core.model.UsageSummary
import java.time.LocalDate

/**
 * Screen-time data from `UsageStatsManager`. "Today" means since
 * local midnight. Without usage access every function returns empty/zero values; it never throws.
 * The suspend functions do their own I/O off the main thread.
 */
interface UsageRepository {

    /** True when usage access is granted. Cheap; fine to call from the main thread. */
    fun hasAccess(): Boolean

    /** Today's total, unlock count and per-app list (most used first). */
    suspend fun today(): UsageSummary

    /** Today's foreground time and opens for one package ("Used 23m today · opened 5×"). */
    suspend fun appToday(packageName: String): AppUsage

    /** Per-app totals over the last [days] days including today, most used first ("Most used" sort). */
    suspend fun ranking(days: Int): List<AppUsage>

    /** Daily totals for the last [days] days including today, oldest first (7-day chart). */
    suspend fun lastDays(days: Int): List<DayUsage>

    /** 24 hourly buckets of [packageName]'s foreground time on [date] (app detail chart). */
    suspend fun hourly(packageName: String, date: LocalDate): List<HourUsage>
}
