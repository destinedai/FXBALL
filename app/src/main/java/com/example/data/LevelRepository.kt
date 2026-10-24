package com.example.data

import com.example.data.model.Candle
import com.example.data.model.Level
import com.example.data.model.LevelSolution
import com.example.data.model.LevelStage

object LevelRepository {

    fun getAllLevels(): List<Level> = levels

    fun getLevelById(id: Int): Level? = levels.find { it.id == id }

    fun getLevelsForStage(stage: LevelStage): List<Level> = levels.filter { it.stage == stage }

    private val levels: List<Level> by lazy {
        listOf(
            createLevel1(),
            createLevel2(),
            createLevel3(),
            createLevel4(),
            createLevel5(),
            createLevel6(),
            createLevel7(),
            createLevel8(),
            createLevel9(),
            createLevel10(),
            createLevel11(),
            createLevel12(),
            createLevel13(),
            createLevel14(),
            createLevel15(),
            createLevel16(),
            createLevel17(),
            createLevel18(),
            createLevel19(),
            createLevel20()
        )
    }

    // --- LEVEL 1: Prompt's exact BTC/USDT Setup ---
    private fun createLevel1(): Level {
        // Base around 81,000 - 81,500
        val candles = mutableListOf<Candle>()
        var t = 1723450000000L

        // Prior range consolidation
        val prePrices = listOf(
            Pair(81120.0, 81160.0), Pair(81150.0, 81090.0), Pair(81100.0, 81140.0),
            Pair(81135.0, 81085.0), Pair(81090.0, 81125.0), Pair(81120.0, 81070.0)
        )
        prePrices.forEach { (o, c) ->
            candles.add(Candle(t, o, maxOf(o, c) + 30, minOf(o, c) - 25, c, 120.0))
            t += 3600000
        }

        // CANDLE INDEX 6: The Bullish Order Block (last red candle before massive impulse!)
        // Matches exact solution { obLow: 81078, obHigh: 81130 }
        val obIndex = 6
        candles.add(
            Candle(
                time = t,
                open = 81130.0,
                high = 81135.0,
                low = 81078.0,
                close = 81085.0,
                volume = 380.0
            )
        )
        t += 3600000

        // Explosive Bullish Impulse Candles (Displacement!)
        val postImpulses = listOf(
            Triple(81090.0, 81220.0, 81240.0), // Big green
            Triple(81215.0, 81340.0, 81380.0), // Huge displacement
            Triple(81335.0, 81450.0, 81490.0), // Continuing rally
            Triple(81440.0, 81390.0, 81460.0), // Minor pause
            Triple(81385.0, 81280.0, 81410.0), // Pullback start
            Triple(81270.0, 81140.0, 81290.0), // Approaching OB
            Triple(81145.0, 81110.0, 81160.0), // Retest touch in OB zone!
            Triple(81115.0, 81250.0, 81280.0), // Rejection bounce!
            Triple(81245.0, 81420.0, 81450.0), // Second leg up
            Triple(81410.0, 81600.0, 81650.0)  // Target reached
        )
        postImpulses.forEach { (o, c, h) ->
            val l = minOf(o, c) - 18.0
            candles.add(Candle(t, o, h, l, c, 260.0))
            t += 3600000
        }

        return Level(
            id = 1,
            title = "Find the Order Block",
            concept = "OB Identification",
            stage = LevelStage.IDENTIFICATION,
            symbol = "BTC/USDT",
            timeframe = "1H",
            description = "Institutions left their footprints before this massive Bitcoin surge. Locate the last bearish candle before the displacement.",
            winCondition = "Draw a rectangle covering at least 80% of the true Order Block (81,078 - 81,130).",
            hint = "Look for the last red/down candle right before the explosive sequence of large green candles.",
            candles = candles,
            solution = LevelSolution(
                obLow = 81078.0,
                obHigh = 81130.0,
                obCandleStartIndex = obIndex,
                obCandleEndIndex = obIndex,
                breakPoint = 81269.0,
                retestZoneLow = 81085.0,
                retestZoneHigh = 81130.0,
                recommendedEntry = 81110.0,
                recommendedSl = 81050.0,
                recommendedTp = 81600.0,
                explanation = "The last bearish down-candle at 81,078-81,130 absorbed all retail selling before banks launched the 500+ point expansion."
            ),
            isBullishSetup = true
        )
    }

