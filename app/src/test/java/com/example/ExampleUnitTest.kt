package com.example

import com.example.data.LevelRepository
import com.example.data.model.TradeSetup
import com.example.data.model.UserBox
import com.example.engine.ValidationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun level1_exactOrderBlockOverlap_validatesSuccessfully() {
        val level1 = LevelRepository.getLevelById(1)
        requireNotNull(level1)

        // Target: obLow: 81078, obHigh: 81130, obCandleIndex: 6
        val userBox = UserBox(
            fromCandleIndex = 6,
            toCandleIndex = 6,
            priceLow = 81078.0,
            priceHigh = 81130.0
        )

        val result = ValidationEngine.validateOrderBlock(userBox, level1)
        assertTrue("Level 1 exact box should be valid", result.isCorrect)
        assertTrue("Score should be >= 80%", result.score >= 80)
        assertEquals("Earns 100 XP", 100, result.xpEarned)
    }

    @Test
    fun level1_inaccurateBox_failsValidation() {
        val level1 = LevelRepository.getLevelById(1)
        requireNotNull(level1)

        val badBox = UserBox(
            fromCandleIndex = 1,
            toCandleIndex = 2,
            priceLow = 81500.0,
            priceHigh = 81600.0
        )

        val result = ValidationEngine.validateOrderBlock(badBox, level1)
        assertFalse("Box outside target must fail", result.isCorrect)
    }

    @Test
    fun stage2_fomoPeak_triggersTrap() {
        val level6 = LevelRepository.getLevelById(6)
        requireNotNull(level6)

        // Candle 8 is the trap peak candle
        val trapResult = ValidationEngine.validateBreakAndRetest(8, level6)
        assertTrue("Should detect FOMO trap", trapResult.isTrapTriggered)
        assertFalse(trapResult.isCorrect)

        // Candle 12 is the retest candle
        val retestResult = ValidationEngine.validateBreakAndRetest(12, level6)
        assertTrue("Should succeed on retest touch", retestResult.isCorrect)
    }

    @Test
    fun stage3_fullTrade_calculatesRiskRewardProperly() {
        val trade = TradeSetup(
            entryPrice = 82500.0,
            stopLossPrice = 82250.0, // 250 risk
            takeProfitPrice = 83500.0 // 1000 reward -> 1:4.0 R:R
        )

        assertEquals(4.0, trade.riskRewardRatio ?: 0.0, 0.01)
    }
}
