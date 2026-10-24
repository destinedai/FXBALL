package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LevelRepository
import com.example.data.model.Level
import com.example.data.model.LevelStage
import com.example.data.model.TradeSetup
import com.example.data.model.UserBox
import com.example.data.model.UserProfile
import com.example.data.model.ValidationResult
import com.example.engine.BallState
import com.example.engine.BouncePhysics
import com.example.engine.ValidationEngine
import com.example.ui.components.ChartToolMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen {
    ROADMAP,
    GAMEPLAY,
    SANDBOX,
    GLOSSARY
}

data class GameUiState(
    val currentScreen: AppScreen = AppScreen.ROADMAP,
    val selectedLevelId: Int = 1,
    val currentLevel: Level = LevelRepository.getLevelById(1)!!,
    val userProfile: UserProfile = UserProfile(),
    val userBox: UserBox? = null,
    val tradeSetup: TradeSetup = TradeSetup(),
    val chartToolMode: ChartToolMode = ChartToolMode.MARK_OB,
    val isBallPlaying: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val ballProgress: Float = 0f,
    val ballState: BallState = BallState(),
    val showSolutionOverlay: Boolean = false,
    val validationResult: ValidationResult? = null,
    val showConfetti: Boolean = false,
    // Sandbox specific
    val sandboxSymbol: String = "BTC/USDT",
    val sandboxLevel: Level = LevelRepository.getLevelById(1)!!
)

class FXBounceViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("fx_bounce_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var ballAnimationJob: Job? = null

    init {
        loadUserProfile()
        startBallAnimation()
    }

    private fun loadUserProfile() {
        val totalXp = prefs.getInt("total_xp", 100)
        val lives = prefs.getInt("lives", 3)
        val streak = prefs.getInt("streak", 3)
        val completedSet = prefs.getStringSet("completed_levels", setOf("1"))?.mapNotNull { it.toIntOrNull() }?.toSet() ?: setOf(1)
        val starsMap = mutableMapOf<Int, Int>()
        completedSet.forEach { id ->
            starsMap[id] = prefs.getInt("level_stars_$id", 3)
        }

        _uiState.update {
            it.copy(
                userProfile = UserProfile(
                    lives = lives,
                    totalXp = totalXp,
                    streakDays = streak,
                    completedLevels = completedSet,
                    levelStars = starsMap
                )
            )
        }
    }

    private fun saveUserProfile(profile: UserProfile) {
        prefs.edit()
            .putInt("total_xp", profile.totalXp)
            .putInt("lives", profile.lives)
            .putInt("streak", profile.streakDays)
            .putStringSet("completed_levels", profile.completedLevels.map { it.toString() }.toSet())
            .apply()
        profile.levelStars.forEach { (lvl, stars) ->
            prefs.edit().putInt("level_stars_$lvl", stars).apply()
        }
    }

    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun selectLevel(levelId: Int) {
        val level = LevelRepository.getLevelById(levelId) ?: return
        val defaultTool = when (level.stage) {
            LevelStage.IDENTIFICATION -> ChartToolMode.MARK_OB
            LevelStage.BREAK_AND_RETEST -> ChartToolMode.VIEW_ONLY
            LevelStage.FULL_TRADE -> ChartToolMode.TRADE_LEVELS
            LevelStage.ADVANCED_SMC -> ChartToolMode.MARK_OB
        }

        _uiState.update {
            it.copy(
                selectedLevelId = levelId,
                currentLevel = level,
                currentScreen = AppScreen.GAMEPLAY,
                userBox = null,
                tradeSetup = TradeSetup(
                    entryPrice = level.solution.recommendedEntry,
                    stopLossPrice = level.solution.recommendedSl,
                    takeProfitPrice = level.solution.recommendedTp
                ),
                chartToolMode = defaultTool,
                ballProgress = 0f,
                showSolutionOverlay = false,
                validationResult = null,
                showConfetti = false,
                isBallPlaying = true
            )
        }
        startBallAnimation()
    }

    fun updateUserBox(box: UserBox) {
        _uiState.update { it.copy(userBox = box) }
    }

    fun updateTradeSetup(setup: TradeSetup) {
        _uiState.update { it.copy(tradeSetup = setup) }
    }

    fun setChartToolMode(mode: ChartToolMode) {
        _uiState.update { it.copy(chartToolMode = mode) }
    }

    fun clearMarkings() {
        _uiState.update { it.copy(userBox = null) }
    }

    fun togglePlayPause() {
        val playing = !_uiState.value.isBallPlaying
        _uiState.update { it.copy(isBallPlaying = playing) }
        if (playing) {
            startBallAnimation()
        } else {
            ballAnimationJob?.cancel()
        }
    }

    fun cycleSpeed() {
        val nextSpeed = when (_uiState.value.playbackSpeed) {
            0.5f -> 1.0f
            1.0f -> 2.0f
            else -> 0.5f
        }
        _uiState.update { it.copy(playbackSpeed = nextSpeed) }
    }

    fun resetBall() {
        _uiState.update { it.copy(ballProgress = 0f) }
    }

    fun toggleShowSolution() {
        _uiState.update { it.copy(showSolutionOverlay = !it.showSolutionOverlay) }
    }

    /**
     * Ball physics runner coroutine
     */
    private fun startBallAnimation() {
        ballAnimationJob?.cancel()
        ballAnimationJob = viewModelScope.launch {
            while (isActive) {
                if (_uiState.value.isBallPlaying) {
                    val level = _uiState.value.currentLevel
                    val maxProgress = (level.candles.size - 1).toFloat()
                    val speed = _uiState.value.playbackSpeed
                    val delta = 0.05f * speed

                    val newProgress = if (_uiState.value.ballProgress + delta > maxProgress) {
                        0f // Loop smoothly
                    } else {
                        _uiState.value.ballProgress + delta
                    }

                    // Compute ball positions
                    val chartWidth = 900f
                    val chartHeight = 700f
                    val padding = 45f
                    val candles = level.candles
                    val minP = candles.minOfOrNull { it.low } ?: 0.0
                    val maxP = candles.maxOfOrNull { it.high } ?: 100.0
                    val candleStep = chartWidth / candles.size

                    val xs = candles.indices.map { 20f + it * candleStep }
                    val ys = candles.map { BouncePhysics.priceToY(it.close, minP, maxP, chartHeight, padding) }
                    val obTop = BouncePhysics.priceToY(level.solution.obHigh, minP, maxP, chartHeight, padding)
                    val obBot = BouncePhysics.priceToY(level.solution.obLow, minP, maxP, chartHeight, padding)

                    val updatedBall = BouncePhysics.updateBall(
                        progress = newProgress,
                        candleYPositions = ys,
                        candleXPositions = xs,
                        obYTop = obTop,
                        obYBottom = obBot,
                        currentTrail = _uiState.value.ballState.trail
                    )

                    _uiState.update {
                        it.copy(
                            ballProgress = newProgress,
                            ballState = updatedBall
                        )
                    }
                }
                delay(30L)
            }
        }
    }

    /**
     * Submit Order Block or Full Trade validation
     */
    fun submitValidation() {
        val state = _uiState.value
        val level = state.currentLevel

        val result = when (level.stage) {
            LevelStage.IDENTIFICATION, LevelStage.ADVANCED_SMC -> {
                ValidationEngine.validateOrderBlock(state.userBox, level)
            }
            LevelStage.FULL_TRADE -> {
                ValidationEngine.validateFullTrade(state.tradeSetup, level)
            }
            LevelStage.BREAK_AND_RETEST -> {
                ValidationEngine.validateBreakAndRetest(state.ballProgress.toInt(), level)
            }
        }

        handleValidationResult(result)
    }

    /**
     * Triggered in Stage 2 (Break & Retest) when player hits "ENTER TRADE"
     */
    fun triggerRetestEntry() {
        val state = _uiState.value
        val currentCandleIndex = state.ballProgress.toInt()
        val result = ValidationEngine.validateBreakAndRetest(currentCandleIndex, state.currentLevel)
        handleValidationResult(result)
    }

    private fun handleValidationResult(result: ValidationResult) {
        val currentProfile = _uiState.value.userProfile
        vibrate(if (result.isCorrect) 120L else 300L)

        val updatedProfile = if (result.isCorrect) {
            val updatedCompleted = currentProfile.completedLevels + _uiState.value.selectedLevelId
            val updatedStars = currentProfile.levelStars.toMutableMap()
            val prevStars = updatedStars[_uiState.value.selectedLevelId] ?: 0
            if (result.stars > prevStars) {
                updatedStars[_uiState.value.selectedLevelId] = result.stars
            }
            currentProfile.copy(
                totalXp = currentProfile.totalXp + result.xpEarned,
                completedLevels = updatedCompleted,
                levelStars = updatedStars
            )
        } else {
            // Deduct life, min 1
            currentProfile.copy(
                lives = maxOf(1, currentProfile.lives - 1)
            )
        }

        saveUserProfile(updatedProfile)

        _uiState.update {
            it.copy(
                userProfile = updatedProfile,
                validationResult = result,
                showConfetti = result.isCorrect,
                showSolutionOverlay = !result.isCorrect
            )
        }
    }

    fun nextLevel() {
        val nextId = _uiState.value.selectedLevelId + 1
        if (LevelRepository.getLevelById(nextId) != null) {
            selectLevel(nextId)
        } else {
            navigateTo(AppScreen.ROADMAP)
        }
    }

    fun retryLevel() {
        _uiState.update {
            it.copy(
                validationResult = null,
                showSolutionOverlay = false,
                ballProgress = 0f,
                showConfetti = false
            )
        }
    }

    fun refillLives() {
        val updated = _uiState.value.userProfile.copy(lives = 3)
        saveUserProfile(updated)
        _uiState.update { it.copy(userProfile = updated) }
    }

    fun setSandboxSymbol(symbol: String) {
        val level = LevelRepository.getAllLevels().find { it.symbol == symbol } ?: LevelRepository.getLevelById(1)!!
        _uiState.update {
            it.copy(
                sandboxSymbol = symbol,
                sandboxLevel = level
            )
        }
    }

    private fun vibrate(durationMillis: Long) {
        try {
            val ctx = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMillis, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMillis)
            }
        } catch (_: Exception) {
            // In case running in non-haptic environment
        }
    }
}
