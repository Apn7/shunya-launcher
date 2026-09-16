package dev.apn7.shunya.core.designsystem.theme

import androidx.compose.ui.unit.dp

/** Spacing tokens (4 dp grid). Generous whitespace is part of the look: prefer the larger step. */
object Spacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 16.dp
    val l = 24.dp
    val xl = 32.dp
    val xxl = 48.dp

    /** Horizontal padding of every screen. */
    val screenHorizontal = 24.dp

    /** Minimum height of anything tappable (accessibility). */
    val minTouchTarget = 48.dp
}

/** Motion tokens: short fades and slides, no springs (PRD section 2). */
object Motion {
    const val SHORT_MILLIS = 150
    const val MEDIUM_MILLIS = 220
}
