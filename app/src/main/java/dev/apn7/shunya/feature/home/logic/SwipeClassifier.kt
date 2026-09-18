package dev.apn7.shunya.feature.home.logic

import kotlin.math.abs
import kotlin.math.max

/** Direction of a home swipe. Screen coordinates: y grows downwards. */
enum class SwipeDirection { Up, Down, Left, Right }

/** Turns a drag into a swipe direction for the home gestures. */
object SwipeClassifier {

    /** The dominant axis must be this much longer than the other one; diagonals do nothing. */
    private const val DOMINANCE = 1.2f

    /**
     * The direction of a drag that moved ([dx], [dy]) pixels so far, or null while it is shorter
     * than [minDistance] or too diagonal to tell.
     */
    fun classify(dx: Float, dy: Float, minDistance: Float): SwipeDirection? {
        val ax = abs(dx)
        val ay = abs(dy)
        if (max(ax, ay) < minDistance) return null
        return when {
            ay >= ax * DOMINANCE -> if (dy < 0f) SwipeDirection.Up else SwipeDirection.Down
            ax >= ay * DOMINANCE -> if (dx < 0f) SwipeDirection.Left else SwipeDirection.Right
            else -> null
        }
    }
}