    // --- LEVEL 2: EUR/USD 15M Institutional Footprint ---
    private fun createLevel2(): Level {
        val candles = generateCandleSeries(
            basePrice = 1.0820,
            preCount = 7,
            obIndex = 7,
            obOpen = 1.0828,
            obClose = 1.0818,
            obHigh = 1.0830,
            obLow = 1.0815,
            rallyAmount = 0.0045,
            isBullish = true
        )
        return Level(
            id = 2,
            title = "EUR/USD Institutional Footprint",
            concept = "Bullish OB",
            stage = LevelStage.IDENTIFICATION,
            symbol = "EUR/USD",
            timeframe = "15M",
            description = "European central banks injected heavy buy orders. Mark the exact base candle where the move originated.",
            winCondition = "Mark the 1.0815 - 1.0830 zone with high accuracy.",
            hint = "Notice the sudden increase in volume on the green impulse right after the base candle.",
            candles = candles,
            solution = LevelSolution(
                obLow = 1.0815,
                obHigh = 1.0830,
                obCandleStartIndex = 7,
                obCandleEndIndex = 7,
                breakPoint = 1.0845,
                retestZoneLow = 1.0820,
                retestZoneHigh = 1.0830,
                recommendedEntry = 1.0825,
                recommendedSl = 1.0808,
                recommendedTp = 1.0865,
                explanation = "The clean down candle at 1.0815-1.0830 represents the origin of the London session expansion."
            ),
            isBullishSetup = true
        )
    }

    // --- LEVEL 3: GBP/USD 1H Bearish Supply Block ---
    private fun createLevel3(): Level {
        val candles = generateCandleSeries(
            basePrice = 1.2950,
            preCount = 6,
            obIndex = 6,
            obOpen = 1.2940,
            obClose = 1.2965,
            obHigh = 1.2972,
            obLow = 1.2938,
            rallyAmount = -0.0060, // Dump
            isBullish = false
        )
        return Level(
            id = 3,
            title = "Bearish Supply Block",
            concept = "Bearish OB",
            stage = LevelStage.IDENTIFICATION,
            symbol = "GBP/USD",
            timeframe = "1H",
            description = "Market makers induced retail to buy before triggering a cascade of sell orders. Identify the Bearish Supply Block.",
            winCondition = "Mark the last up candle before the violent sell-off (1.2938 - 1.2972).",
            hint = "In a bearish setup, the Order Block is the last GREEN candle before the aggressive drop.",
            candles = candles,
            solution = LevelSolution(
                obLow = 1.2938,
                obHigh = 1.2972,
                obCandleStartIndex = 6,
                obCandleEndIndex = 6,
                breakPoint = 1.2920,
                retestZoneLow = 1.2945,
                retestZoneHigh = 1.2965,
                recommendedEntry = 1.2955,
                recommendedSl = 1.2985,
                recommendedTp = 1.2890,
                explanation = "The final green candle at 1.2938-1.2972 trapped late buyers before initiating the bearish market structure break."
            ),
            isBullishSetup = false
        )
    }

    // --- LEVEL 4: ETH/USDT 4H Multi-Candle Consolidation OB ---
    private fun createLevel4(): Level {
        val candles = generateCandleSeries(
            basePrice = 2650.0,
            preCount = 5,
            obIndex = 5,
            obOpen = 2660.0,
            obClose = 2640.0,
            obHigh = 2668.0,
            obLow = 2635.0,
            rallyAmount = 140.0,
            isBullish = true
        )
        return Level(
            id = 4,
            title = "Inside Bar Mitigation",
            concept = "Precision OB",
            stage = LevelStage.IDENTIFICATION,
            symbol = "ETH/USDT",
            timeframe = "4H",
            description = "Ethereum paused inside a tight consolidation before an aggressive rally. Capture the full wick-to-wick boundaries.",
            winCondition = "Encompass both the candle body and lower wick between 2635 and 2668.",
            hint = "Institutional orders rest across the entire lower shadow where stop losses were collected.",
            candles = candles,
            solution = LevelSolution(
                obLow = 2635.0,
                obHigh = 2668.0,
                obCandleStartIndex = 5,
                obCandleEndIndex = 5,
                breakPoint = 2710.0,
                retestZoneLow = 2645.0,
                retestZoneHigh = 2665.0,
                recommendedEntry = 2655.0,
                recommendedSl = 2620.0,
                recommendedTp = 2780.0,
                explanation = "The lower wick at 2635 was a liquidity grab prior to impulsive buy expansion."
            ),
            isBullishSetup = true
        )
    }

