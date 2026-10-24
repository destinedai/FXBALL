package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LevelRepository
import com.example.ui.AppScreen
import com.example.ui.GameUiState
import com.example.ui.components.CandlestickChartCanvas
import com.example.ui.components.ConfettiEffect
import com.example.ui.components.LevelHUD
import com.example.ui.components.TradeDrawingToolbar
import com.example.ui.components.ValidationResultModal
import com.example.ui.theme.CandleBullish
import com.example.ui.theme.ChartBackground
import com.example.ui.theme.ChartGrid
import com.example.ui.theme.ChartSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GamePlayScreen(
    uiState: GameUiState,
    onNavigateBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onCycleSpeed: () -> Unit,
    onResetBall: () -> Unit,
    onShowHint: () -> Unit,
    onUserBoxChange: (com.example.data.model.UserBox) -> Unit,
    onTradeSetupChange: (com.example.data.model.TradeSetup) -> Unit,
    onToolModeChange: (com.example.ui.components.ChartToolMode) -> Unit,
    onClearMarkings: () -> Unit,
    onSubmitValidation: () -> Unit,
    onTriggerRetestEntry: () -> Unit,
    onNextLevel: () -> Unit,
    onRetryLevel: () -> Unit,
    onToggleShowSolution: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNavigateBack()
    }

    val level = uiState.currentLevel
    val hasNext = LevelRepository.getLevelById(level.id + 1) != null

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChartBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top HUD
                LevelHUD(
                    level = level,
                    userProfile = uiState.userProfile,
                    isPlaying = uiState.isBallPlaying,
                    onTogglePlay = onTogglePlay,
                    playbackSpeed = uiState.playbackSpeed,
                    onCycleSpeed = onCycleSpeed,
                    onResetBall = onResetBall,
                    onShowHint = onShowHint,
                    onExitLevel = onNavigateBack
                )

                // Win Condition & Instructions Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF131A26))
                        .border(1.dp, Color(0xFF24334C), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = level.winCondition,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Candlestick Chart with Ball Physics Engine
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    CandlestickChartCanvas(
                        level = level,
                        userBox = uiState.userBox,
                        onUserBoxChange = onUserBoxChange,
                        tradeSetup = uiState.tradeSetup,
                        onTradeSetupChange = onTradeSetupChange,
                        ballState = uiState.ballState,
                        toolMode = uiState.chartToolMode,
                        showSolutionOverlay = uiState.showSolutionOverlay
                    )
                }

                // Bottom Action & Tools Toolbar
                TradeDrawingToolbar(
                    level = level,
                    toolMode = uiState.chartToolMode,
                    onToolModeChange = onToolModeChange,
                    userBox = uiState.userBox,
                    tradeSetup = uiState.tradeSetup,
                    onClearMarkings = onClearMarkings,
                    onSubmitValidation = onSubmitValidation,
                    onTriggerRetestEntry = onTriggerRetestEntry
                )
            }

            // Confetti Overlay on Win
            if (uiState.showConfetti) {
                ConfettiEffect()
            }

            // Validation Result Modal Dialog
            uiState.validationResult?.let { result ->
                ValidationResultModal(
                    result = result,
                    onNextLevel = onNextLevel,
                    onRetry = onRetryLevel,
                    onToggleShowSolution = onToggleShowSolution,
                    showingSolution = uiState.showSolutionOverlay,
                    hasNextLevel = hasNext
                )
            }
        }
    }
}
