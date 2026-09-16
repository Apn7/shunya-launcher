package dev.apn7.shunya.feature.focus.policy

import dev.apn7.shunya.core.contract.LaunchPolicy
import dev.apn7.shunya.core.model.AppKey
import dev.apn7.shunya.core.model.LaunchDecision

/** [LaunchPolicy] for focus rules. Stub behaviour: everything is allowed. */
class FocusLaunchPolicy : LaunchPolicy {

    override suspend fun decide(app: AppKey): LaunchDecision = LaunchDecision.Allow
}
