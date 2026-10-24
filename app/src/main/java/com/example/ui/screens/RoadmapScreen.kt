package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LevelRepository
import com.example.data.model.Level
import com.example.data.model.LevelStage
import com.example.data.model.UserProfile
import com.example.ui.AppScreen
import com.example.ui.theme.CandleBullish
import com.example.ui.theme.ChartBackground
import com.example.ui.theme.ChartGrid
import com.example.ui.theme.ChartSurface
import com.example.ui.theme.ChartSurfaceElevated
import com.example.ui.theme.DuolingoHeart
import com.example.ui.theme.DuolingoStar
import com.example.ui.theme.DuolingoStreak
import com.example.ui.theme.DuolingoXp
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.sin

@Composable
fun RoadmapScreen(
    userProfile: UserProfile,
    onSelectLevel: (Int) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onRefillLives: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allLevels = remember { LevelRepository.getAllLevels() }
    var showRefillDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "levelPulse"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ChartBackground,
        topBar = {
            RoadmapTopBar(
                userProfile = userProfile,
                onLivesClick = { showRefillDialog = true }
            )
        },
        bottomBar = {
            RoadmapBottomNav(
                currentScreen = AppScreen.ROADMAP,
                onNavigate = onNavigate
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                // Banner
                DailyStreakBanner(streakDays = userProfile.streakDays, totalStars = userProfile.starsCount)
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Group into 4 Worlds
            val stages = listOf(
                LevelStage.IDENTIFICATION,
                LevelStage.BREAK_AND_RETEST,
                LevelStage.FULL_TRADE,
                LevelStage.ADVANCED_SMC
            )

            stages.forEach { stage ->
                val stageLevels = allLevels.filter { it.stage == stage }

                item {
                    StageHeaderCard(stage = stage)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                itemsIndexed(stageLevels) { index, level ->
                    val isCompleted = userProfile.completedLevels.contains(level.id)
                    val isUnlocked = userProfile.isLevelUnlocked(level.id)
                    val stars = userProfile.levelStars[level.id] ?: 0

                    // Duolingo winding path horizontal offset
                    val horizontalWinding = (sin(level.id * 1.1) * 65.dp.value).dp

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.offset(x = horizontalWinding)) {
                            DuolingoLevelNode(
                                level = level,
                                isCompleted = isCompleted,
                                isUnlocked = isUnlocked,
                                stars = stars,
                                pulseScale = if (isUnlocked && !isCompleted) pulseScale else 1f,
                                onClick = {
                                    if (isUnlocked) {
                                        onSelectLevel(level.id)
                                    }
                                }
                            )
                        }
                    }
                }

                item {
                    // Milestone Trophy
                    MilestoneCard(stage = stage)
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showRefillDialog) {
        AlertDialog(
            onDismissRequest = { showRefillDialog = false },
            containerColor = ChartSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = DuolingoHeart,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Refill Hearts", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "You currently have ${userProfile.lives} lives. Refill back to 3 full hearts to keep practicing high-probability trade setups!",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRefillLives()
                        showRefillDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CandleBullish, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Refill Hearts (Free)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRefillDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun RoadmapTopBar(
    userProfile: UserProfile,
    onLivesClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChartSurface)
            .border(1.dp, ChartGrid)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF00E676), Color(0xFFFFD600)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "FX BOUNCE",
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        // Gamification Counters
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Streak
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChartSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Whatshot, null, tint = DuolingoStreak, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("${userProfile.streakDays}", color = DuolingoStreak, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // XP
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChartSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Bolt, null, tint = DuolingoXp, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("${userProfile.totalXp}", color = DuolingoXp, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Hearts (Tappable for refill)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChartSurfaceElevated)
                    .clickable { onLivesClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("refill_lives_trigger")
            ) {
                Icon(Icons.Default.Favorite, null, tint = DuolingoHeart, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("${userProfile.lives}", color = DuolingoHeart, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DailyStreakBanner(streakDays: Int, totalStars: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF1E2838), Color(0xFF141A24))
                )
            )
            .border(1.dp, Color(0xFF2B3A52), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = DuolingoXp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LEARN BY BOUNCING",
                        color = DuolingoXp,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$streakDays Day Trading Streak!",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Price is the ball. Master Order Blocks & Retests.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Star Counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF33290A))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Star, null, tint = DuolingoStar, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("$totalStars", color = DuolingoStar, fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun StageHeaderCard(stage: LevelStage) {
    val stageColor = when (stage) {
        LevelStage.IDENTIFICATION -> CandleBullish
        LevelStage.BREAK_AND_RETEST -> Color(0xFF2979FF)
        LevelStage.FULL_TRADE -> Color(0xFFFFD600)
        LevelStage.ADVANCED_SMC -> Color(0xFFE040FB)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ChartSurfaceElevated)
            .border(1.dp, stageColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(stageColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (stage) {
                        LevelStage.IDENTIFICATION -> Icons.Default.Timeline
                        LevelStage.BREAK_AND_RETEST -> Icons.Default.Whatshot
                        LevelStage.FULL_TRADE -> Icons.Default.Bolt
                        LevelStage.ADVANCED_SMC -> Icons.Default.EmojiEvents
                    },
                    contentDescription = null,
                    tint = stageColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = stage.label.uppercase(),
                    color = stageColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = stage.description,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun DuolingoLevelNode(
    level: Level,
    isCompleted: Boolean,
    isUnlocked: Boolean,
    stars: Int,
    pulseScale: Float,
    onClick: () -> Unit
) {
    val nodeColor = when {
        isCompleted -> CandleBullish
        isUnlocked -> Color(0xFF2979FF)
        else -> Color(0xFF2A2E39)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(enabled = isUnlocked) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .scale(pulseScale)
                .size(68.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(nodeColor, nodeColor.copy(alpha = 0.7f))
                    )
                )
                .border(
                    3.dp,
                    if (isUnlocked && !isCompleted) Color.White else nodeColor.copy(alpha = 0.8f),
                    CircleShape
                )
                .testTag("level_node_${level.id}"),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            } else if (isUnlocked) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Label
        Text(
            text = "Lvl ${level.id}",
            color = if (isUnlocked) TextPrimary else TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        // Stars beneath completed nodes
        if (isCompleted) {
            Row(horizontalArrangement = Arrangement.Center) {
                for (s in 1..3) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (s <= stars) DuolingoStar else Color(0xFF3E4352),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        } else {
            Text(
                text = level.symbol,
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun MilestoneCard(stage: LevelStage) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1B2230))
            .border(1.dp, ChartGrid, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = DuolingoXp,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "STAGE CHECKPOINT",
                        color = DuolingoXp,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Claim +150 Bonus XP upon stage completion",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RoadmapBottomNav(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = ChartSurface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.ROADMAP,
            onClick = { onNavigate(AppScreen.ROADMAP) },
            icon = { Icon(Icons.Default.Timeline, contentDescription = "Roadmap") },
            label = { Text("Levels", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CandleBullish,
                indicatorColor = CandleBullish,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_roadmap")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.SANDBOX,
            onClick = { onNavigate(AppScreen.SANDBOX) },
            icon = { Icon(Icons.Default.Science, contentDescription = "Sandbox") },
            label = { Text("Sandbox", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CandleBullish,
                indicatorColor = CandleBullish,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_sandbox")
        )

        NavigationBarItem(
            selected = currentScreen == AppScreen.GLOSSARY,
            onClick = { onNavigate(AppScreen.GLOSSARY) },
            icon = { Icon(Icons.Default.MenuBook, contentDescription = "SMC Bible") },
            label = { Text("SMC Bible", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = CandleBullish,
                indicatorColor = CandleBullish,
                unselectedIconColor = TextSecondary,
                unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_glossary")
        )
    }
}
