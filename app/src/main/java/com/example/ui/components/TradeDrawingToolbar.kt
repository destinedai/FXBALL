package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Level
import com.example.data.model.LevelStage
import com.example.data.model.TradeSetup
import com.example.data.model.UserBox
import com.example.ui.theme.CandleBullish
import com.example.ui.theme.ChartGrid
import com.example.ui.theme.ChartSurface
import com.example.ui.theme.ChartSurfaceElevated
import com.example.ui.theme.EntryLineColor
import com.example.ui.theme.StopLossColor
import com.example.ui.theme.TakeProfitColor
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TradeDrawingToolbar(
    level: Level,
    toolMode: ChartToolMode,
    onToolModeChange: (ChartToolMode) -> Unit,
    userBox: UserBox?,
    tradeSetup: TradeSetup,
    onClearMarkings: () -> Unit,
    onSubmitValidation: () -> Unit,
    onTriggerRetestEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(ChartSurface)
            .border(1.dp, ChartGrid, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(14.dp)
    ) {
        // Stage 1 & 4 Tools
        if (level.stage == LevelStage.IDENTIFICATION || level.stage == LevelStage.ADVANCED_SMC) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tool Toggle: Mark OB
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (toolMode == ChartToolMode.MARK_OB) Color(0xFF00381B) else ChartSurfaceElevated)
                        .border(
                            1.dp,
                            if (toolMode == ChartToolMode.MARK_OB) CandleBullish else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onToolModeChange(ChartToolMode.MARK_OB) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("tool_mark_ob")
                ) {
                    Icon(
                        imageVector = Icons.Default.CropSquare,
                        contentDescription = "Mark OB",
                        tint = if (toolMode == ChartToolMode.MARK_OB) CandleBullish else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DRAW ORDER BLOCK",
                        color = if (toolMode == ChartToolMode.MARK_OB) CandleBullish else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Clear
                if (userBox != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onClearMarkings() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("clear_markings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset Box",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Big Duolingo Check Button
            Button(
                onClick = onSubmitValidation,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CandleBullish,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_validation_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CHECK ORDER BLOCK",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        } else if (level.stage == LevelStage.BREAK_AND_RETEST) {
            // Stage 2: Break & Retest Button
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "⚠️ Watch out for fakeout traps! Tap only when the ball retests the OB.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Button(
                    onClick = onTriggerRetestEntry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2979FF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("enter_retest_trade_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENTER TRADE NOW (RETEST)",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        } else if (level.stage == LevelStage.FULL_TRADE) {
            // Stage 3: Full Trade Tools & Live R:R Ratio
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Risk:Reward Ratio badge
                    val rr = tradeSetup.riskRewardRatio
                    val rrText = if (rr != null) "R:R 1:${String.format("%.1f", rr)}" else "R:R: Set 3 Lines"
                    val rrColor = if (rr != null && rr >= 1.8) CandleBullish else Color(0xFFFFD600)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ChartSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = rrText,
                            color = rrColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Tool selector
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TradePill(
                            label = "ENTRY",
                            color = EntryLineColor,
                            isSet = tradeSetup.entryPrice != null
                        )
                        TradePill(
                            label = "SL",
                            color = StopLossColor,
                            isSet = tradeSetup.stopLossPrice != null
                        )
                        TradePill(
                            label = "TP",
                            color = TakeProfitColor,
                            isSet = tradeSetup.takeProfitPrice != null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Submit Trade
                Button(
                    onClick = onSubmitValidation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CandleBullish,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_trade_setup_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EVALUATE FULL TRADE",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TradePill(
    label: String,
    color: Color,
    isSet: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSet) color.copy(alpha = 0.2f) else ChartSurfaceElevated)
            .border(1.dp, if (isSet) color else Color.Transparent, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = if (isSet) color else TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
