package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LevelRepository
import com.example.data.model.Level
import com.example.data.model.TradeSetup
import com.example.data.model.UserBox
import com.example.engine.BallState
import com.example.engine.ValidationEngine
import com.example.ui.AppScreen
import com.example.ui.components.CandlestickChartCanvas
import com.example.ui.components.ChartToolMode
import com.example.ui.theme.CandleBullish
import com.example.ui.theme.ChartBackground
import com.example.ui.theme.ChartGrid
import com.example.ui.theme.ChartSurface
import com.example.ui.theme.ChartSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SandboxScreen(
    onNavigate: (AppScreen) -> Unit,
    ballState: BallState,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigate(AppScreen.ROADMAP)
    }

    val pairs = listOf("BTC/USDT", "EUR/USD", "GBP/USD", "SOL/USDT", "XAU/USD")
    var selectedPair by remember { mutableStateOf("BTC/USDT") }

    val currentLevel: Level = remember(selectedPair) {
        LevelRepository.getAllLevels().find { it.symbol == selectedPair } ?: LevelRepository.getLevelById(1)!!
    }

    var userBox by remember(selectedPair) { mutableStateOf<UserBox?>(null) }
    var tradeSetup by remember(selectedPair) {
        mutableStateOf(
            TradeSetup(
                entryPrice = currentLevel.solution.recommendedEntry,
                stopLossPrice = currentLevel.solution.recommendedSl,
                takeProfitPrice = currentLevel.solution.recommendedTp
            )
        )
    }
    var toolMode by remember { mutableStateOf(ChartToolMode.MARK_OB) }
    var showSolution by remember { mutableStateOf(false) }
    var gradeText by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChartBackground,
        bottomBar = {
            RoadmapBottomNav(
                currentScreen = AppScreen.SANDBOX,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChartSurface)
                    .border(1.dp, ChartGrid)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = CandleBullish,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "PRACTICE SANDBOX",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Free-hand practice on real market setups",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Pair Selector Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChartSurfaceElevated)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pairs) { pair ->
                    val isSelected = pair == selectedPair
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CandleBullish else ChartSurface)
                            .border(1.dp, if (isSelected) CandleBullish else ChartGrid, RoundedCornerShape(8.dp))
                            .clickable {
                                selectedPair = pair
                                gradeText = null
                                showSolution = false
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("pair_chip_$pair")
                    ) {
                        Text(
                            text = pair,
                            color = if (isSelected) Color.Black else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Grade Feedback Banner
            gradeText?.let { feedback ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF00381B))
                        .border(1.dp, CandleBullish, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = feedback,
                        color = CandleBullish,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Candlestick Chart Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                CandlestickChartCanvas(
                    level = currentLevel,
                    userBox = userBox,
                    onUserBoxChange = { userBox = it },
                    tradeSetup = tradeSetup,
                    onTradeSetupChange = { tradeSetup = it },
                    ballState = ballState,
                    toolMode = toolMode,
                    showSolutionOverlay = showSolution
                )
            }

            // Sandbox Action Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChartSurface)
                    .border(1.dp, ChartGrid)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle Tool Mode
                OutlinedButton(
                    onClick = {
                        toolMode = if (toolMode == ChartToolMode.MARK_OB) ChartToolMode.TRADE_LEVELS else ChartToolMode.MARK_OB
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("sandbox_tool_toggle")
                ) {
                    Icon(
                        imageVector = Icons.Default.CropSquare,
                        contentDescription = null,
                        tint = Color(0xFF64B5F6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (toolMode == ChartToolMode.MARK_OB) "Box Tool" else "Trade Tool",
                        color = Color(0xFF64B5F6),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Show / Hide Target
                OutlinedButton(
                    onClick = { showSolution = !showSolution },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("sandbox_show_target")
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showSolution) "Hide Target" else "Show Target",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Auto Grade Button
                Button(
                    onClick = {
                        val res = if (toolMode == ChartToolMode.MARK_OB) {
                            ValidationEngine.validateOrderBlock(userBox, currentLevel)
                        } else {
                            ValidationEngine.validateFullTrade(tradeSetup, currentLevel)
                        }
                        gradeText = "${res.feedback}: ${res.score}% Match! ${res.detailedReason}"
                        showSolution = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CandleBullish, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(46.dp)
                        .testTag("sandbox_grade_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TEST MY EYE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