    // --- LEVEL 5: GOLD 1H Master OB Challenge ---
    private fun createLevel5(): Level {
        val candles = generateCandleSeries(
            basePrice = 2410.0,
            preCount = 8,
            obIndex = 8,
            obOpen = 2415.0,
            obClose = 2408.0,
            obHigh = 2418.0,
            obLow = 2405.0,
            rallyAmount = 45.0,
            isBullish = true
        )
        return Level(
            id = 5,
            title = "Gold Rush Order Block",
            concept = "Master OB",
            stage = LevelStage.IDENTIFICATION,
            symbol = "XAU/USD",
            timeframe = "1H",
            description = "Gold respects technical zones with razor precision. Complete this challenge to unlock Stage 2: Break & Retest!",
            winCondition = "Mark the 2405 - 2418 institutional accumulation block.",
            hint = "Look closely at the candle right before the first large green expansion bar.",
            candles = candles,
            solution = LevelSolution(
                obLow = 2405.0,
                obHigh = 2418.0,
                obCandleStartIndex = 8,
                obCandleEndIndex = 8,
                breakPoint = 2426.0,
                retestZoneLow = 2410.0,
                retestZoneHigh = 2416.0,
                recommendedEntry = 2412.0,
                recommendedSl = 2398.0,
                recommendedTp = 2450.0,
                explanation = "A textbook Bullish Order Block at 2405-2418 followed by clear displacement and Break of Structure (BOS)."
            ),
            isBullishSetup = true
        )
    }

    // --- LEVEL 6: Break & Retest: The Breakout Trap ---
    private fun createLevel6(): Level {
        val candles = generateBreakRetestCandles(
            basePrice = 82000.0,
            obLow = 81850.0,
            obHigh = 82050.0,
            trapPeakPrice = 82750.0,
            retestPrice = 82020.0,
            finalRallyPrice = 83400.0
        )
        return Level(
            id = 6,
            title = "The Breakout Trap",
            concept = "Break & Retest",
            stage = LevelStage.BREAK_AND_RETEST,
            symbol = "BTC/USDT",
            timeframe = "1H",
            description = "Price breaks out of resistance! Retail traders rush to hit BUY at the peak. DO NOT fall for the FOMO trap. Wait for the ball to retest the OB!",
            winCondition = "Wait until the ball touches the green retest zone, then tap 'ENTER TRADE'.",
            hint = "Resist the urge to tap while the breakout candle is skyrocketing. Wait for the retest dip!",
            candles = candles,
            solution = LevelSolution(
                obLow = 81850.0,
                obHigh = 82050.0,
                obCandleStartIndex = 4,
                obCandleEndIndex = 4,
                breakPoint = 82300.0,
                retestZoneLow = 81900.0,
                retestZoneHigh = 82080.0,
                recommendedEntry = 82020.0,
                recommendedSl = 81750.0,
                recommendedTp = 83400.0,
                trapCandleIndex = 8,
                retestCandleIndex = 12,
                explanation = "Retail traders buy the candle peak at candle #8. Smart money lets price pull back to candle #12 (retest) before launching the true rally."
            ),
            isBullishSetup = true
        )
    }

    // --- LEVEL 7: Patience on the Retest ---
    private fun createLevel7(): Level {
        val candles = generateBreakRetestCandles(
            basePrice = 1.0850,
            obLow = 1.0840,
            obHigh = 1.0858,
            trapPeakPrice = 1.0910,
            retestPrice = 1.0855,
            finalRallyPrice = 1.0960
        )
        return Level(
            id = 7,
            title = "Patience on the Retest",
            concept = "Retest Timing",
            stage = LevelStage.BREAK_AND_RETEST,
            symbol = "EUR/USD",
            timeframe = "30M",
            description = "The market is testing your discipline. Watch the price ball approach the old Order Block ceiling. Tap the exact moment it bounces!",
            winCondition = "Trigger your buy order at the retest zone.",
            hint = "Let the price wick into 1.0840 - 1.0858 before taking action.",
            candles = candles,
            solution = LevelSolution(
                obLow = 1.0840,
                obHigh = 1.0858,
                obCandleStartIndex = 4,
                obCandleEndIndex = 4,
                breakPoint = 1.0875,
                retestZoneLow = 1.0845,
                retestZoneHigh = 1.0860,
                recommendedEntry = 1.0855,
                recommendedSl = 1.0830,
                recommendedTp = 1.0960,
                trapCandleIndex = 8,
                retestCandleIndex = 13,
                explanation = "Entering on the retest provides the smallest possible invalidation distance with maximum reward potential."
            ),
            isBullishSetup = true
        )
    }

