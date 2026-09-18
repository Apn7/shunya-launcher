package dev.apn7.shunya.feature.home.logic

/** The one status line shown on home (PRD 3.1). */
sealed interface HomeStatus {

    /** "Focus · 23m left"; [remainingMillis] is null for focus "until I stop". */
    data class Focus(val remainingMillis: Long?) : HomeStatus

    /** "4 notifications held". */
    data class HeldNotifications(val count: Int) : HomeStatus

    /** "Screen time today 1h 12m". */
    data class ScreenTime(val millis: Long) : HomeStatus

    companion object {
        /**
         * The most relevant line: running focus first, then held notifications, then today's
         * screen time ([screenTimeMillis] is null without usage access). Null when none applies.
         */
        fun pick(
            focusActive: Boolean,
            focusEndsAt: Long?,
            nowMillis: Long,
            heldCount: Int,
            screenTimeMillis: Long?,
        ): HomeStatus? = when {
            focusActive -> Focus(focusEndsAt?.let { ClockTicks.ceilToMinute(it - nowMillis) })
            heldCount > 0 -> HeldNotifications(heldCount)
            screenTimeMillis != null -> ScreenTime(screenTimeMillis)
            else -> null
        }
    }
}
