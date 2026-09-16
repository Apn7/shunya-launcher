package dev.apn7.shunya.core.model

/** What should happen when the user opens an app. Produced by `LaunchPolicy.decide`. */
sealed interface LaunchDecision {

    /** Open the app right away. */
    data object Allow : LaunchDecision

    /** Mindful pause: show the gate with a [pauseSeconds] countdown and today's usage first. */
    data class Pause(
        val pauseSeconds: Int,
        val usedTodayMillis: Long,
        val opensToday: Int,
    ) : LaunchDecision

    /**
     * The daily limit is used up (including any extension). [canExtend] is true when today's
     * one "5 more minutes" extension is still available.
     */
    data class LimitReached(
        val limitMinutes: Int,
        val usedTodayMillis: Long,
        val canExtend: Boolean,
    ) : LaunchDecision

    /**
     * Blocked by a focus session or schedule. [endsAt] is epoch millis, or null when focus runs
     * until the user stops it. [scheduleName] is set when a schedule causes the block.
     */
    data class Blocked(
        val endsAt: Long?,
        val scheduleName: String? = null,
    ) : LaunchDecision
}