    // --- LEVEL 8: Liquidity Sweep Retest ---
    private fun createLevel8(): Level {
        val candles = generateBreakRetestCandles(
            basePrice = 1.3000,
            obLow = 1.2985,
            obHigh = 1.3015,
            trapPeakPrice = 1.3070,
            retestPrice = 1.3005,
            finalRallyPrice = 1.3140
        )
        return Level(
            id = 8,
            title = "The Liquidity Sweep Retest",
            concept = "Mitigation Bounce",
            stage = LevelStage.BREAK_AND_RETEST,
            symbol = "GBP/USD",
            timeframe = "1H",
            description = "A classic inducement cycle. Notice how the market collects resting stop orders before returning to the original Order Block.",
            winCondition = "Execute buy order on the mitigation candle.",
            hint = "The ball will decelerate as it touches the Order Block floor.",
            candles = candles,
            solution = LevelSolution(
                obLow = 1.2985,
                obHigh = 1.3015,
                obCandleStartIndex = 4,
                obCandleEndIndex = 4,
                breakPoint = 1.3030,
                retestZoneLow = 1.2995,
                retestZoneHigh = 1.3015,
                recommendedEntry = 1.3005,
                recommendedSl = 1.2970,
                recommendedTp = 1.3140,
                trapCandleIndex = 8,
                retestCandleIndex = 12,
                explanation = "Price mitigates the open interest in the order block and respects it as strong support."
            ),
            isBullishSetup = true
        )
    }

    // --- LEVEL 9: Fast Wick Rejection ---
    private fun createLevel9(): Level {
        val candles = generateBreakRetestCandles(
            basePrice = 145.0,
            obLow = 144.2,
            obHigh = 145.8,
            trapPeakPrice = 152.0,
            retestPrice = 145.4,
            finalRallyPrice = 160.0
        )
        return Level(
            id = 9,
            title = "Fast Wick Rejection",
            concept = "High Volatility Retest",
            stage = LevelStage.BREAK_AND_RETEST,
            symbol = "SOL/USDT",
            timeframe = "15M",
            description = "Crypto moves rapidly! Be ready on the trigger as the ball dips to the order block zone.",
            winCondition = "Tap ENTER within 2 candles of the retest touch.",
            hint = "Focus on the 144.2 - 145.8 green floor.",
            candles = candles,
            solution = LevelSolution(
                obLow = 144.2,
                obHigh = 145.8,
                obCandleStartIndex = 4,
                obCandleEndIndex = 4,
                breakPoint = 147.5,
                retestZoneLow = 144.8,
                retestZoneHigh = 145.8,
                recommendedEntry = 145.4,
                recommendedSl = 142.9,
                recommendedTp = 160.0,
                trapCandleIndex = 8,
                retestCandleIndex = 12,
                explanation = "Solana's aggressive wick rejection shows strong institutional buying power defending the block."
            ),
            isBullishSetup = true
        )
    }

    // --- LEVEL 10: Stage 2 Boss: The Breaker Flip ---
    private fun createLevel10(): Level {
        val candles = generateBreakRetestCandles(
            basePrice = 84000.0,
            obLow = 83800.0,
            obHigh = 84150.0,
            trapPeakPrice = 85200.0,
            retestPrice = 84050.0,
            finalRallyPrice = 86500.0
        )
        return Level(
            id = 10,
            title = "Stage 2 Boss: The Breaker Flip",
            concept = "Resistance to Support",
            stage = LevelStage.BREAK_AND_RETEST,
            symbol = "BTC/USDT",
            timeframe = "4H",
            description = "The ultimate test of trade timing! Successfully execute on the retest to unlock Stage 3: Full Trade & Risk/Reward.",
            winCondition = "Avoid the breakout trap at peak and enter on retest.",
            hint = "Watch the candles compress into the 84,000 zone.",
            candles = candles,
            solution = LevelSolution(
                obLow = 83800.0,
                obHigh = 84150.0,
                obCandleStartIndex = 4,
                obCandleEndIndex = 4,
                breakPoint = 84500.0,
                retestZoneLow = 83900.0,
                retestZoneHigh = 84150.0,
                recommendedEntry = 84050.0,
                recommendedSl = 83500.0,
                recommendedTp = 86500.0,
                trapCandleIndex = 8,
                retestCandleIndex = 12,
                explanation = "Mastery achieved! Old resistance flipped cleanly into dynamic institutional support."
            ),
            isBullishSetup = true
        )
    }

