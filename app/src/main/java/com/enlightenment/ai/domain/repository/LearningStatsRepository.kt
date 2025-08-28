package com.enlightenment.ai.domain.repository

import kotlinx.coroutines.flow.Flow

interface LearningStatsRepository {
    suspend fun incrementStoriesCompleted()
    suspend fun recordLearningSession(durationMinutes: Int)
    suspend fun getStoriesCompleted(): Int
    suspend fun getTotalLearningDays(): Int
    suspend fun getLastLearningDate(): Long?
    fun observeLearningStats(): Flow<LearningStats>
}

data class LearningStats(
    val totalStoriesCompleted: Int = 0,
    val totalLearningDays: Int = 0,
    val currentStreak: Int = 0,
    val totalLearningMinutes: Int = 0,
    val lastLearningDate: Long? = null
)