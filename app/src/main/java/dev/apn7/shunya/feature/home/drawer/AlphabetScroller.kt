package dev.apn7.shunya.feature.home.drawer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.rememberHaptics
import dev.apn7.shunya.core.designsystem.theme.Motion
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.feature.home.drawer.logic.Section
import dev.apn7.shunya.feature.home.drawer.logic.SectionIndex
import dev.apn7.shunya.feature.home.drawer.logic.WaveMath

private val StripWidth = 32.dp
private val MaxLetterHeight = 22.dp

/** How far the wave reaches from the finger, and how far the letter under it moves left. */
private val WaveRadius = 72.dp
private val WaveShift = 44.dp
private const val WAVE_MAX_SCALE = 2.4f

/**
 * Niagara-style fast scroller on the right edge (PRD 3.2): only the letters that exist. While a
 * finger is on it, letters near the finger grow and slide left like a wave, the list jumps to
 * the letter under the finger and each letter change ticks. The strip is excluded from the
 * system back gesture so dragging along the edge scrolls instead of going back.
 */
@Composable
internal fun AlphabetScroller(
    sections: List<Section>,
    onSelect: (Section) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (sections.isEmpty()) return
    val haptics = rememberHaptics()
    val density = LocalDensity.current
    val latestSections by rememberUpdatedState(sections)
    val latestOnSelect by rememberUpdatedState(onSelect)
    var touchY by remember { mutableStateOf<Float?>(null) }
    var selected by remember { mutableIntStateOf(-1) }
    val indexLabel = stringResource(R.string.drawer_alphabet_index)

    BoxWithConstraints(modifier = modifier) {
        val letterHeight = minOf(MaxLetterHeight, maxHeight / sections.size)
        val letterHeightPx = with(density) { letterHeight.toPx() }
        val stripHeightPx = letterHeightPx * sections.size
        val radiusPx = with(density) { WaveRadius.toPx() }
        val shiftPx = with(density) { WaveShift.toPx() }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(StripWidth)
                .height(letterHeight * sections.size)
                .systemGestureExclusion()
                .semantics { contentDescription = indexLabel }
                .pointerInput(sections.size, stripHeightPx) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        fun select(y: Float) {
                            touchY = y
                            val index = SectionIndex.letterIndexAt(y, stripHeightPx, latestSections.size)
                            if (index >= 0 && index != selected) {
                                selected = index
                                haptics.tick()
                                latestOnSelect(latestSections[index])
                            }
                        }
                        select(down.position.y)
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id }
                            if (change == null || !change.pressed) break
                            change.consume()
                            select(change.position.y)
                        }
                        touchY = null
                        selected = -1
                    }
                },
        ) {
            sections.forEachIndexed { index, section ->
                key(section.letter) {
                    val center = (index + 0.5f) * letterHeightPx
                    val finger = touchY
                    val target = if (finger == null) 0f else WaveMath.influence(finger - center, radiusPx)
                    val influence by animateFloatAsState(
                        targetValue = target,
                        animationSpec = if (finger == null) tween(Motion.SHORT_MILLIS) else snap(),
                        label = "wave",
                    )
                    ScrollerLetter(
                        letter = section.letter,
                        influence = influence,
                        highlighted = index == selected,
                        shiftPx = shiftPx,
                        jumpLabel = stringResource(R.string.drawer_jump_to, section.letter),
                        onJump = { onSelect(section) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(letterHeight),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrollerLetter(
    letter: String,
    influence: Float,
    highlighted: Boolean,
    shiftPx: Float,
    jumpLabel: String,
    onJump: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.semantics {
            onClick(label = jumpLabel) {
                onJump()
                true
            }
        },
    ) {
        Text(
            text = letter,
            style = ShunyaTheme.typography.caption,
            color = if (highlighted) ShunyaTheme.colors.ink else ShunyaTheme.colors.secondary,
            modifier = Modifier.graphicsLayer {
                val scale = WaveMath.scale(influence, WAVE_MAX_SCALE)
                scaleX = scale
                scaleY = scale
                translationX = -WaveMath.shift(influence, shiftPx)
            },
        )
    }
}
