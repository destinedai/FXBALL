package com.example.data.model

data class UserProfile(
    val lives: Int = 3,
    val maxLives: Int = 3,
    val totalXp: Int = 0,
    val streakDays: Int = 3,
    val completedLevels: Set<Int> = emptySet(),
    val levelScores: Map<Int, Int> = emptyMap(),
    val levelStars: Map<Int, Int> = emptyMap()
) {
    fun isLevelUnlocked(levelId: Int): Boolean {
        if (levelId == 1) return true
        return completedLevels.contains(levelId - 1)
    }

    val starsCount: Int
        get() = levelStars.values.sum()
}
