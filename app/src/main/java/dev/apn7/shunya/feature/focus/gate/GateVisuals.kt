package dev.apn7.shunya.feature.focus.gate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.apn7.shunya.R
import dev.apn7.shunya.core.designsystem.component.ProportionBar
import dev.apn7.shunya.core.designsystem.rememberHaptics
import dev.apn7.shunya.core.designsystem.theme.Motion
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing

/** One slow breath: this long in, the same out. */
private const val BREATH_MILLIS = 4_000

/** Size of the ring when it does not breathe. */
private const val STILL_SCALE = 0.8f

/**
 * The gate's "zero" ring. [breathing] = true grows and shrinks it slowly (a calm breathing pace);
 * otherwise it stays still and no animation runs. [content] (e.g. the countdown) sits in the middle.
 * The animated value is read only while drawing, so breathing never recomposes the screen.
 */
@Composable
internal fun GateRing(
    breathing: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    val colors = ShunyaTheme.colors
    val breath: State<Float>? = if (breathing) rememberBreath() else null
    Box(modifier = modifier.size(200.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scale = breath?.value ?: STILL_SCALE
            val radius = size.minDimension / 2f * scale
            drawCircle(color = colors.divider, radius = radius)
            drawCircle(color = colors.tertiary, radius = radius, style = Stroke(width = 1.dp.toPx()))
        }
        content()
    }
}

/** Scale of the ring over one slow breath in and out, repeating. */
@Composable
private fun rememberBreath(): State<Float> {
    val transition = rememberInfiniteTransition(label = "breathing")
    return transition.animateFloat(
        initialValue = 0.62f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = BREATH_MILLIS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath",
    )
}

/**
 * A deliberate control: the action runs only after [holdMillis] of continuous pressing, with a bar
 * that fills while holding and falls back on release. Used for "End focus" (honest friction).
 */
@Composable
internal fun HoldToConfirm(
    text: String,
    holdMillis: Int,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ShunyaTheme.colors
    val haptics = rememberHaptics()
    val progress: Animatable<Float, AnimationVector1D> = remember { Animatable(0f) }
    var holding: Boolean by remember { mutableStateOf(false) }
    val latestOnConfirmed by rememberUpdatedState(onConfirmed)
    LaunchedEffect(holding) {
        if (holding) {
            haptics.longPress()
            val remaining = ((1f - progress.value) * holdMillis).toInt().coerceAtLeast(1)
            progress.animateTo(1f, animationSpec = tween(durationMillis = remaining, easing = LinearEasing))
            haptics.confirm()
            latestOnConfirmed()
        } else {
            progress.animateTo(0f, animationSpec = tween(durationMillis = Motion.MEDIUM_MILLIS))
        }
    }
    val seconds = holdMillis / 1000
    val description = stringResource(R.string.focus_gate_hold_description, text, seconds)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.minTouchTarget)
            .semantics { contentDescription = description }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        holding = true
                        tryAwaitRelease()
                        holding = false
                    },
                )
            }
            .padding(vertical = Spacing.s),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = text, style = ShunyaTheme.typography.label, color = colors.ink, textAlign = TextAlign.Center)
        Text(
            text = stringResource(R.string.focus_gate_hold_hint, seconds),
            style = ShunyaTheme.typography.caption,
            color = colors.tertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.xs),
        )
        ProportionBar(fraction = progress.value, modifier = Modifier.padding(top = Spacing.s))
    }
}
