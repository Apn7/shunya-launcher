package dev.apn7.shunya.feature.focus.policy

import dev.apn7.shunya.core.contract.LaunchPolicy
import dev.apn7.shunya.core.contract.UsageRepository
import dev.apn7.shunya.core.data.FocusConfigRepository
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.AppUsage
import dev.apn7.shunya.core.model.FocusConfig
import dev.apn7.shunya.core.model.FocusStatus
import dev.apn7.shunya.core.model.LaunchDecision
import dev.apn7.shunya.core.model.ProductLimits
import dev.apn7.shunya.feature.focus.logic.ActiveFocus
import dev.apn7.shunya.feature.focus.logic.LaunchFacts
import dev.apn7.shunya.feature.focus.logic.LaunchRules
import dev.apn7.shunya.feature.focus.logic.LaunchVerdict
import dev.apn7.shunya.feature.focus.logic.LimitExtension
import kotlinx.coroutines.CancellationException
import java.time.LocalDate

/**
 * [LaunchPolicy] for the focus rules ([LaunchRules]: Blocked > LimitReached > Pause > Allow).
 *
 * Fast path: apps that are neither distracting nor limited are allowed without touching usage data.
 * Usage is read only when it matters (a limit, or the pause screen's "47m today" line) and comes from
 * the repository's short cache. [focusNow] evaluates sessions and schedules on demand, so blocking is
 * correct even when nobody is watching `FocusController.status`. Any failure means [LaunchDecision.Allow].
 */
class FocusLaunchPolicy(
    private val configRepository: FocusConfigRepository,
    private val usageRepository: UsageRepository,
    private val focusNow: () -> FocusStatus,
) : LaunchPolicy {

    override suspend fun decide(app: AppKey): LaunchDecision {
        val config = configRepository.config.value
        val pkg = app.packageName
        val distracting = config.isDistracting(pkg)
        val limit = config.limitMinutesFor(pkg)?.takeIf { it > 0 }
        if (!distracting && limit == null) return LaunchDecision.Allow
        return try {
            val focus = if (distracting) activeFocus() else null
            // A block needs no usage numbers; skip the query.
            val usage = if (focus == null) usageRepository.appToday(pkg) else AppUsage.none(pkg)
            LaunchRules.decide(facts(config, pkg, distracting, limit, focus, usage)).toDecision()
        } catch (e: CancellationException) {
            throw e
        } catch (e: RuntimeException) {
            LaunchDecision.Allow
        }
    }

    private fun facts(
        config: FocusConfig,
        pkg: String,
        distracting: Boolean,
        limit: Int?,
        focus: ActiveFocus?,
        usage: AppUsage,
    ): LaunchFacts {
        val stored = config.extensions
        return LaunchFacts(
            distracting = distracting,
            pauseSeconds = config.pauseSecondsFor(pkg).coerceIn(ProductLimits.PAUSE_SECONDS_MIN, ProductLimits.PAUSE_SECONDS_MAX),
            focus = focus,
            limitMinutes = limit,
            extension = stored.minutesByPackage[pkg]?.let { LimitExtension(stored.date, it) },
            today = LocalDate.now().toString(),
            usedTodayMillis = usage.foregroundMillis,
            opensToday = usage.launchCount,
        )
    }

    /** Active focus, naming the schedule only when no manual session is running (the gate then offers "End focus"). */
    private fun activeFocus(): ActiveFocus? {
        val status = focusNow()
        if (!status.isActive) return null
        val scheduleName = if (status.session == null) status.activeSchedules.firstOrNull()?.name else null
        return ActiveFocus(endsAt = status.endsAt, scheduleName = scheduleName)
    }

    private fun LaunchVerdict.toDecision(): LaunchDecision = when (this) {
        LaunchVerdict.Allow -> LaunchDecision.Allow
        is LaunchVerdict.Pause -> LaunchDecision.Pause(seconds, usedTodayMillis, opensToday)
        is LaunchVerdict.LimitReached -> LaunchDecision.LimitReached(limitMinutes, usedTodayMillis, canExtend)
        is LaunchVerdict.Blocked -> LaunchDecision.Blocked(endsAt, scheduleName)
    }
}
