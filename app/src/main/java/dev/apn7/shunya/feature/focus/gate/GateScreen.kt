package dev.apn7.shunya.feature.focus.gate

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import dev.apn7.shunya.LocalAppContainer
import dev.apn7.shunya.R
import dev.apn7.shunya.core.data.FocusConfigRepository
import dev.apn7.shunya.core.designsystem.component.ShunyaButtonStyle
import dev.apn7.shunya.core.designsystem.component.ShunyaTextButton
import dev.apn7.shunya.core.designsystem.durationText
import dev.apn7.shunya.core.designsystem.rememberHaptics
import dev.apn7.shunya.core.designsystem.theme.ShunyaTheme
import dev.apn7.shunya.core.designsystem.theme.Spacing
import dev.apn7.shunya.core.model.LaunchDecision
import dev.apn7.shunya.core.model.ProductLimits
import dev.apn7.shunya.feature.focus.ui.clockTime
import dev.apn7.shunya.feature.focus.ui.rememberNow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** What the gate's buttons do; implemented by [GateActivity]. */
internal interface GateActions {
    /** Not now / Close / Back: leave the app closed. */
    fun leave()

    /** Open the app (after the pause, or "open once" during a schedule). */
    fun open()

    /** Take today's one extension, then open. */
    fun extendAndOpen()
}

/**
 * The gate for one request: asks the launch policy (the single source of truth) and shows the
 * matching mode. After "End focus" the policy is asked again, since a schedule may still apply.
 */
@Composable
internal fun GateRoute(request: GateRequest, actions: GateActions) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    var decisionVersion: Int by remember { mutableIntStateOf(0) }
    val decision: LaunchDecision? by produceState<LaunchDecision?>(initialValue = null, request, decisionVersion) {
        value = container.launchPolicy.decide(request.app)
    }
    val hasUsage: Boolean = remember { container.usageRepository.hasAccess() }
    val fallbackLabel = stringResource(R.string.focus_gate_app_fallback)
    val label = request.label.ifBlank { container.appsRepository.find(request.app)?.displayLabel.orEmpty() }.ifBlank { fallbackLabel }

    BackHandler { actions.leave() }

    when (val current = decision) {
        null -> GateLayout(visual = {}, title = "", lines = {}, buttons = {})
        LaunchDecision.Allow -> LaunchedEffect(Unit) { actions.open() }
        is LaunchDecision.Pause -> PauseGate(label, current, hasUsage, actions)
        is LaunchDecision.LimitReached -> LimitGate(label, current, actions)
        is LaunchDecision.Blocked -> BlockedGate(
            label = label,
            blocked = current,
            actions = actions,
            onEndFocus = {
                scope.launch {
                    endSession(container.focusConfigRepository)
                    decisionVersion += 1
                }
            },
            onBlockOver = { decisionVersion += 1 },
        )
    }
}

/** Ends the manual session and waits (briefly) until the stored config reflects it. */
private suspend fun endSession(repository: FocusConfigRepository) {
    repository.update { it.copy(session = null) }
    withTimeoutOrNull(1_000L) { repository.config.first { it.session == null } }
}

@Composable
private fun PauseGate(label: String, pause: LaunchDecision.Pause, hasUsage: Boolean, actions: GateActions) {
    val haptics = rememberHaptics()
    var secondsLeft: Int by remember { mutableIntStateOf(pause.pauseSeconds) }
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1_000L)
            secondsLeft -= 1
        }
        haptics.confirm()
    }
    val usageLine = when {
        !hasUsage -> null
        pause.usedTodayMillis <= 0L && pause.opensToday == 0 -> stringResource(R.string.focus_gate_not_opened_today)
        else -> stringResource(R.string.focus_gate_usage_line, durationText(pause.usedTodayMillis), pause.opensToday)
    }
    GateLayout(
        visual = {
            GateRing(breathing = true) {
                if (secondsLeft > 0) {
                    Text(
                        text = stringResource(R.string.focus_gate_countdown, secondsLeft),
                        style = ShunyaTheme.typography.title,
                        color = ShunyaTheme.colors.secondary,
                    )
                }
            }
        },
        title = stringResource(R.string.focus_gate_pause_title, label),
        lines = {
            if (usageLine != null) GateLine(usageLine)
            GateLine(stringResource(R.string.focus_gate_breathe), faint = true)
        },
        buttons = {
            ShunyaTextButton(text = stringResource(R.string.common_not_now), onClick = { actions.leave() }, style = ShunyaButtonStyle.Secondary)
            ShunyaTextButton(
                text = stringResource(R.string.common_open),
                onClick = { actions.open() },
                enabled = secondsLeft == 0,
                outlined = true,
            )
        },
    )
}

