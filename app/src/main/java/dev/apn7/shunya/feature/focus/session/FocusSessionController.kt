package dev.apn7.shunya.feature.focus.session

import dev.apn7.shunya.core.contract.FocusController
import dev.apn7.shunya.core.data.FocusConfigRepository
import dev.apn7.shunya.core.model.FocusConfig
import dev.apn7.shunya.core.model.FocusSession
import dev.apn7.shunya.core.model.FocusStatus
import dev.apn7.shunya.core.model.ProductLimits
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import java.time.ZoneId

/**
 * [FocusController] over [FocusConfigRepository]. The session lives in the stored config (so it
 * survives restarts); schedules are evaluated on demand against the local clock.
 *
 * [status] re-evaluates whenever the config changes and on a ticker that wakes at the next minute
 * boundary (when schedules can start or end) or at the session end, only while someone collects it.
 * [onScheduleGrayscale] is told when "grayscale during this schedule" should start (true) or stop.
 */
class FocusSessionController(
    private val configRepository: FocusConfigRepository,
    scope: CoroutineScope,
    private val onScheduleGrayscale: (Boolean) -> Unit = {},
) : FocusController {

    /** Last grayscale wish sent, so only changes are forwarded (a manual toggle meanwhile is respected). */
    private var lastGrayscaleWanted: Boolean? = null

    override val status: StateFlow<FocusStatus> = statusUpdates()
        .stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), statusNow())

    /** Focus state right now, computed on demand (does not depend on anyone collecting [status]). */
    fun statusNow(): FocusStatus = FocusEvaluator.status(configRepository.config.value, System.currentTimeMillis(), ZoneId.systemDefault())

    override fun startSession(minutes: Int?) {
        val now = System.currentTimeMillis()
        val endsAt = minutes?.takeIf { it > 0 }?.let { now + it * MINUTE_MILLIS }
        configRepository.edit { it.copy(session = FocusSession(startedAt = now, endsAt = endsAt)) }
    }

    override fun endSession() {
        configRepository.edit { it.copy(session = null) }
    }

    override fun toggleSession() {
        val config = configRepository.config.value
        if (config.session?.isActiveAt(System.currentTimeMillis()) == true) {
            endSession()
        } else {
            startSession(config.sessionDurationsMinutes.firstOrNull() ?: ProductLimits.FOCUS_DURATIONS_DEFAULT.first())
        }
    }

    override fun isBlockedNow(packageName: String): Boolean =
        FocusEvaluator.isBlocked(configRepository.config.value, packageName, System.currentTimeMillis(), ZoneId.systemDefault())

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun statusUpdates(): Flow<FocusStatus> = configRepository.config
        .flatMapLatest { config -> ticks(config) }
        .distinctUntilChanged()
        .onEach { syncGrayscale(it) }

    /** Emits the status now and again whenever it can next change, until the config changes. */
    private fun ticks(config: FocusConfig): Flow<FocusStatus> = flow {
        while (true) {
            val now = System.currentTimeMillis()
            emit(FocusEvaluator.status(config, now, ZoneId.systemDefault()))
            delay(nextCheckDelay(config, now))
        }
    }

    /** Until the next whole minute (schedules change only then) or the session end, whichever is sooner. */
    private fun nextCheckDelay(config: FocusConfig, now: Long): Long {
        val toNextMinute = MINUTE_MILLIS - now % MINUTE_MILLIS + TICK_SLACK_MILLIS
        val sessionEnd = config.session?.endsAt
        val toSessionEnd = if (sessionEnd != null && sessionEnd > now) sessionEnd - now + TICK_SLACK_MILLIS else Long.MAX_VALUE
        return minOf(toNextMinute, toSessionEnd).coerceIn(TICK_SLACK_MILLIS, MINUTE_MILLIS + TICK_SLACK_MILLIS)
    }

    private fun syncGrayscale(status: FocusStatus) {
        // The stored config is read a few ms after start; deciding on the defaults would flicker grayscale.
        if (!configRepository.isLoaded.value) return
        val wanted = status.activeSchedules.any { it.grayscale }
        if (wanted == lastGrayscaleWanted) return
        lastGrayscaleWanted = wanted
        onScheduleGrayscale(wanted)
    }

    private companion object {
        const val MINUTE_MILLIS = 60_000L
        const val TICK_SLACK_MILLIS = 50L
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
