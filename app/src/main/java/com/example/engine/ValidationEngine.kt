package com.example.engine

import com.example.data.model.Level
import com.example.data.model.LevelStage
import com.example.data.model.TradeSetup
import com.example.data.model.UserBox
import com.example.data.model.ValidationResult
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object ValidationEngine {

    /**
     * Stage 1: Validates if the user's drawn box overlaps the target Order Block by >= 80%
     */
    fun validateOrderBlock(userBox: UserBox?, level: Level): ValidationResult {
        if (userBox == null) {
            return ValidationResult(
                isCorrect = false,
                score = 0,
                feedback = "No box drawn!",
                detailedReason = "Drag or tap on the chart to draw a rectangle encompassing the Order Block.",
                xpEarned = 0,
                stars = 0
            )
        }

        val sol = level.solution
        val targetLow = sol.obLow
        val targetHigh = sol.obHigh
        val targetRange = targetHigh - targetLow

        if (targetRange <= 0.00001) {
            return ValidationResult(
                isCorrect = false,
                score = 0,
                feedback = "Invalid target",
                detailedReason = "Level configuration error",
                xpEarned = 0,
                stars = 0
            )
        }

        // Price overlap calculation
        val overlapLow = max(userBox.low, targetLow)
        val overlapHigh = min(userBox.high, targetHigh)
        val overlapHeight = max(0.0, overlapHigh - overlapLow)
        val priceCoverage = overlapHeight / targetRange

        // Penalty if user box is excessively gigantic (e.g., covering entire chart)
        val userHeight = max(0.00001, userBox.high - userBox.low)
        val precisionRatio = overlapHeight / userHeight // 1.0 if user box equals target

        // Candle index overlap calculation
        val targetStart = sol.obCandleStartIndex
        val targetEnd = sol.obCandleEndIndex
        val candleOverlap = max(0, min(userBox.endIndex, targetEnd) - max(userBox.startIndex, targetStart) + 1)
        val targetCandleCount = max(1, targetEnd - targetStart + 1)
        val candleCoverage = candleOverlap.toDouble() / targetCandleCount.toDouble()

        // Weighted score: 65% price height overlap, 20% precision, 15% candle index coverage
        // With a 10% generous margin for mobile touch ergonomics
        val effectivePriceCoverage = min(1.0, priceCoverage * 1.1)
        val compositeScore = ((effectivePriceCoverage * 65.0) + (precisionRatio * 20.0) + (candleCoverage * 15.0)).toInt()
            .coerceIn(0, 100)

        val isWin = compositeScore >= 75 || priceCoverage >= 0.78

        return if (isWin) {
            val stars = when {
                compositeScore >= 90 -> 3
                compositeScore >= 80 -> 2
                else -> 1
            }
            val xp = if (compositeScore >= 90) 100 else 85
            ValidationResult(
                isCorrect = true,
                score = compositeScore,
                feedback = if (compositeScore >= 90) "Perfect Institutional OB!" else "Order Block Identified!",
                detailedReason = "Accurately spotted the institutional footprint before the high-volume displacement rally. ${sol.explanation}",
                xpEarned = xp,
                stars = stars
            )
        } else {
            val reason = when {
                userBox.high < targetLow -> "Your box is too low. Look for the last down candle right before the aggressive green expansion."
                userBox.low > targetHigh -> "Your box is too high. You marked the breakout leg rather than the origin Order Block."
                priceCoverage < 0.5 -> "The box missed the core body and wicks of the mitigation zone. Overlap was only ${(priceCoverage * 100).toInt()}%."
                else -> "Close! Make sure to encompass the full candle range (open to low/high wick) of the base candle."
            }
            ValidationResult(
                isCorrect = false,
                score = compositeScore,
                feedback = "Missed the Order Block",
                detailedReason = "$reason ${sol.explanation}",
                xpEarned = 0,
                stars = 0
            )
        }
    }

    /**
     * Stage 2: Break & Retest validation
     * Player must avoid clicking during the FOMO breakout, and click when the ball lands in the retest zone
     */
    fun validateBreakAndRetest(currentCandleIndex: Int, level: Level): ValidationResult {
        val sol = level.solution
        val trapIndex = sol.trapCandleIndex
        val retestIndex = sol.retestCandleIndex

        return when {
            // Player clicked too early during breakout/trap phase!
            trapIndex != -1 && abs(currentCandleIndex - trapIndex) <= 1 -> {
                ValidationResult(
                    isCorrect = false,
                    score = 25,
                    feedback = "FOMO TRAP TRIGGERED!",
                    detailedReason = "You bought at the peak of the breakout candle! Smart money used your buy orders as exit liquidity before pulling back. Always wait for the retest!",
                    xpEarned = 0,
                    stars = 0,
                    isTrapTriggered = true
                )
            }
            // Player clicked accurately in the retest zone!
            retestIndex != -1 && abs(currentCandleIndex - retestIndex) <= 2 -> {
                ValidationResult(
                    isCorrect = true,
                    score = 100,
                    feedback = "SNIPER RETEST ENTRY!",
                    detailedReason = "Patience paid off! You caught the price bounce right off the mitigated Order Block before the next impulse leg.",
                    xpEarned = 100,
                    stars = 3,
                    breakRetestSuccess = true
                )
            }
            // Clicked in no-man's land
            currentCandleIndex < retestIndex -> {
                ValidationResult(
                    isCorrect = false,
                    score = 40,
                    feedback = "Entered Too Early",
                    detailedReason = "Price hasn't completed its pullback to the Order Block ceiling yet. Wait for the retest touch!",
                    xpEarned = 0,
                    stars = 0
                )
            }
            else -> {
                ValidationResult(
                    isCorrect = false,
                    score = 30,
                    feedback = "Entered Too Late",
                    detailedReason = "The bounce already left the station! Chasing green candles results in poor risk/reward.",
                    xpEarned = 0,
                    stars = 0
                )
            }
        }
    }

    /**
     * Stage 3: Full Trade Setup validation
     * Entry in retest zone = 40 points
     * SL below OB with safety buffer = 30 points
     * TP realistic (>= 1:2 RR) = 30 points
     */
    fun validateFullTrade(setup: TradeSetup, level: Level): ValidationResult {
        val entry = setup.entryPrice
        val sl = setup.stopLossPrice
        val tp = setup.takeProfitPrice

        if (entry == null || sl == null || tp == null) {
            return ValidationResult(
                isCorrect = false,
                score = 0,
                feedback = "Incomplete Trade Setup",
                detailedReason = "Place all 3 levels: Entry, Stop Loss, and Take Profit.",
                xpEarned = 0,
                stars = 0
            )
        }

        val sol = level.solution
        var entryPoints = 0
        var slPoints = 0
        var tpPoints = 0
        val notes = mutableListOf<String>()

        // 1. Entry validation (Retest zone)
        val isBullish = level.isBullishSetup
        val retestLow = min(sol.retestZoneLow, sol.retestZoneHigh)
        val retestHigh = max(sol.retestZoneLow, sol.retestZoneHigh)
        val retestTolerance = (retestHigh - retestLow) * 0.4

        if (entry in (retestLow - retestTolerance)..(retestHigh + retestTolerance)) {
            entryPoints = 40
            notes.add("Entry positioned inside the institutional retest zone (+40 pts).")
        } else {
            notes.add("Entry is outside the optimal retest zone.")
        }

        // 2. Stop Loss validation
        if (isBullish) {
            if (sl < sol.obLow) {
                // Properly below the OB
                val buffer = sol.obLow - sl
                val maxBuffer = (sol.obHigh - sol.obLow) * 1.5
                if (buffer <= maxBuffer) {
                    slPoints = 30
                    notes.add("Stop Loss safely protected below the Order Block low (+30 pts).")
                } else {
                    slPoints = 20
                    notes.add("Stop Loss is too wide, hurting your risk-to-reward ratio (+20 pts).")
                }
            } else {
                notes.add("Stop Loss is INSIDE or ABOVE the Order Block! You will get wicked out.")
            }
        } else {
            // Bearish setup: SL must be above OB High
            if (sl > sol.obHigh) {
                slPoints = 30
                notes.add("Stop Loss safely protected above the Bearish OB high (+30 pts).")
            } else {
                notes.add("Stop Loss is below the Bearish OB high! Vulnerable to sweep.")
            }
        }

        // 3. Take Profit & R:R validation
        val risk = abs(entry - sl)
        val reward = abs(tp - entry)
        val rrRatio = if (risk > 0.0001) reward / risk else 0.0

        if (rrRatio >= 1.8) {
            tpPoints = 30
            notes.add("Outstanding Take Profit! R:R is 1:${String.format("%.1f", rrRatio)} (+30 pts).")
        } else if (rrRatio >= 1.2) {
            tpPoints = 20
            notes.add("Acceptable Take Profit (1:${String.format("%.1f", rrRatio)} R:R), aim for 1:2+ (+20 pts).")
        } else {
            notes.add("Take Profit is too close to entry (1:${String.format("%.1f", rrRatio)} R:R). Poor expectancy.")
        }

        val totalScore = entryPoints + slPoints + tpPoints
        val isPassed = totalScore >= 70

        val stars = when {
            totalScore >= 90 -> 3
            totalScore >= 75 -> 2
            totalScore >= 60 -> 1
            else -> 0
        }

        return ValidationResult(
            isCorrect = isPassed,
            score = totalScore,
            feedback = if (isPassed) "Grade A Setup ($totalScore/100)" else "Setup Needs Adjustment ($totalScore/100)",
            detailedReason = notes.joinToString(" "),
            xpEarned = if (isPassed) totalScore else 0,
            stars = stars
        )
    }
}
