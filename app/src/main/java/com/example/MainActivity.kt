package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.FXBounceViewModel
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.GlossaryScreen
import com.example.ui.screens.RoadmapScreen
import com.example.ui.screens.SandboxScreen
import com.example.ui.theme.ChartBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: FXBounceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ChartBackground
                ) {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    Crossfade(
                        targetState = uiState.currentScreen,
                        label = "screenTransition"
                    ) { screen ->
                        when (screen) {
                            AppScreen.ROADMAP -> {
                                RoadmapScreen(
                                    userProfile = uiState.userProfile,
                                    onSelectLevel = { levelId -> viewModel.selectLevel(levelId) },
                                    onNavigate = { target -> viewModel.navigateTo(target) },
                                    onRefillLives = { viewModel.refillLives() }
                                )
                            }
                            AppScreen.GAMEPLAY -> {
                                GamePlayScreen(
                                    uiState = uiState,
                                    onNavigateBack = { viewModel.navigateTo(AppScreen.ROADMAP) },
                                    onTogglePlay = { viewModel.togglePlayPause() },
                                    onCycleSpeed = { viewModel.cycleSpeed() },
                                    onResetBall = { viewModel.resetBall() },
                                    onShowHint = { /* hint toggled in HUD */ },
                                    onUserBoxChange = { box -> viewModel.updateUserBox(box) },
                                    onTradeSetupChange = { setup -> viewModel.updateTradeSetup(setup) },
                                    onToolModeChange = { mode -> viewModel.setChartToolMode(mode) },
                                    onClearMarkings = { viewModel.clearMarkings() },
                                    onSubmitValidation = { viewModel.submitValidation() },
                                    onTriggerRetestEntry = { viewModel.triggerRetestEntry() },
                                    onNextLevel = { viewModel.nextLevel() },
                                    onRetryLevel = { viewModel.retryLevel() },
                                    onToggleShowSolution = { viewModel.toggleShowSolution() }
                                )
                            }
                            AppScreen.SANDBOX -> {
                                SandboxScreen(
                                    onNavigate = { target -> viewModel.navigateTo(target) },
                                    ballState = uiState.ballState
                                )
                            }
                            AppScreen.GLOSSARY -> {
                                GlossaryScreen(
                                    onNavigate = { target -> viewModel.navigateTo(target) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
