package dev.apn7.shunya.core.contract

import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.LaunchDecision

/**
 * Decides whether an app may open right away.
 *
 * Called by `AppLauncher` for every launch from Shunya, and by the gate and the accessibility
 * service for system-wide blocking. Order of precedence: [LaunchDecision.Blocked] (focus session
 * or schedule, distracting apps only) > [LaunchDecision.LimitReached] > [LaunchDecision.Pause]
 * (distracting apps) > [LaunchDecision.Allow].
 */
interface LaunchPolicy {

    /**
     * Decision for opening [app] now. Must be quick (it runs on every tap: target < 50 ms),
     * must never throw, and must return [LaunchDecision.Allow] when usage access is missing and
     * nothing else applies.
     */
    suspend fun decide(app: AppKey): LaunchDecision
}