    // --- LEVEL 11: Stage 3: Full Trade - Placing the Shield (Stop Loss) ---
    private fun createLevel11(): Level {
        val candles = generateTradeSetupCandles(82500.0, 82350.0, 82550.0, 83600.0)
        return Level(
            id = 11,
            title = "Placing the Shield (Stop Loss)",
            concept = "Invalidation Defense",
            stage = LevelStage.FULL_TRADE,
            symbol = "BTC/USDT",
            timeframe = "1H",
            description = "Where you place your Stop Loss defines your trading career. Place your Entry in the retest zone, and your SL safely below the OB low.",
            winCondition = "Entry in retest zone (82,450-82,550), SL strictly below 82,350, TP at 83,500+.",
            hint = "Never place SL inside the Order Block. Leave a slight buffer below the lowest wick.",
            candles = candles,
            solution = LevelSolution(
                obLow = 82350.0,
                obHigh = 82550.0,
                obCandleStartIndex = 3,
                obCandleEndIndex = 3,
                breakPoint = 82800.0,
                retestZoneLow = 82450.0,
                retestZoneHigh = 82550.0,
                recommendedEntry = 82500.0,
                recommendedSl = 82250.0,
                recommendedTp = 83500.0,
                explanation = "Placing SL at 82,250 keeps your trade protected beneath the institutional order floor."
            ),
            isBullishSetup = true
        )
    }

    // --- LEVEL 12: Targeting Liquidity (Take Profit) ---
    private fun createLevel12(): Level {
        val candles = generateTradeSetupCandles(1.0850, 1.0838, 1.0855, 1.0920)
        return Level(
            id = 12,
            title = "Targeting Liquidity",
            concept = "Take Profit Placement",
            stage = LevelStage.FULL_TRADE,
            symbol = "EUR/USD",
            timeframe = "1H",
            description = "Identify the major swing high where retail stop losses and buy orders are resting. Set your TP to take profit right there.",
            winCondition = "Set TP target at 1.0920 with protected SL below 1.0838.",
            hint = "Look for the previous high on the chart.",
            candles = candles,
            solution = LevelSolution(
                obLow = 1.0838,
                obHigh = 1.0855,
                obCandleStartIndex = 3,
                obCandleEndIndex = 3,
                breakPoint = 1.0880,
                retestZoneLow = 1.0845,
                retestZoneHigh = 1.0855,
                recommendedEntry = 1.0850,
                recommendedSl = 1.0825,
                recommendedTp = 1.0920,
                explanation = "Targeting the old swing high captures the full liquidity pool before any reversal."
            ),
            isBullishSetup = true
        )
    }

    // --- LEVEL 13: The 1:2 Golden Ratio ---
    private fun createLevel13(): Level {
        val candles = generateTradeSetupCandles(1.3050, 1.3035, 1.3060, 1.3140)
        return Level(
            id = 13,
            title = "The 1:2 Golden Ratio",
            concept = "Risk:Reward 1:2",
            stage = LevelStage.FULL_TRADE,
            symbol = "GBP/USD",
            timeframe = "1H",
            description = "Professional traders only take setups where the reward is at least twice the risk. Achieve at least a 1:2.0 R:R.",
            winCondition = "Achieve a verified Risk/Reward ratio of 1:2 or higher.",
            hint = "Risk = Entry - SL. Reward = TP - Entry. Ensure Reward >= 2 * Risk.",
            candles = candles,
            solution = LevelSolution(
                obLow = 1.3035,
                obHigh = 1.3060,
                obCandleStartIndex = 3,
                obCandleEndIndex = 3,
                breakPoint = 1.3085,
                retestZoneLow = 1.3045,
                retestZoneHigh = 1.3060,
                recommendedEntry = 1.3055,
                recommendedSl = 1.3020,
                recommendedTp = 1.3135,
                explanation = "With a 35 pip risk and an 80 pip target, this setup provides a fantastic 1:2.3 Risk/Reward ratio."
            ),
            isBullishSetup = true
        )
    }

