package com.example.data.model

data class ConceptItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val rule: String,
    val keyPoints: List<String>,
    val proTip: String,
    val category: String
)

object GlossaryData {
    val items = listOf(
        ConceptItem(
            id = "order_block",
            title = "Order Block (OB)",
            subtitle = "Institutional Footprint",
            rule = "The last down-candle before a high-volume, impulsive green rally (Bullish OB), or the last up-candle before an aggressive sell-off (Bearish OB).",
            keyPoints = listOf(
                "Created by institutional banks placing massive market orders",
                "Must be followed by strong displacement breaking previous market structure",
                "Price tends to return to mitigate unfulfilled resting buy/sell limits"
            ),
            proTip = "Valid OBs leave an imbalance (FVG) immediately after their formation.",
            category = "Foundations"
        ),
        ConceptItem(
            id = "break_and_retest",
            title = "Break & Retest",
            subtitle = "Patience Over FOMO",
            rule = "When price breaks past resistance/OB, retail traders buy the breakout peak (the Trap). Smart money waits for the retest dip to bounce.",
            keyPoints = listOf(
                "Never buy at the peak of an explosive breakout candle",
                "Wait for price to re-enter the upper boundary of the Order Block",
                "Enter when the bounce wick confirms rejection of the zone"
            ),
            proTip = "If price slices cleanly through the OB without bouncing, it is invalidated.",
            category = "Entry Techniques"
        ),
        ConceptItem(
            id = "risk_reward",
            title = "Risk:Reward (R:R)",
            subtitle = "The Professional Edge",
            rule = "Keep Stop Loss strictly protected below the Order Block invalidation level, targeting at least 1:2 or 1:3 profit.",
            keyPoints = listOf(
                "Never place Stop Loss directly inside the Order Block — place it just below the wick",
                "Take Profit should target recent swing highs or unmitigated liquidity pools",
                "With a 1:2 R:R ratio, you can be right only 40% of the time and still be consistently profitable"
            ),
            proTip = "A tight invalidation invalidates quickly if you are wrong, saving trading capital.",
            category = "Risk Management"
        ),
        ConceptItem(
            id = "liquidity_sweep",
            title = "Liquidity Sweep & Traps",
            subtitle = "Where Retail Gets Liquidated",
            rule = "Market makers briefly puncture equal highs or equal lows to sweep retail stop orders before reversing aggressively in the true direction.",
            keyPoints = listOf(
                "Equal highs/lows are magnets for price action",
                "A swift wick beyond the level followed by an instant close back inside signals a sweep",
                "Combine liquidity sweeps with Order Block retests for sniper entries"
            ),
            proTip = "When you see an obvious breakout that everyone is talking about, prepare for the sweep.",
            category = "Advanced SMC"
        ),
        ConceptItem(
            id = "bos_choch",
            title = "BOS vs CHOCH",
            subtitle = "Reading Trend Structure",
            rule = "BOS (Break of Structure) confirms continuation of the prevailing trend. CHOCH (Change of Character) signals the first warning of a trend reversal.",
            keyPoints = listOf(
                "BOS: Price creates a higher high in an uptrend",
                "CHOCH: Price violates the most recent higher low that caused the last high",
                "The Order Block created right before a CHOCH is one of the highest probability trade zones"
            ),
            proTip = "Always trade in the direction of the higher timeframe trend until a confirmed CHOCH occurs.",
            category = "Market Structure"
        )
    )
}
