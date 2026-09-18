package dev.apn7.shunya.feature.home.drawer.logic

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

/**
 * The fast scroller's "wave" (Niagara style): letters near the finger grow and move left.
 * [influence] is 1 under the finger and eases smoothly to 0 at [radius], so neighbours follow
 * the finger like a wave instead of jumping.
 */
object WaveMath {

    /** Smooth bump: 1 at distance 0, 0 at [radius] and beyond (cosine squared, no corners). */
    fun influence(distance: Float, radius: Float): Float {
        if (radius <= 0f) return 0f
        val x = abs(distance) / radius
        if (x >= 1f) return 0f
        val c = cos(x * PI / 2.0).toFloat()
        return c * c
    }

    /** Scale of a letter with [influence]: 1 at rest, [maxScale] under the finger. */
    fun scale(influence: Float, maxScale: Float): Float = 1f + (maxScale - 1f) * influence.coerceIn(0f, 1f)

    /** Leftward shift of a letter with [influence], up to [maxShift] under the finger. */
    fun shift(influence: Float, maxShift: Float): Float = maxShift * influence.coerceIn(0f, 1f)
}
