package dev.apn7.shunya.core.model

import kotlinx.serialization.Serializable

/**
 * The user's per-app changes, persisted by `AppOverridesRepository`. Apps are referenced by
 * [AppKey.id]. Entries for uninstalled apps are harmless: they are ignored when lists are built.
 *
 * Change it only through the `with…` functions, e.g.
 * `overridesRepository.edit { it.withHidden(app.key, hidden = true) }`.
 */
@Serializable
data class AppOverrides(
    /** Renamed apps: [AppKey.id] to the custom label. */
    val customLabels: Map<String, String> = emptyMap(),
    /** Apps hidden from the drawer and search. */
    val hidden: Set<String> = emptySet(),
    /** Home favorites in display order. */
    val favorites: List<String> = emptyList(),
) {
    fun labelFor(key: AppKey): String? = customLabels[key.id]

    fun isHidden(key: AppKey): Boolean = key.id in hidden

    fun isFavorite(key: AppKey): Boolean = key.id in favorites

    /** Sets or clears (null or blank [label]) the custom label of [key]. */
    fun withLabel(key: AppKey, label: String?): AppOverrides {
        val clean = label?.trim().orEmpty()
        val labels = if (clean.isEmpty()) customLabels - key.id else customLabels + (key.id to clean)
        return copy(customLabels = labels)
    }

    /** Hides or unhides [key]. Hiding also removes the app from the favorites. */
    fun withHidden(key: AppKey, hidden: Boolean): AppOverrides =
        if (hidden) {
            copy(hidden = this.hidden + key.id, favorites = favorites - key.id)
        } else {
            copy(hidden = this.hidden - key.id)
        }

    /**
     * Adds [key] to the end of the favorites or removes it. Adding is a no-op when [key] is already
     * a favorite or [max] favorites exist (check [isFull] first to tell the user).
     */
    fun withFavorite(key: AppKey, favorite: Boolean, max: Int = ProductLimits.FAVORITES_MAX): AppOverrides =
        when {
            !favorite -> copy(favorites = favorites - key.id)
            key.id in favorites || isFull(max) -> this
            else -> copy(favorites = favorites + key.id)
        }

    /** True when [max] favorites are already set. */
    fun isFull(max: Int): Boolean = favorites.size >= max

    /** Replaces the favorites with [keys] in this order (duplicates dropped, capped at [max]). */
    fun withFavorites(keys: List<AppKey>, max: Int = ProductLimits.FAVORITES_MAX): AppOverrides =
        copy(favorites = keys.map { it.id }.distinct().take(max))

    /** Moves the favorite at index [from] to index [to]; out-of-range indices are ignored. */
    fun movingFavorite(from: Int, to: Int): AppOverrides {
        if (from !in favorites.indices || to !in favorites.indices || from == to) return this
        val reordered = favorites.toMutableList()
        val moved = reordered.removeAt(from)
        reordered.add(to, moved)
        return copy(favorites = reordered)
    }
}
