package dev.apn7.shunya.feature.home.drawer.logic

/** "Most used" drawer order (PRD 3.2). */
object UsageOrder {

    /**
     * [items] ordered by [ranking] (package names, most used first). Items whose package is not
     * ranked follow in their original (alphabetical) order. An empty ranking, e.g. without usage
     * access, keeps [items] unchanged: the A–Z fallback.
     */
    fun <T> sort(items: List<T>, ranking: List<String>, packageOf: (T) -> String): List<T> {
        if (ranking.isEmpty()) return items
        val rankOf = HashMap<String, Int>(ranking.size * 2)
        ranking.forEachIndexed { index, pkg -> if (!rankOf.containsKey(pkg)) rankOf[pkg] = index }
        // sortedWith is stable: equal ranks keep their alphabetical order.
        return items.sortedWith(
            Comparator { a, b ->
                val rankA = rankOf[packageOf(a)] ?: Int.MAX_VALUE
                val rankB = rankOf[packageOf(b)] ?: Int.MAX_VALUE
                rankA.compareTo(rankB)
            },
        )
    }
}