    // --- LEVEL 14: Sniper 1:3 R:R Setup ---
    private fun createLevel14(): Level {
        val candles = generateTradeSetupCandles(2680.0, 2665.0, 2690.0, 2790.0)
        return Level(
            id = 14,
            title = "Sniper 1:3 R:R Setup",
            concept = "Asymmetric Payoff",
            stage = LevelStage.FULL_TRADE,
            symbol = "ETH/USDT",
            timeframe = "4H",
            description = "Tight invalidation paired with a massive upside target. Calibrate Entry, SL, and TP to hit 1:3.0+ R:R.",
            winCondition = "Reach 1:3.0 Risk/Reward ratio with proper level placement.",
            hint = "A tighter SL just under the OB low increases the R:R multiple dramatically.",
            candles = candles,
            solution = LevelSolution(
                obLow = 2665.0,
                obHigh = 2690.0,
                obCandleStartIndex = 3,
                obCandleEndIndex = 3,
                breakPoint = 2720.0,
                retestZoneLow = 2675.0,
                retestZoneHigh = 2690.0,
                recommendedEntry = 2685.0,
                recommendedSl = 2655.0,
                recommendedTp = 2785.0,
                explanation = "Risking 30 points to gain 100 points produces a stellar 1:3.3 R:R trade."
            ),
            isBullishSetup = true
        )
    }

    // --- LEVEL 15: Stage 3 Boss: Prop Firm Challenge ---
    private fun createLevel15(): Level {
        val candles = generateTradeSetupCandles(85000.0, 84750.0, 85150.0, 86800.0)
        return Level(
            id = 15,
            title = "Stage 3 Boss: Prop Firm Evaluation",
            concept = "Institutional Execution",
            stage = LevelStage.FULL_TRADE,
            symbol = "BTC/USDT",
            timeframe = "1H",
            description = "Simulate a live prop firm qualification trade. Perfectly align Entry, protected SL, and high R:R target.",
            winCondition = "Score 90+ points across all three trade markers.",
            hint = "Check each parameter: Entry inside retest, SL below 84,750, TP at 86,500+.",
            candles = candles,
            solution = LevelSolution(
                obLow = 84750.0,
                obHigh = 85150.0,
                obCandleStartIndex = 3,
                obCandleEndIndex = 3,
                breakPoint = 85500.0,
                retestZoneLow = 84900.0,
                retestZoneHigh = 85150.0,
                recommendedEntry = 85050.0,
                recommendedSl = 84550.0,
                recommendedTp = 86600.0,
                explanation = "Flawless execution! Passed the prop firm criteria with disciplined risk parameters."
            ),
            isBullishSetup = true
        )
    }

    // --- LEVELS 16 to 20: Stage 4 Advanced SMC Combine ---
    private fun createLevel16(): Level = createAdvancedLevel(
        id = 16,
        title = "Fair Value Gap (FVG) Confluence",
        symbol = "BTC/USDT",
        timeframe = "1H",
        basePrice = 86000.0,
        concept = "FVG + OB",
        description = "Where the Order Block meets an unfilled Fair Value Gap imbalance. High-grade institutional liquidity."
    )

    private fun createLevel17(): Level = createAdvancedLevel(
        id = 17,
        title = "Change of Character (CHOCH)",
        symbol = "EUR/USD",
        timeframe = "15M",
        basePrice = 1.0920,
        concept = "CHOCH Reversal",
        description = "Trend shift confirmed when the previous structural low is broken with aggressive velocity."
    )

    private fun createLevel18(): Level = createAdvancedLevel(
        id = 18,
        title = "Equal Highs Liquidity Pool",
        symbol = "XAU/USD",
        timeframe = "1H",
        basePrice = 2440.0,
        concept = "Liquidity Pool",
        description = "Double tops are liquidity magnets. Banks will sweep them to fill large institutions' orders."
    )

    private fun createLevel19(): Level = createAdvancedLevel(
        id = 19,
        title = "Breaker Block Transformation",
        symbol = "GBP/USD",
        timeframe = "4H",
        basePrice = 1.3120,
        concept = "Breaker Block",
        description = "A failed Order Block that traded through transforms into a potent Breaker Block support level."
    )

    private fun createLevel20(): Level = createAdvancedLevel(
        id = 20,
        title = "The FX Bounce Master Combine",
        symbol = "BTC/USDT",
        timeframe = "1D",
        basePrice = 88000.0,
        concept = "Master Combine",
        description = "The pinnacle test of price action mastery. Synthesize Order Blocks, liquidity sweeps, and sniper risk management."
    )

    // --- HELPERS FOR SYNTHESIZING CANDLE CHARTS ---

