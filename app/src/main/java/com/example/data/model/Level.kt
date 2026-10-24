package com.example.data.model

enum class LevelStage(val label: String, val description: String) {
    IDENTIFICATION("Stage 1: Identification", "Find and mark the true institutional Order Block (OB)"),
    BREAK_AND_RETEST("Stage 2: Break & Retest", "Avoid FOMO breakout traps and wait for the retest bounce"),
    FULL_TRADE("Stage 3: Full Trade Setup", "Place Entry, protected SL below OB, and 1:2+ TP"),
    ADVANCED_SMC("Stage 4: Institutional Combine", "Fair Value Gaps, Liquidity Sweeps, and CHOCH setups")
}

data class LevelSolution(
    val obLow: Double,
    val obHigh: Double,
    val obCandleStartIndex: Int,
    val obCandleEndIndex: Int,
    val breakPoint: Double,
    val retestZoneLow: Double,
    val retestZoneHigh: Double,
    val recommendedEntry: Double,
    val recommendedSl: Double,
    val recommendedTp: Double,
    val trapCandleIndex: Int = -1,
    val retestCandleIndex: Int = -1,
    val explanation: String
)

data class Level(
    val id: Int,
    val title: String,
    val concept: String,
    val stage: LevelStage,
    val symbol: String,
    val timeframe: String,
    val description: String,
    val winCondition: String,
    val hint: String,
    val candles: List<Candle>,
    val solution: LevelSolution,
    val isBullishSetup: Boolean = true
)

data class UserBox(
    val fromCandleIndex: Int,
    val toCandleIndex: Int,
    val priceHigh: Double,
    val priceLow: Double
) {
    val high: Double get() = maxOf(priceHigh, priceLow)
    val low: Double get() = minOf(priceHigh, priceLow)
    val startIndex: Int get() = minOf(fromCandleIndex, toCandleIndex)
    val endIndex: Int get() = maxOf(fromCandleIndex, toCandleIndex)
}

data class TradeSetup(
    val entryPrice: Double? = null,
    val stopLossPrice: Double? = null,
    val takeProfitPrice: Double? = null
) {
    val isValid: Boolean get() = entryPrice != null && stopLossPrice != null && takeProfitPrice != null

    val riskRewardRatio: Double?
        get() {
            val entry = entryPrice ?: return null
            val sl = stopLossPrice ?: return null
            val tp = takeProfitPrice ?: return null
            val risk = kotlin.math.abs(entry - sl)
            if (risk <= 0.00001) return null
            val reward = kotlin.math.abs(tp - entry)
            return reward / risk
        }
}

data class ValidationResult(
    val isCorrect: Boolean,
    val score: Int,
    val feedback: String,
    val detailedReason: String,
    val xpEarned: Int,
    val stars: Int,
    val isTrapTriggered: Boolean = false,
    val breakRetestSuccess: Boolean = false
)
