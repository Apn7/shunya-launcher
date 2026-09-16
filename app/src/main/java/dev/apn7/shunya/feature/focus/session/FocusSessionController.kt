package dev.apn7.shunya.feature.focus.session

import dev.apn7.shunya.core.contract.FocusController
import dev.apn7.shunya.core.model.FocusStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** [FocusController] over `FocusConfigRepository`. Stub behaviour: focus is never active. */
class FocusSessionController : FocusController {

    override val status: StateFlow<FocusStatus> = MutableStateFlow(FocusStatus.Inactive)

    override fun startSession(minutes: Int?) = Unit

    override fun endSession() = Unit

    override fun toggleSession() = Unit

    override fun isBlockedNow(packageName: String): Boolean = false
}
