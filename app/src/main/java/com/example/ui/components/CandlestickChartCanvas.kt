package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Candle
import com.example.data.model.Level
import com.example.data.model.TradeSetup
import com.example.data.model.UserBox
import com.example.engine.BallState
import com.example.engine.BouncePhysics
import com.example.ui.theme.CandleBearish
import com.example.ui.theme.CandleBullish
import com.example.ui.theme.ChartBackground
import com.example.ui.theme.ChartGrid
import com.example.ui.theme.ChartGridSubtle
import com.example.ui.theme.EntryLineColor
import com.example.ui.theme.OrderBlockFill
import com.example.ui.theme.OrderBlockStroke
import com.example.ui.theme.StopLossColor
import com.example.ui.theme.TakeProfitColor
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class ChartToolMode {
    MARK_OB,
    TRADE_LEVELS,
    VIEW_ONLY
}

@Composable
fun CandlestickChartCanvas(
    level: Level,
    userBox: UserBox?,
    onUserBoxChange: (UserBox) -> Unit,
    tradeSetup: TradeSetup,
    onTradeSetupChange: (TradeSetup) -> Unit,
    ballState: BallState,
    toolMode: ChartToolMode,
    showSolutionOverlay: Boolean,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val candles = level.candles

    var dragStartOffset by remember { mutableStateOf<Offset?>(null) }
    var dragCurrentOffset by remember { mutableStateOf<Offset?>(null) }

    // Active trade line dragging in TRADE_LEVELS mode
    var activeDraggingLine by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("chart_canvas_container")
            .pointerInput(toolMode, level, userBox, tradeSetup) {
                detectTapGestures { tapOffset ->
                    val width = size.width.toFloat()
                    val height = size.height.toFloat()
                    val padding = 45f
                    val rightScaleWidth = 140f
                    val chartWidth = width - rightScaleWidth

                    val minP = candles.minOfOrNull { it.low } ?: 0.0
                    val maxP = candles.maxOfOrNull { it.high } ?: 100.0
                    val tappedPrice = BouncePhysics.yToPrice(tapOffset.y, minP, maxP, height, padding)

                    val candleSpacing = chartWidth / max(1, candles.size)
                    val tappedIndex = ((tapOffset.x - 20f) / candleSpacing).toInt().coerceIn(0, candles.size - 1)

                    if (toolMode == ChartToolMode.MARK_OB) {
                        // Tap to initialize or expand OB around candle
                        val tappedCandle = candles[tappedIndex]
                        val high = tappedCandle.high
                        val low = tappedCandle.low
                        onUserBoxChange(
                            UserBox(
                                fromCandleIndex = max(0, tappedIndex - 1),
                                toCandleIndex = min(candles.size - 1, tappedIndex + 1),
                                priceHigh = high,
                                priceLow = low
                            )
                        )
                    } else if (toolMode == ChartToolMode.TRADE_LEVELS) {
                        // Quick set: if closest to entry/sl/tp
                        when {
                            tradeSetup.entryPrice == null -> onTradeSetupChange(tradeSetup.copy(entryPrice = tappedPrice))
                            tradeSetup.stopLossPrice == null -> onTradeSetupChange(tradeSetup.copy(stopLossPrice = tappedPrice))
                            tradeSetup.takeProfitPrice == null -> onTradeSetupChange(tradeSetup.copy(takeProfitPrice = tappedPrice))
                            else -> {
                                // Update whichever line is closest to tap
                                val distEntry = abs(tapOffset.y - BouncePhysics.priceToY(tradeSetup.entryPrice!!, minP, maxP, height, padding))
                                val distSl = abs(tapOffset.y - BouncePhysics.priceToY(tradeSetup.stopLossPrice!!, minP, maxP, height, padding))
                                val distTp = abs(tapOffset.y - BouncePhysics.priceToY(tradeSetup.takeProfitPrice!!, minP, maxP, height, padding))
                                val minDist = minOf(distEntry, distSl, distTp)
                                when (minDist) {
                                    distEntry -> onTradeSetupChange(tradeSetup.copy(entryPrice = tappedPrice))
                                    distSl -> onTradeSetupChange(tradeSetup.copy(stopLossPrice = tappedPrice))
                                    else -> onTradeSetupChange(tradeSetup.copy(takeProfitPrice = tappedPrice))
                                }
                            }
                        }
                    }
                }
            }
            .pointerInput(toolMode, level, userBox, tradeSetup) {
                detectDragGestures(
                    onDragStart = { start ->
                        dragStartOffset = start
                        dragCurrentOffset = start

                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val padding = 45f
                        val minP = candles.minOfOrNull { it.low } ?: 0.0
                        val maxP = candles.maxOfOrNull { it.high } ?: 100.0

                        if (toolMode == ChartToolMode.TRADE_LEVELS) {
                            // Detect if dragging near SL, Entry, or TP
                            val entryY = tradeSetup.entryPrice?.let { BouncePhysics.priceToY(it, minP, maxP, height, padding) }
                            val slY = tradeSetup.stopLossPrice?.let { BouncePhysics.priceToY(it, minP, maxP, height, padding) }
                            val tpY = tradeSetup.takeProfitPrice?.let { BouncePhysics.priceToY(it, minP, maxP, height, padding) }

                            activeDraggingLine = when {
                                entryY != null && abs(start.y - entryY) < 35f -> "ENTRY"
                                slY != null && abs(start.y - slY) < 35f -> "SL"
                                tpY != null && abs(start.y - tpY) < 35f -> "TP"
                                else -> "ENTRY" // default
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val current = dragCurrentOffset?.plus(dragAmount) ?: change.position
                        dragCurrentOffset = current

                        val width = size.width.toFloat()
                        val height = size.height.toFloat()
                        val padding = 45f
                        val rightScaleWidth = 140f
                        val chartWidth = width - rightScaleWidth
                        val candleSpacing = chartWidth / max(1, candles.size)

                        val minP = candles.minOfOrNull { it.low } ?: 0.0
                        val maxP = candles.maxOfOrNull { it.high } ?: 100.0

                        if (toolMode == ChartToolMode.MARK_OB && dragStartOffset != null) {
                            val startX = dragStartOffset!!.x
                            val currX = current.x
                            val startIdx = ((startX - 20f) / candleSpacing).toInt().coerceIn(0, candles.size - 1)
                            val currIdx = ((currX - 20f) / candleSpacing).toInt().coerceIn(0, candles.size - 1)

                            val p1 = BouncePhysics.yToPrice(dragStartOffset!!.y, minP, maxP, height, padding)
                            val p2 = BouncePhysics.yToPrice(current.y, minP, maxP, height, padding)

                            onUserBoxChange(
                                UserBox(
                                    fromCandleIndex = min(startIdx, currIdx),
                                    toCandleIndex = max(startIdx, currIdx),
                                    priceHigh = max(p1, p2),
                                    priceLow = min(p1, p2)
                                )
                            )
                        } else if (toolMode == ChartToolMode.TRADE_LEVELS) {
                            val draggedPrice = BouncePhysics.yToPrice(current.y, minP, maxP, height, padding)
                            when (activeDraggingLine) {
                                "ENTRY" -> onTradeSetupChange(tradeSetup.copy(entryPrice = draggedPrice))
                                "SL" -> onTradeSetupChange(tradeSetup.copy(stopLossPrice = draggedPrice))
                                "TP" -> onTradeSetupChange(tradeSetup.copy(takeProfitPrice = draggedPrice))
                            }
                        }
                    },
                    onDragEnd = {
                        dragStartOffset = null
                        dragCurrentOffset = null
                        activeDraggingLine = null
                    },
                    onDragCancel = {
                        dragStartOffset = null
                        dragCurrentOffset = null
                        activeDraggingLine = null
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val padding = 45f
            val rightScaleWidth = 140f
            val chartWidth = canvasWidth - rightScaleWidth

            val minPrice = candles.minOfOrNull { it.low } ?: 0.0
            val maxPrice = candles.maxOfOrNull { it.high } ?: 100.0

            // 1. Background & TradingView Grid
            drawRect(color = ChartBackground)

            val gridSteps = 6
            val priceStep = (maxPrice - minPrice) / gridSteps
            for (i in 0..gridSteps) {
                val gridPrice = minPrice + i * priceStep
                val y = BouncePhysics.priceToY(gridPrice, minPrice, maxPrice, canvasHeight, padding)

                // Grid line
                drawLine(
                    color = ChartGridSubtle,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Right scale price label
                val priceLabel = formatPrice(gridPrice)
                val textLayoutResult = textMeasurer.measure(
                    text = priceLabel,
                    style = TextStyle(
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(chartWidth + 12f, y - textLayoutResult.size.height / 2f)
                )
            }

            // Right scale border
            drawLine(
                color = ChartGrid,
                start = Offset(chartWidth, 0f),
                end = Offset(chartWidth, canvasHeight),
                strokeWidth = 1.5f
            )

            // 2. Candlestick Bars
            val candleCount = max(1, candles.size)
            val candleWidth = max(6f, (chartWidth / candleCount) * 0.65f)
            val candleGap = (chartWidth / candleCount) * 0.35f
            val candleStep = chartWidth / candleCount

            candles.forEachIndexed { index, candle ->
                val xCenter = 20f + index * candleStep + candleWidth / 2f
                val openY = BouncePhysics.priceToY(candle.open, minPrice, maxPrice, canvasHeight, padding)
                val closeY = BouncePhysics.priceToY(candle.close, minPrice, maxPrice, canvasHeight, padding)
                val highY = BouncePhysics.priceToY(candle.high, minPrice, maxPrice, canvasHeight, padding)
                val lowY = BouncePhysics.priceToY(candle.low, minPrice, maxPrice, canvasHeight, padding)

                val isGreen = candle.close >= candle.open
                val candleColor = if (isGreen) CandleBullish else CandleBearish

                // Draw Wick
                drawLine(
                    color = candleColor,
                    start = Offset(xCenter, highY),
                    end = Offset(xCenter, lowY),
                    strokeWidth = 2.dp.toPx()
                )

                // Draw Body
                val bodyTopY = minOf(openY, closeY)
                val bodyBottomY = maxOf(openY, closeY)
                val bodyHeight = max(3f, bodyBottomY - bodyTopY)

                drawRoundRect(
                    color = candleColor,
                    topLeft = Offset(xCenter - candleWidth / 2f, bodyTopY),
                    size = Size(candleWidth, bodyHeight),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }

            // 3. Solution Overlay (if toggled by hint or graded)
            if (showSolutionOverlay) {
                val sol = level.solution
                val solTopY = BouncePhysics.priceToY(sol.obHigh, minPrice, maxPrice, canvasHeight, padding)
                val solBottomY = BouncePhysics.priceToY(sol.obLow, minPrice, maxPrice, canvasHeight, padding)
                val solStartX = 20f + sol.obCandleStartIndex * candleStep - candleWidth / 2f
                val solEndX = chartWidth // Extend to right edge like TradingView zone

                // Fill target zone
                drawRect(
                    color = Color(0x3300E676),
                    topLeft = Offset(solStartX, minOf(solTopY, solBottomY)),
                    size = Size(solEndX - solStartX, abs(solBottomY - solTopY)),
                    style = Fill
                )
                // Border
                drawRect(
                    color = Color(0xFF00E676),
                    topLeft = Offset(solStartX, minOf(solTopY, solBottomY)),
                    size = Size(solEndX - solStartX, abs(solBottomY - solTopY)),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Label
                val solLabel = textMeasurer.measure(
                    text = "TARGET OB: ${formatPrice(sol.obLow)} - ${formatPrice(sol.obHigh)}",
                    style = TextStyle(
                        color = Color(0xFF00E676),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
                drawText(
                    textLayoutResult = solLabel,
                    topLeft = Offset(solStartX + 8f, minOf(solTopY, solBottomY) + 6f)
                )
            }

            // 4. User Drawn Order Block
            if (userBox != null) {
                val userTopY = BouncePhysics.priceToY(userBox.high, minPrice, maxPrice, canvasHeight, padding)
                val userBottomY = BouncePhysics.priceToY(userBox.low, minPrice, maxPrice, canvasHeight, padding)
                val userStartX = 20f + userBox.startIndex * candleStep - candleWidth / 2f
                val userEndX = 20f + userBox.endIndex * candleStep + candleWidth * 1.5f

                // Fill
                drawRoundRect(
                    color = OrderBlockFill,
                    topLeft = Offset(userStartX, minOf(userTopY, userBottomY)),
                    size = Size(max(30f, userEndX - userStartX), abs(userBottomY - userTopY)),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Fill
                )
                // Border
                drawRoundRect(
                    color = OrderBlockStroke,
                    topLeft = Offset(userStartX, minOf(userTopY, userBottomY)),
                    size = Size(max(30f, userEndX - userStartX), abs(userBottomY - userTopY)),
                    cornerRadius = CornerRadius(4f, 4f),
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                    )
                )

                // Box Range Tag
                val tagText = textMeasurer.measure(
                    text = "MY OB: ${formatPrice(userBox.low)} - ${formatPrice(userBox.high)}",
                    style = TextStyle(
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
                // Tag badge background
                drawRoundRect(
                    color = Color(0xCC00E676),
                    topLeft = Offset(userStartX + 4f, minOf(userTopY, userBottomY) - 22f),
                    size = Size(tagText.size.width + 12f, 20f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawText(
                    textLayoutResult = tagText,
                    topLeft = Offset(userStartX + 10f, minOf(userTopY, userBottomY) - 20f)
                )
            }

            // 5. Full Trade Setup Lines (Entry, SL, TP)
            drawTradeLevels(
                tradeSetup = tradeSetup,
                chartWidth = chartWidth,
                minPrice = minPrice,
                maxPrice = maxPrice,
                canvasHeight = canvasHeight,
                padding = padding,
                textMeasurer = textMeasurer
            )

            // 6. Dynamic Bounce Springboard Effect
            if (ballState.isInOrderBlockZone) {
                // Glow line on the OB springboard floor
                val obFloorY = userBox?.let { BouncePhysics.priceToY(it.low, minPrice, maxPrice, canvasHeight, padding) }
                    ?: level.solution.let { BouncePhysics.priceToY(it.obLow, minPrice, maxPrice, canvasHeight, padding) }
                drawLine(
                    color = Color(0xFF00E676),
                    start = Offset(0f, obFloorY),
                    end = Offset(chartWidth, obFloorY),
                    strokeWidth = 4f
                )
            }

            // 7. Interactive Ball ("Price is the Ball")
            drawPriceBall(ballState)
        }
    }
}

private fun DrawScope.drawTradeLevels(
    tradeSetup: TradeSetup,
    chartWidth: Float,
    minPrice: Double,
    maxPrice: Double,
    canvasHeight: Float,
    padding: Float,
    textMeasurer: TextMeasurer
) {
    // Take Profit (TP) Line
    tradeSetup.takeProfitPrice?.let { tp ->
        val y = BouncePhysics.priceToY(tp, minPrice, maxPrice, canvasHeight, padding)
        drawLine(
            color = TakeProfitColor,
            start = Offset(0f, y),
            end = Offset(chartWidth, y),
            strokeWidth = 2.5f
        )
        // Badge
        val label = textMeasurer.measure(
            text = "TP: ${formatPrice(tp)}",
            style = TextStyle(color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        )
        drawRoundRect(
            color = TakeProfitColor,
            topLeft = Offset(chartWidth - label.size.width - 16f, y - 13f),
            size = Size(label.size.width + 14f, 26f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawText(textLayoutResult = label, topLeft = Offset(chartWidth - label.size.width - 9f, y - 8f))
    }

    // Entry Line
    tradeSetup.entryPrice?.let { entry ->
        val y = BouncePhysics.priceToY(entry, minPrice, maxPrice, canvasHeight, padding)
        drawLine(
            color = EntryLineColor,
            start = Offset(0f, y),
            end = Offset(chartWidth, y),
            strokeWidth = 2.5f
        )
        // Badge
        val label = textMeasurer.measure(
            text = "ENTRY: ${formatPrice(entry)}",
            style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        )
        drawRoundRect(
            color = EntryLineColor,
            topLeft = Offset(chartWidth - label.size.width - 16f, y - 13f),
            size = Size(label.size.width + 14f, 26f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawText(textLayoutResult = label, topLeft = Offset(chartWidth - label.size.width - 9f, y - 8f))
    }

    // Stop Loss (SL) Line
    tradeSetup.stopLossPrice?.let { sl ->
        val y = BouncePhysics.priceToY(sl, minPrice, maxPrice, canvasHeight, padding)
        drawLine(
            color = StopLossColor,
            start = Offset(0f, y),
            end = Offset(chartWidth, y),
            strokeWidth = 2.5f
        )
        // Badge
        val label = textMeasurer.measure(
            text = "SL: ${formatPrice(sl)}",
            style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        )
        drawRoundRect(
            color = StopLossColor,
            topLeft = Offset(chartWidth - label.size.width - 16f, y - 13f),
            size = Size(label.size.width + 14f, 26f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawText(textLayoutResult = label, topLeft = Offset(chartWidth - label.size.width - 9f, y - 8f))
    }
}

private fun DrawScope.drawPriceBall(ballState: BallState) {
    if (ballState.position == Offset.Zero) return

    val pos = ballState.position

    // Draw particle trail
    ballState.trail.forEachIndexed { i, trailPos ->
        val alpha = (1f - (i.toFloat() / ballState.trail.size.toFloat())) * 0.45f
        val radius = 9f * (1f - (i.toFloat() / ballState.trail.size.toFloat()) * 0.5f)
        drawCircle(
            color = Color(0xFFFFD600).copy(alpha = alpha),
            radius = radius,
            center = trailPos
        )
    }

    // Outer Glow aura
    val glowColor = if (ballState.isInOrderBlockZone) Color(0xFF00E676) else Color(0xFFFFD600)
    drawCircle(
        color = glowColor.copy(alpha = 0.35f),
        radius = 20f + (ballState.bounceImpulse * 0.4f),
        center = pos
    )

    // Inner Neon Ball Body
    drawCircle(
        color = glowColor,
        radius = 11f,
        center = pos
    )

    // Specular Highlight
    drawCircle(
        color = Color.White.copy(alpha = 0.85f),
        radius = 4f,
        center = Offset(pos.x - 3f, pos.y - 3f)
    )
}

private fun formatPrice(price: Double): String {
    return if (price >= 1000.0) {
        String.format("%,.1f", price)
    } else if (price >= 100.0) {
        String.format("%.2f", price)
    } else {
        String.format("%.4f", price)
    }
}
