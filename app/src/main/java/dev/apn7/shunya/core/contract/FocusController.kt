package dev.apn7.shunya.core.contract

import dev.apn7.shunya.core.model.FocusStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * Focus sessions and schedules at runtime. Rules are stored in
 * `FocusConfigRepository`; this turns them into "what is active right now".
 */
interface FocusController {

    /**
     * Live focus state. Kept current while anyone collects it: schedules start and end with the
     * clock and sessions time out, without any background alarm.
     */
    val status: StateFlow<FocusStatus>

    /** Starts (or replaces) the manual session: [minutes] long, or until [endSession] when null. */
    fun startSession(minutes: Int?)

    /** Ends the manual session. Active schedules stay in force. */
    fun endSession()

    /**
     * For the home gesture and the quick menu: ends a running session, otherwise starts one for the
     * first of `FocusConfig.sessionDurationsMinutes` (25 minutes by default).
     */
    fun toggleSession()

    /** True when [packageName] is distracting and a session or schedule is active right now. */
    fun isBlockedNow(packageName: String): Boolean
}
