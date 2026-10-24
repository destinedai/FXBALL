package com.example.data.model

data class Candle(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 100.0
) {
    val isBullish: Boolean get() = close >= open
    val range: Double get() = high - low
    val bodyTop: Double get() = maxOf(open, close)
    val bodyBottom: Double get() = minOf(open, close)
    val upperWick: Double get() = high - bodyTop
    val lowerWick: Double get() = bodyBottom - low
}
