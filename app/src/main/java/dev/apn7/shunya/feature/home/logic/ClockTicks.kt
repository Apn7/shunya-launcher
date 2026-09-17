package dev.apn7.shunya.feature.home.logic

/**
 * Delays for a clock that changes exactly when the minute (or second) does, instead of drifting
 * with a fixed 60 s timer. Epoch millis are used as is: every current time zone offset is a
 * whole number of minutes, so epoch minute boundaries are wall-clock minute boundaries.
 */
object ClockTicks {

    private const val SECOND_MILLIS = 1_000L
    private const val MINUTE_MILLIS = 60_000L

    /** Millis from [nowMillis] until the next full minute (1..60 000). */
    fun millisUntilNextMinute(nowMillis: Long): Long = MINUTE_MILLIS - nowMillis.mod(MINUTE_MILLIS)

    /** Millis from [nowMillis] until the next full second (1..1 000). */
    fun millisUntilNextSecond(nowMillis: Long): Long = SECOND_MILLIS - nowMillis.mod(SECOND_MILLIS)

    /** [millis] rounded up to whole minutes, for countdowns: 24m10s left reads "25m". */
    fun ceilToMinute(millis: Long): Long {
        if (millis <= 0L) return 0L
        return (millis + MINUTE_MILLIS - 1) / MINUTE_MILLIS * MINUTE_MILLIS
    }
}
