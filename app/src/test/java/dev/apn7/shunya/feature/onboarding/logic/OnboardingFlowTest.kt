package dev.apn7.shunya.feature.onboarding.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingFlowTest {

    @Test
    fun stepsRunInPrdOrder() {
        var step: OnboardingStep? = OnboardingStep.Welcome
        val seen = mutableListOf<OnboardingStep>()
        while (step != null) {
            seen += step
            step = OnboardingFlow.next(step)
        }
        assertEquals(
            listOf(
                OnboardingStep.Welcome,
                OnboardingStep.Look,
                OnboardingStep.Favorites,
                OnboardingStep.DefaultLauncher,
                OnboardingStep.Permissions,
                OnboardingStep.Done,
            ),
            seen,
        )
    }

    @Test
    fun previousStopsAtWelcome() {
        assertNull(OnboardingFlow.previous(OnboardingStep.Welcome))
        assertEquals(OnboardingStep.Welcome, OnboardingFlow.previous(OnboardingStep.Look))
        assertNull(OnboardingFlow.next(OnboardingStep.Done))
    }

    @Test
    fun positionsAreOneBased() {
        assertEquals(1, OnboardingFlow.position(OnboardingStep.Welcome))
        assertEquals(6, OnboardingFlow.position(OnboardingStep.Done))
        assertEquals(6, OnboardingFlow.total)
    }

    @Test
    fun favoritesToggleInPickOrder() {
        val picked = FavoriteSelection.toggled(FavoriteSelection.toggled(emptyList(), "b", 6), "a", 6)
        assertEquals(listOf("b", "a"), picked)
        assertEquals(listOf("a"), FavoriteSelection.toggled(picked, "b", 6))
    }

    @Test
    fun favoritesStopAtMaxButCanStillBeRemoved() {
        val full = listOf("a", "b")
        assertTrue(FavoriteSelection.isFull(full, 2))
        assertEquals(full, FavoriteSelection.toggled(full, "c", 2))
        assertEquals(listOf("b"), FavoriteSelection.toggled(full, "a", 2))
        assertFalse(FavoriteSelection.isFull(listOf("b"), 2))
    }
}
