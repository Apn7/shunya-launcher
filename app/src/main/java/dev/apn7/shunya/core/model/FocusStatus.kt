package dev.apn7.shunya.core.model

/** Live focus state for the home status line, the focus hub and the gate. */
data class FocusStatus(
    /** The running manual session, or null. */
    val session: FocusSession? = null,
    /** Enabled schedules whose window contains "now". */
    val activeSchedules: List<Schedule> = emptyList(),
    /** When blocking ends (epoch millis): null while inactive or when focus is open-ended. */
    val endsAt: Long? = null,
) {
    /** True while a session or any schedule is active: distracting apps are blocked. */
    val isActive: Boolean get() = session != null || activeSchedules.isNotEmpty()

    companion object {
        val Inactive: FocusStatus = FocusStatus()
    }
}
