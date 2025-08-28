package com.enlightenment.ai.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.enlightenment.ai.domain.repository.LearningStats
import com.enlightenment.ai.domain.repository.LearningStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LearningStatsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : LearningStatsRepository {
    
    companion object {
        private val STORIES_COMPLETED = intPreferencesKey("stories_completed")
        private val LEARNING_DAYS = intPreferencesKey("learning_days")
        private val CURRENT_STREAK = intPreferencesKey("current_streak")
        private val TOTAL_MINUTES = intPreferencesKey("total_minutes")
        private val LAST_LEARNING_DATE = longPreferencesKey("last_learning_date")
        private val LEARNING_DATES = stringSetPreferencesKey("learning_dates")
    }
    
    override suspend fun incrementStoriesCompleted() {
        dataStore.edit { preferences ->
            val current = preferences[STORIES_COMPLETED] ?: 0
            preferences[STORIES_COMPLETED] = current + 1
        }
    }
    
    override suspend fun recordLearningSession(durationMinutes: Int) {
        dataStore.edit { preferences ->
            // Update total minutes
            val currentMinutes = preferences[TOTAL_MINUTES] ?: 0
            preferences[TOTAL_MINUTES] = currentMinutes + durationMinutes
            
            // Update learning dates
            val today = LocalDate.now().toString()
            val learningDates = preferences[LEARNING_DATES]?.toMutableSet() ?: mutableSetOf()
            val wasNewDay = !learningDates.contains(today)
            learningDates.add(today)
            preferences[LEARNING_DATES] = learningDates
            
            // Update learning days count
            preferences[LEARNING_DAYS] = learningDates.size
            
            // Update last learning date
            preferences[LAST_LEARNING_DATE] = System.currentTimeMillis()
            
            // Update streak
            if (wasNewDay) {
                updateStreak(preferences)
            }
        }
    }
    
    private fun updateStreak(preferences: MutablePreferences) {
        val lastDate = preferences[LAST_LEARNING_DATE]
        val today = LocalDate.now()
        
        if (lastDate != null) {
            val lastLearningDate = LocalDate.ofInstant(
                java.time.Instant.ofEpochMilli(lastDate),
                ZoneId.systemDefault()
            )
            
            val daysSinceLastLearning = java.time.temporal.ChronoUnit.DAYS.between(
                lastLearningDate, today
            )
            
            preferences[CURRENT_STREAK] = when {
                daysSinceLastLearning == 0L -> preferences[CURRENT_STREAK] ?: 1
                daysSinceLastLearning == 1L -> (preferences[CURRENT_STREAK] ?: 0) + 1
                else -> 1 // Reset streak
            }
        } else {
            preferences[CURRENT_STREAK] = 1
        }
    }
    
    override suspend fun getStoriesCompleted(): Int {
        return dataStore.data.map { preferences ->
            preferences[STORIES_COMPLETED] ?: 0
        }.catch { 
            emit(emptyPreferences())
        }.collect { return@collect it[STORIES_COMPLETED] ?: 0 }
    }
    
    override suspend fun getTotalLearningDays(): Int {
        return dataStore.data.map { preferences ->
            preferences[LEARNING_DAYS] ?: 0
        }.catch { 
            emit(emptyPreferences())
        }.collect { return@collect it[LEARNING_DAYS] ?: 0 }
    }
    
    override suspend fun getLastLearningDate(): Long? {
        return dataStore.data.map { preferences ->
            preferences[LAST_LEARNING_DATE]
        }.catch { 
            emit(emptyPreferences())
        }.collect { return@collect it[LAST_LEARNING_DATE] }
    }
    
    override fun observeLearningStats(): Flow<LearningStats> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                LearningStats(
                    totalStoriesCompleted = preferences[STORIES_COMPLETED] ?: 0,
                    totalLearningDays = preferences[LEARNING_DAYS] ?: 0,
                    currentStreak = preferences[CURRENT_STREAK] ?: 0,
                    totalLearningMinutes = preferences[TOTAL_MINUTES] ?: 0,
                    lastLearningDate = preferences[LAST_LEARNING_DATE]
                )
            }
    }
}