    private fun generateCandleSeries(
        basePrice: Double,
        preCount: Int,
        obIndex: Int,
        obOpen: Double,
        obClose: Double,
        obHigh: Double,
        obLow: Double,
        rallyAmount: Double,
        isBullish: Boolean
    ): List<Candle> {
        val candles = mutableListOf<Candle>()
        var t = 1723450000000L
        val step = if (isBullish) 0.0003 * basePrice else -0.0003 * basePrice

        // Pre-candles
        for (i in 0 until preCount) {
            val o = basePrice + (i % 3 - 1) * step
            val c = o + (if (i % 2 == 0) step * 0.8 else -step * 0.8)
            val h = maxOf(o, c) + kotlin.math.abs(step) * 1.2
            val l = minOf(o, c) - kotlin.math.abs(step) * 1.2
            candles.add(Candle(t, o, h, l, c, 100.0 + i * 10))
            t += 3600000
        }

        // OB Candle
        candles.add(Candle(t, obOpen, obHigh, obLow, obClose, 280.0))
        t += 3600000

        // Impulse candles
        val impulseCount = 5
        val impulseStep = rallyAmount / impulseCount
        var curr = obClose
        for (i in 0 until impulseCount) {
            val o = curr
            curr += impulseStep
            val c = curr
            val h = maxOf(o, c) + kotlin.math.abs(impulseStep) * 0.3
            val l = minOf(o, c) - kotlin.math.abs(impulseStep) * 0.2
            candles.add(Candle(t, o, h, l, c, 350.0 + i * 40))
            t += 3600000
        }

        // Pullback candles
        val pullbackCount = 4
        val pullbackStep = -(rallyAmount * 0.65) / pullbackCount
        for (i in 0 until pullbackCount) {
            val o = curr
            curr += pullbackStep
            val c = curr
            val h = maxOf(o, c) + kotlin.math.abs(pullbackStep) * 0.2
            val l = minOf(o, c) - kotlin.math.abs(pullbackStep) * 0.2
            candles.add(Candle(t, o, h, l, c, 180.0))
            t += 3600000
        }

        return candles
    }

    private fun generateBreakRetestCandles(
        basePrice: Double,
        obLow: Double,
        obHigh: Double,
        trapPeakPrice: Double,
        retestPrice: Double,
        finalRallyPrice: Double
    ): List<Candle> {
        val candles = mutableListOf<Candle>()
        var t = 1723450000000L

        // Prior range (candles 0-3)
        candles.add(Candle(t, basePrice - 40, basePrice + 10, basePrice - 70, basePrice - 10, 110.0)); t += 3600000
        candles.add(Candle(t, basePrice - 10, basePrice + 30, basePrice - 40, basePrice + 15, 120.0)); t += 3600000
        candles.add(Candle(t, basePrice + 15, basePrice + 50, basePrice - 10, basePrice + 40, 115.0)); t += 3600000
        candles.add(Candle(t, basePrice + 40, basePrice + 60, basePrice + 10, basePrice + 20, 130.0)); t += 3600000

        // Candle 4: The Order Block
        candles.add(Candle(t, obHigh, obHigh + 15, obLow, obLow + 20, 310.0)); t += 3600000

        // Candles 5-7: Breakout sequence
        candles.add(Candle(t, obLow + 25, obHigh + 80, obLow + 15, obHigh + 70, 240.0)); t += 3600000
        candles.add(Candle(t, obHigh + 70, obHigh + 220, obHigh + 50, obHigh + 200, 320.0)); t += 3600000
        candles.add(Candle(t, obHigh + 200, obHigh + 450, obHigh + 180, obHigh + 420, 480.0)); t += 3600000

        // Candle 8: THE TRAP (peak FOMO breakout candle)
        candles.add(Candle(t, obHigh + 420, trapPeakPrice, obHigh + 390, trapPeakPrice - 80, 520.0)); t += 3600000

        // Candles 9-11: Pullback phase
        candles.add(Candle(t, trapPeakPrice - 80, trapPeakPrice - 40, trapPeakPrice - 260, trapPeakPrice - 240, 210.0)); t += 3600000
        candles.add(Candle(t, trapPeakPrice - 240, trapPeakPrice - 200, trapPeakPrice - 480, trapPeakPrice - 450, 190.0)); t += 3600000
        candles.add(Candle(t, trapPeakPrice - 450, trapPeakPrice - 400, retestPrice + 80, retestPrice + 90, 180.0)); t += 3600000

        // Candle 12: THE RETEST TOUCH (Ball bounce springboard)
        candles.add(Candle(t, retestPrice + 90, retestPrice + 120, obLow + 10, retestPrice + 80, 390.0)); t += 3600000

        // Candles 13-16: True expansion rally
        candles.add(Candle(t, retestPrice + 80, retestPrice + 350, retestPrice + 60, retestPrice + 320, 410.0)); t += 3600000
        candles.add(Candle(t, retestPrice + 320, retestPrice + 650, retestPrice + 300, retestPrice + 620, 440.0)); t += 3600000
        candles.add(Candle(t, retestPrice + 620, finalRallyPrice, retestPrice + 580, finalRallyPrice - 40, 490.0)); t += 3600000

        return candles
    }

