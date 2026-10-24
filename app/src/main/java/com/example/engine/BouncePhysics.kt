package com.example.engine

import androidx.compose.ui.geometry.Offset
import kotlin.math.sin

data class BallState(
    val currentCandleIndex: Float = 0f,
    val position: Offset = Offset.Zero,
    val velocityY: Float = 0f,
    val isBouncing: Boolean = false,
    val isInOrderBlockZone: Boolean = false,
    val isInRetestZone: Boolean = false,
    val trail: List<Offset> = emptyList(),
    val bounceImpulse: Float = 0f
)

object BouncePhysics {

    /**
     * Computes the screen X coordinate for a given candle index
     */
    fun getCandleX(index: Float, candleWidth: Float, spacing: Float, startOffset: Float): Float {
        return startOffset + index * (candleWidth + spacing) + candleWidth / 2f
    }

    /**
     * Converts a price value to canvas Y coordinate
     */
    fun priceToY(price: Double, minPrice: Double, maxPrice: Double, canvasHeight: Float, padding: Float = 40f): Float {
        val range = maxPrice - minPrice
        if (range <= 0.00001) return canvasHeight / 2f
        val usableHeight = canvasHeight - padding * 2
        val normalized = ((maxPrice - price) / range).toFloat()
        return padding + normalized * usableHeight
    }

    /**
     * Converts canvas Y coordinate to price
     */
    fun yToPrice(y: Float, minPrice: Double, maxPrice: Double, canvasHeight: Float, padding: Float = 40f): Double {
        val range = maxPrice - minPrice
        val usableHeight = canvasHeight - padding * 2
        val clampedY = (y - padding).coerceIn(0f, usableHeight)
        val normalized = clampedY / usableHeight
        return maxPrice - (normalized * range)
    }

    /**
     * Updates ball physics along the candlestick price path
     */
    fun updateBall(
        progress: Float, // from 0f to candles.size - 1
        candleYPositions: List<Float>,
        candleXPositions: List<Float>,
        obYTop: Float?,
        obYBottom: Float?,
        currentTrail: List<Offset>
    ): BallState {
        if (candleYPositions.isEmpty() || candleXPositions.isEmpty()) {
            return BallState()
        }

        val maxIndex = candleYPositions.size - 1
        val clampedProgress = progress.coerceIn(0f, maxIndex.toFloat())

        val index0 = clampedProgress.toInt().coerceIn(0, maxIndex)
        val index1 = (index0 + 1).coerceIn(0, maxIndex)
        val fract = clampedProgress - index0

        val x0 = candleXPositions[index0]
        val x1 = candleXPositions[index1]
        val currentX = x0 + (x1 - x0) * fract

        val y0 = candleYPositions[index0]
        val y1 = candleYPositions[index1]
        val basePriceY = y0 + (y1 - y0) * fract

        // Parabolic bounce curve between candles for realistic bouncing ball sensation
        val bounceArc = (sin(fract * Math.PI).toFloat() * 18f)
        val ballY = basePriceY - bounceArc

        val currentPos = Offset(currentX, ballY)

        // Check if inside OB zone
        val inOB = if (obYTop != null && obYBottom != null) {
            val top = minOf(obYTop, obYBottom)
            val bottom = maxOf(obYTop, obYBottom)
            ballY in top..bottom
        } else false

        // Update trail (up to 8 points)
        val newTrail = (listOf(currentPos) + currentTrail).take(8)

        return BallState(
            currentCandleIndex = clampedProgress,
            position = currentPos,
            isBouncing = bounceArc > 4f,
            isInOrderBlockZone = inOB,
            isInRetestZone = inOB,
            trail = newTrail,
            bounceImpulse = bounceArc
        )
    }
}
