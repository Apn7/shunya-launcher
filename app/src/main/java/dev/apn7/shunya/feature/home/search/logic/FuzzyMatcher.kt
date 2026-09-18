package dev.apn7.shunya.feature.home.search.logic

/** How a label matched a query, best first (PRD 3.2). */
enum class MatchKind {
    /** The whole label: "youtube" → YouTube. */
    Exact,

    /** The start of the label: "you" → YouTube. */
    Prefix,

    /** The first letters of the words: "yt" → YouTube, "gm" → Google Maps. */
    WordInitials,

    /** The start of a later word: "maps" → Google Maps, "goo ma" → Google Maps. */
    WordPrefix,

    /** Anywhere inside the label: "ube" → YouTube. */
    Substring,

    /** The letters in order, with gaps: "ytb" → YouTube. */
    Subsequence,
}

/** One match: its [kind] and a [penalty] that orders matches of the same kind (lower is better). */
data class Match(val kind: MatchKind, val penalty: Int)

/** A query prepared once per keystroke. Spaces and punctuation are ignored except to split [tokens]. */
class SearchQuery(raw: String) {
    val tokens: List<String> = SearchText.queryTokens(raw)
    val compact: String = tokens.joinToString("")
    val isEmpty: Boolean get() = compact.isEmpty()
}

/**
 * Fuzzy app search: exact > prefix > word initials > word prefix > substring > subsequence,
 * case- and accent-insensitive. Pure Kotlin so it is unit tested on the JVM.
 */
object FuzzyMatcher {

    /** How [key] matches [query], or null when it does not. */
    fun match(query: SearchQuery, key: SearchKey): Match? {
        val q = query.compact
        val text = key.compact
        if (q.isEmpty() || text.isEmpty()) return null
        if (text == q) return Match(MatchKind.Exact, 0)
        if (text.startsWith(q)) return Match(MatchKind.Prefix, text.length - q.length)
        if (q.length >= 2 && key.initials.startsWith(q)) {
            return Match(MatchKind.WordInitials, key.initials.length - q.length)
        }
        val wordIndex = wordPrefixIndex(query, key)
        if (wordIndex != null) return Match(MatchKind.WordPrefix, wordIndex)
        val at = text.indexOf(q)
        if (at >= 0) return Match(MatchKind.Substring, at)
        if (q.length >= 2) {
            val gaps = subsequencePenalty(q, text)
            if (gaps != null) return Match(MatchKind.Subsequence, gaps)
        }
        return null
    }

    /** The better of the matches of [query] against any of [keys] (e.g. custom and system label). */
    fun bestMatch(query: SearchQuery, keys: List<SearchKey>): Match? {
        var best: Match? = null
        for (key in keys) {
            val candidate = match(query, key) ?: continue
            if (best == null || isBetter(candidate, best)) best = candidate
        }
        return best
    }

    /**
     * The items matching [query], best first. Ties keep the order of [items] (the drawer passes
     * them alphabetically), after preferring shorter labels. A blank query returns [items] as is.
     */
    fun <T> rank(query: String, items: List<T>, keysOf: (T) -> List<SearchKey>): List<T> {
        val prepared = SearchQuery(query)
        if (prepared.isEmpty) return items
        val scored = ArrayList<Scored>()
        items.forEachIndexed { index, item ->
            val keys = keysOf(item)
            val match = bestMatch(prepared, keys)
            if (match != null) {
                val length = keys.minOfOrNull { it.compact.length } ?: 0
                scored.add(Scored(index, match, length))
            }
        }
        scored.sort()
        return scored.map { items[it.index] }
    }

    private fun isBetter(candidate: Match, current: Match): Boolean =
        candidate.kind < current.kind || (candidate.kind == current.kind && candidate.penalty < current.penalty)

    /**
     * Index of the first word matched by prefix: either the query (spaces ignored) starts at a
     * later word ("maps", "tube mu"), or each query token starts a word, in order ("goo ma").
     */
    private fun wordPrefixIndex(query: SearchQuery, key: SearchKey): Int? {
        for (w in 1 until key.wordStarts.size) {
            if (key.compact.startsWith(query.compact, key.wordStarts[w])) return w
        }
        if (query.tokens.size < 2) return null
        var nextWord = 0
        var firstWord = -1
        for (token in query.tokens) {
            var found = -1
            var w = nextWord
            while (w < key.words.size) {
                if (key.words[w].startsWith(token)) {
                    found = w
                    break
                }
                w++
            }
            if (found < 0) return null
            if (firstWord < 0) firstWord = found
            nextWord = found + 1
        }
        return firstWord
    }

    /** Gaps inside the earliest in-order occurrence of [query]'s letters in [text] (then its start). */
    private fun subsequencePenalty(query: String, text: String): Int? {
        var matched = 0
        var start = -1
        var end = -1
        for (i in text.indices) {
            if (text[i] == query[matched]) {
                if (start < 0) start = i
                end = i
                matched++
                if (matched == query.length) break
            }
        }
        if (matched < query.length) return null
        val gaps = end - start + 1 - query.length
        return gaps * 100 + start.coerceAtMost(99)
    }

    /** Sort order: kind, penalty, shorter label, then the caller's order. */
    private class Scored(val index: Int, val match: Match, val length: Int) : Comparable<Scored> {
        override fun compareTo(other: Scored): Int = when {
            match.kind != other.match.kind -> match.kind.compareTo(other.match.kind)
            match.penalty != other.match.penalty -> match.penalty.compareTo(other.match.penalty)
            length != other.length -> length.compareTo(other.length)
            else -> index.compareTo(other.index)
        }
    }
}