@Composable
private fun LimitGate(label: String, limit: LaunchDecision.LimitReached, actions: GateActions) {
    GateLayout(
        visual = { GateRing(breathing = false) },
        title = stringResource(R.string.focus_gate_limit_title, durationText(limit.limitMinutes * 60_000L), label),
        lines = {
            GateLine(stringResource(R.string.focus_gate_limit_used, durationText(limit.usedTodayMillis)))
            if (!limit.canExtend) GateLine(stringResource(R.string.focus_gate_limit_extended), faint = true)
            GateLine(stringResource(R.string.focus_gate_limit_tomorrow), faint = true)
        },
        buttons = {
            if (limit.canExtend) {
                ShunyaTextButton(
                    text = stringResource(R.string.focus_gate_limit_extend, ProductLimits.LIMIT_EXTENSION_MINUTES),
                    onClick = { actions.extendAndOpen() },
                    style = ShunyaButtonStyle.Secondary,
                )
            } else {
                Spacer(Modifier)
            }
            ShunyaTextButton(text = stringResource(R.string.common_close), onClick = { actions.leave() }, outlined = true)
        },
    )
}

@Composable
private fun BlockedGate(
    label: String,
    blocked: LaunchDecision.Blocked,
    actions: GateActions,
    onEndFocus: () -> Unit,
    onBlockOver: () -> Unit,
) {
    val context = LocalContext.current
    val now = rememberNow()
    val endsAt = blocked.endsAt
    if (endsAt != null) {
        // When the block runs out while the gate is open, ask the policy again.
        LaunchedEffect(endsAt) {
            delay((endsAt - System.currentTimeMillis()).coerceAtLeast(0L) + 500L)
            onBlockOver()
        }
    }
    val scheduleName = blocked.scheduleName
    val title = when {
        scheduleName.isNullOrBlank() -> stringResource(R.string.focus_gate_blocked_title, label)
        else -> stringResource(R.string.focus_gate_blocked_title_schedule, label, scheduleName.orEmpty())
    }
    GateLayout(
        visual = { GateRing(breathing = false) },
        title = title,
        lines = {
            if (endsAt != null) {
                GateLine(stringResource(R.string.focus_gate_blocked_until, clockTime(context, endsAt), durationText(endsAt - now)))
            } else {
                GateLine(stringResource(R.string.focus_gate_blocked_open_ended))
            }
            val holdText = if (scheduleName == null) {
                stringResource(R.string.focus_gate_hold_end_focus)
            } else {
                stringResource(R.string.focus_gate_hold_open_once)
            }
            HoldToConfirm(
                text = holdText,
                holdMillis = ProductLimits.END_FOCUS_HOLD_SECONDS * 1000,
                onConfirmed = { if (scheduleName == null) onEndFocus() else actions.open() },
                modifier = Modifier.padding(top = Spacing.xl),
            )
        },
        buttons = {
            Spacer(Modifier)
            ShunyaTextButton(text = stringResource(R.string.common_close), onClick = { actions.leave() }, outlined = true)
        },
    )
}

/** Shared frame of every mode: ring, title and lines centred, buttons at the bottom. */
@Composable
private fun GateLayout(
    visual: @Composable () -> Unit,
    title: String,
    lines: @Composable () -> Unit,
    buttons: @Composable () -> Unit,
) {
    val colors = ShunyaTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding()
            .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        visual()
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = ShunyaTheme.typography.title,
                color = colors.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = Spacing.xl, bottom = Spacing.s)
                    .semantics { heading() },
            )
        }
        lines()
        Spacer(Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            buttons()
        }
    }
}

@Composable
private fun GateLine(text: String, faint: Boolean = false) {
    Text(
        text = text,
        style = ShunyaTheme.typography.bodySmall,
        color = if (faint) ShunyaTheme.colors.tertiary else ShunyaTheme.colors.secondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = Spacing.s),
    )
}
