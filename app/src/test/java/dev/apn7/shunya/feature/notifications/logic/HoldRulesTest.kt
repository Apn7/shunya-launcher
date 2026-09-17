package dev.apn7.shunya.feature.notifications.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class HoldRulesTest {

    private val own = "dev.apn7.shunya"
    private val on = HoldSettings(holdEnabled = true, allowedPackages = setOf("com.android.dialer"), ownPackage = own)

    private fun facts(pkg: String = "com.instagram.android") = NotificationFacts(packageName = pkg)

    @Test
    fun holdsOrdinaryNotificationFromNonAllowedApp() {
        assertEquals(HoldDecision.Hold, HoldRules.decide(facts(), on))
    }

    @Test
    fun nothingIsHeldWhenFilterIsOff() {
        assertEquals(HoldDecision.FilterOff, HoldRules.decide(facts(), on.copy(holdEnabled = false)))
    }

    @Test
    fun allowedAppsStayInTheShade() {
        assertEquals(HoldDecision.AllowedApp, HoldRules.decide(facts("com.android.dialer"), on))
    }

    @Test
    fun ownAndSystemPackagesAreNeverHeld() {
        assertEquals(HoldDecision.OwnOrSystem, HoldRules.decide(facts(own), on))
        assertEquals(HoldDecision.OwnOrSystem, HoldRules.decide(facts("com.android.systemui"), on))
        assertEquals(HoldDecision.OwnOrSystem, HoldRules.decide(facts("android"), on))
    }

    @Test
    fun protectedKindsAreNeverHeld() {
        val base = facts()
        val kinds = listOf(
            base.copy(isOngoing = true),
            base.copy(isClearable = false),
            base.copy(isForegroundService = true),
            base.copy(isGroupSummary = true),
            base.copy(isTimeCritical = true),
            base.copy(isMedia = true),
        )
        kinds.forEach { assertEquals(it.toString(), HoldDecision.ProtectedKind, HoldRules.decide(it, on)) }
    }

    @Test
    fun protectedKindWinsOverAllowedButFilterOffWinsOverAll() {
        val ongoingAllowed = facts("com.android.dialer").copy(isOngoing = true)
        assertEquals(HoldDecision.ProtectedKind, HoldRules.decide(ongoingAllowed, on))
        assertEquals(HoldDecision.FilterOff, HoldRules.decide(ongoingAllowed, on.copy(holdEnabled = false)))
    }

    @Test
    fun nullCustomListMeansDefaults() {
        val defaults = setOf("a", "b")
        assertEquals(defaults, HoldRules.effectiveAllowed(null, defaults))
        assertEquals(setOf("c"), HoldRules.effectiveAllowed(setOf("c"), defaults))
        assertEquals(emptySet<String>(), HoldRules.effectiveAllowed(emptySet(), defaults))
    }

    @Test
    fun firstToggleMaterialisesTheDefaults() {
        val defaults = setOf("a", "b")
        assertEquals(setOf("a", "b", "c"), HoldRules.toggled(null, defaults, "c", allowed = true))
        assertEquals(setOf("b"), HoldRules.toggled(null, defaults, "a", allowed = false))
        assertEquals(setOf("x"), HoldRules.toggled(setOf("x", "y"), defaults, "y", allowed = false))
    }
}
