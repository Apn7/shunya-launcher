package dev.apn7.shunya.feature.focus.usage

import dev.apn7.shunya.core.contract.UsageRepository
import dev.apn7.shunya.core.model.AppUsage
import dev.apn7.shunya.core.model.DayUsage
import dev.apn7.shunya.core.model.HourUsage
import dev.apn7.shunya.core.model.UsageSummary
import java.time.LocalDate

/** [UsageRepository] over `UsageStatsManager`. Stub behaviour: no access, no data. */
class UsageStatsRepository : UsageRepository {

    override fun hasAccess(): Boolean = false

    override suspend fun today(): UsageSummary = UsageSummary.empty(LocalDate.now())

    override suspend fun appToday(packageName: String): AppUsage = AppUsage.none(packageName)

    override suspend fun ranking(days: Int): List<AppUsage> = emptyList()

    override suspend fun lastDays(days: Int): List<DayUsage> = emptyList()

    override suspend fun hourly(packageName: String, date: LocalDate): List<HourUsage> = emptyList()
}