    private fun generateTradeSetupCandles(
        basePrice: Double,
        obLow: Double,
        obHigh: Double,
        targetPrice: Double
    ): List<Candle> {
        val candles = mutableListOf<Candle>()
        var t = 1723450000000L
        val span = obHigh - obLow

        // Pre candles
        candles.add(Candle(t, basePrice - span, basePrice, basePrice - span * 1.5, basePrice - span * 0.5, 100.0)); t += 3600000
        candles.add(Candle(t, basePrice - span * 0.5, basePrice + span * 0.5, basePrice - span, basePrice, 120.0)); t += 3600000
        candles.add(Candle(t, basePrice, basePrice + span * 0.8, basePrice - span * 0.2, basePrice + span * 0.3, 110.0)); t += 3600000

        // OB candle (index 3)
        candles.add(Candle(t, obHigh, obHigh + span * 0.1, obLow, obLow + span * 0.2, 350.0)); t += 3600000

        // Rally leg
        candles.add(Candle(t, obLow + span * 0.3, obHigh + span * 1.5, obLow + span * 0.2, obHigh + span * 1.3, 300.0)); t += 3600000
        candles.add(Candle(t, obHigh + span * 1.3, obHigh + span * 3.0, obHigh + span * 1.1, obHigh + span * 2.8, 380.0)); t += 3600000
        candles.add(Candle(t, obHigh + span * 2.8, obHigh + span * 4.2, obHigh + span * 2.5, obHigh + span * 4.0, 420.0)); t += 3600000

        // Retest pullback
        candles.add(Candle(t, obHigh + span * 4.0, obHigh + span * 4.1, obHigh + span * 2.2, obHigh + span * 2.4, 200.0)); t += 3600000
        candles.add(Candle(t, obHigh + span * 2.4, obHigh + span * 2.5, obHigh + span * 0.8, obHigh + span * 1.0, 180.0)); t += 3600000
        candles.add(Candle(t, obHigh + span * 1.0, obHigh + span * 1.2, obLow + span * 0.4, obLow + span * 0.8, 260.0)); t += 3600000

        // Target expansion
        candles.add(Candle(t, obLow + span * 0.8, obHigh + span * 2.5, obLow + span * 0.6, obHigh + span * 2.3, 390.0)); t += 3600000
        candles.add(Candle(t, obHigh + span * 2.3, targetPrice, obHigh + span * 2.0, targetPrice - span * 0.2, 450.0)); t += 3600000

        return candles
    }

    private fun createAdvancedLevel(
        id: Int,
        title: String,
        symbol: String,
        timeframe: String,
        basePrice: Double,
        concept: String,
        description: String
    ): Level {
        val delta = basePrice * 0.015
        val candles = generateTradeSetupCandles(
            basePrice = basePrice,
            obLow = basePrice - delta * 0.3,
            obHigh = basePrice + delta * 0.2,
            targetPrice = basePrice + delta * 2.5
        )
        return Level(
            id = id,
            title = title,
            concept = concept,
            stage = LevelStage.ADVANCED_SMC,
            symbol = symbol,
            timeframe = timeframe,
            description = description,
            winCondition = "Establish full professional trade setup with 1:2+ R:R ratio.",
            hint = "Look for confluence between the Order Block and liquidity zones.",
            candles = candles,
            solution = LevelSolution(
                obLow = basePrice - delta * 0.3,
                obHigh = basePrice + delta * 0.2,
                obCandleStartIndex = 3,
                obCandleEndIndex = 3,
                breakPoint = basePrice + delta * 0.8,
                retestZoneLow = basePrice - delta * 0.1,
                retestZoneHigh = basePrice + delta * 0.2,
                recommendedEntry = basePrice,
                recommendedSl = basePrice - delta * 0.55,
                recommendedTp = basePrice + delta * 2.4,
                explanation = "Advanced confluence detected: Institutional mitigation coupled with clear target liquidity."
            ),
            isBullishSetup = true
        )
    }
}
