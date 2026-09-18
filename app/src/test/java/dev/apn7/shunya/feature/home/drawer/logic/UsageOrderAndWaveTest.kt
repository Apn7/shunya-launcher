package dev.apn7.shunya.feature.home.drawer.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UsageOrderAndWaveTest {

    @Test
    fun rankedPackagesComeFirstThenTheRestAlphabetically() {
        val apps = listOf("a.alarm", "b.browser", "c.chat", "d.docs")
        val sorted = UsageOrder.sort(apps, ranking = listOf("c.chat", "a.alarm", "x.uninstalled")) { it }
        assertEquals(listOf("c.chat", "a.alarm", "b.browser", "d.docs"), sorted)
    }

    @Test
    fun emptyRankingKeepsTheAlphabeticalOrder() {
        val apps = listOf("b", "a")
        assertEquals(apps, UsageOrder.sort(apps, ranking = emptyList()) { it })
    }

    @Test
    fun activitiesOfOnePackageShareItsRank() {
        val apps = listOf("mail/Inbox", "maps/Main", "mail/Compose")
        val sorted = UsageOrder.sort(apps, ranking = listOf("mail")) { it.substringBefore('/') }
        assertEquals(listOf("mail/Inbox", "mail/Compose", "maps/Main"), sorted)
    }

    @Test
    fun waveInfluenceIsOneUnderTheFingerAndZeroAtTheRadius() {
        assertEquals(1f, WaveMath.influence(0f, 100f), 1e-4f)
        assertEquals(0f, WaveMath.influence(100f, 100f), 1e-4f)
        assertEquals(0f, WaveMath.influence(150f, 100f), 1e-4f)
        assertEquals(WaveMath.influence(30f, 100f), WaveMath.influence(-30f, 100f), 1e-6f)
        assertEquals(0f, WaveMath.influence(10f, 0f), 1e-6f)
    }

    @Test
    fun waveInfluenceFallsSmoothlyWithDistance() {
        var previous = 1f
        for (d in 1..100) {
            val value = WaveMath.influence(d.toFloat(), 100f)
            assertTrue("influence must not grow with distance", value <= previous)
            previous = value
        }
    }

    @Test
    fun waveScaleAndShiftFollowInfluence() {
        assertEquals(1f, WaveMath.scale(0f, 2.2f), 1e-6f)
        assertEquals(2.2f, WaveMath.scale(1f, 2.2f), 1e-6f)
        assertEquals(1.6f, WaveMath.scale(0.5f, 2.2f), 1e-6f)
        assertEquals(0f, WaveMath.shift(0f, 40f), 1e-6f)
        assertEquals(40f, WaveMath.shift(1f, 40f), 1e-6f)
        assertEquals(40f, WaveMath.shift(3f, 40f), 1e-6f)
    }
}
