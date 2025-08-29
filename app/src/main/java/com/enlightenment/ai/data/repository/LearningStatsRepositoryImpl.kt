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
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据层 - LearningStatsRepository实现
 * 
 * 架构职责：
 * 实现LearningStatsRepository接口，协调远程服务和本地存储。
 * 负责数据的获取、转换、缓存和错误处理。
 * 
 * 核心功能：
 * 1. 数据获取和存储
 * 2. 缓存管理
 * 3. 错误处理
 * 4. 数据转换
 * 
 * 技术特点：
 * - 协程实现异步操作
 * - 统一错误处理
 * - 数据映射转换
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@Singleton
/**
 * LearningStatsRepositoryImpl - LearningStats仓库实现
 * 
 * 仓库模式实现类，协调本地和远程数据源
 * 
 * 核心职责：
 * - 统一数据访问接口
 * - 实现缓存策略
 * - 处理数据同步
 * - 错误处理和降级
 * 
 * 数据策略：
 * - 优先使用本地缓存
 * - 异步更新远程数据
 * - 智能数据预加载
 * - 离线模式支持
 * 
 * @since 1.0.0
 */
class LearningStatsRepositoryImpl @Inject constructor(  // 依赖注入
    private val dataStore: DataStore<Preferences>
) : LearningStatsRepository {
    
    companion object {
        private val STORIES_COMPLETED = intPreferencesKey("stories_completed")
        private val LEARNING_DAYS = intPreferencesKey("learning_days")
        private val CURRENT_STREAK = intPreferencesKey("current_streak")
        private val TOTAL_MINUTES = intPreferencesKey("total_minutes")
        private val LAST_LEARNING_DATE = longPreferencesKey("last_learning_date")
        private val LEARNING_DATES = stringSetPreferencesKey("learning_dates")
        private val TODAY_MINUTES = intPreferencesKey("today_minutes")
        private val TODAY_STORIES = intPreferencesKey("today_stories")
        private val TODAY_DATE = stringPreferencesKey("today_date")
    }
    
    override suspend fun incrementStoriesCompleted() {
        dataStore.edit { preferences ->
            val current = preferences[STORIES_COMPLETED] ?: 0
            preferences[STORIES_COMPLETED] = current + 1
            
            // Update today's count
            val today = LocalDate.now().toString()
            if (preferences[TODAY_DATE] != today) {
                preferences[TODAY_DATE] = today
                preferences[TODAY_STORIES] = 1
                preferences[TODAY_MINUTES] = 0
            } else {
                val todayStories = preferences[TODAY_STORIES] ?: 0
                preferences[TODAY_STORIES] = todayStories + 1
            }
        }
    }
    
    override suspend fun recordLearningSession(durationMinutes: Int) {
        dataStore.edit { preferences ->
            // Update total minutes
            val currentMinutes = preferences[TOTAL_MINUTES] ?: 0
            preferences[TOTAL_MINUTES] = currentMinutes + durationMinutes
            
            // Update today's minutes
            val today = LocalDate.now().toString()
            if (preferences[TODAY_DATE] != today) {
                preferences[TODAY_DATE] = today
                preferences[TODAY_MINUTES] = durationMinutes
                preferences[TODAY_STORIES] = 0
            } else {
                val todayMinutes = preferences[TODAY_MINUTES] ?: 0
                preferences[TODAY_MINUTES] = todayMinutes + durationMinutes
            }
            
            // Update learning dates
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
            
            val daysSinceLastLearning = ChronoUnit.DAYS.between(
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
        }.catch {   // 捕获并处理异常
            emit(emptyPreferences())
        }.collect { return@collect it[STORIES_COMPLETED] ?: 0 }  // 收集数据流更新
    }
    
    override suspend fun getTotalLearningDays(): Int {
        return dataStore.data.map { preferences ->
            preferences[LEARNING_DAYS] ?: 0
        }.catch {   // 捕获并处理异常
            emit(emptyPreferences())
        }.collect { return@collect it[LEARNING_DAYS] ?: 0 }  // 收集数据流更新
    }
    
    override suspend fun getLastLearningDate(): Long? {
        return dataStore.data.map { preferences ->
            preferences[LAST_LEARNING_DATE]
        }.catch {   // 捕获并处理异常
            emit(emptyPreferences())
        }.collect { return@collect it[LAST_LEARNING_DATE] }  // 收集数据流更新
    }
    
    override fun observeLearningStats(): Flow<LearningStats> {
        return dataStore.data
            .catch { exception ->  // 捕获并处理异常
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
    
    override suspend fun getTodayMinutes(): Int {
        return dataStore.data.map { preferences ->
            val today = LocalDate.now().toString()
            if (preferences[TODAY_DATE] == today) {
                preferences[TODAY_MINUTES] ?: 0
            } else {
                0
            }
        }.catch {   // 捕获并处理异常
            emit(0)
        }.collect { return@collect it }  // 收集数据流更新
    }
    
    override suspend fun getTodayStories(): Int {
        return dataStore.data.map { preferences ->
            val today = LocalDate.now().toString()
            if (preferences[TODAY_DATE] == today) {
                preferences[TODAY_STORIES] ?: 0
            } else {
                0
            }
        }.catch {   // 捕获并处理异常
            emit(0)
        }.collect { return@collect it }  // 收集数据流更新
    }
}