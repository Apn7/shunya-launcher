package dev.apn7.shunya.feature.focus.logic

/** A run of consecutive ISO days, e.g. Sunday to Thursday: first = 7, last = 4, length = 5. */
data class DayRun(
    val first: Int,
    val last: Int,
    val length: Int,
)

/**
 * Week-order helpers for schedule day pickers and summaries ("Every day", "Sun–Thu", "Mon, Wed").
 * Days are ISO numbers (1 = Monday … 7 = Sunday); the week may start on any day.
 */
object DayRanges {

    const val DAYS_PER_WEEK = 7

    /** The seven days in display order, starting at [firstDay]. */
    fun weekOrder(firstDay: Int): List<Int> {
        val start = firstDay.coerceIn(1, DAYS_PER_WEEK)
        return (0 until DAYS_PER_WEEK).map { offset -> (start - 1 + offset) % DAYS_PER_WEEK + 1 }
    }

    /**
     * Runs of consecutive selected [days], ordered by where each run starts in the week that begins
     * at [firstDay]. A run over the end of the week stays whole: Sun–Thu is one run even when the
     * week starts on Monday. All seven days give one run starting at [firstDay].
     */
    fun runs(days: Set<Int>, firstDay: Int): List<DayRun> {
        val selected = days.filter { it in 1..DAYS_PER_WEEK }.toSet()
        val order = weekOrder(firstDay)
        if (selected.isEmpty()) return emptyList()
        if (selected.size == DAYS_PER_WEEK) return listOf(DayRun(order.first(), order.last(), DAYS_PER_WEEK))
        // Scan from a day that is not selected, so no run is cut in two by the end of the week.
        val gap = order.indexOfFirst { it !in selected }
        val result = ArrayList<DayRun>()
        var runFirst = 0
        var runLength = 0
        for (offset in 0 until DAYS_PER_WEEK) {
            val day = order[(gap + offset) % DAYS_PER_WEEK]
            if (day in selected) {
                if (runLength == 0) runFirst = day
                runLength += 1
            } else if (runLength > 0) {
                result.add(DayRun(runFirst, lastOf(runFirst, runLength), runLength))
                runLength = 0
            }
        }
        if (runLength > 0) result.add(DayRun(runFirst, lastOf(runFirst, runLength), runLength))
        return result.sortedBy { order.indexOf(it.first) }
    }

    private fun lastOf(first: Int, length: Int): Int = (first - 1 + length - 1) % DAYS_PER_WEEK + 1
}
