package com.enlightenment.ai.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.enlightenment.ai.domain.model.ChildProfile
import com.enlightenment.ai.domain.model.LearningLevel
import com.enlightenment.ai.domain.model.LearningStyle
import com.enlightenment.ai.domain.repository.ProfileRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据层 - ProfileRepository实现
 * 
 * 架构职责：
 * 实现ProfileRepository接口，协调远程服务和本地存储。
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
 * ProfileRepositoryImpl - Profile仓库实现
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
class ProfileRepositoryImpl @Inject constructor(  // 依赖注入
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) : ProfileRepository {
    
    companion object {
        private val PROFILE_KEY = stringPreferencesKey("child_profile")
    }
    
    override suspend fun getProfile(): ChildProfile? {
        return try {
            val preferences = dataStore.data.catch {   // 捕获并处理异常
                if (it is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }.map { preferences ->
                preferences[PROFILE_KEY]?.let { json ->
                    gson.fromJson(json, ChildProfile::class.java)
                }
            }.collect { profile ->  // 收集数据流更新
                return@collect profile
            }
            null
        } catch (e: Exception) {  // 捕获并处理异常
            null
        }
    }
    
    override suspend fun saveProfile(profile: ChildProfile) {
        dataStore.edit { preferences ->
            preferences[PROFILE_KEY] = gson.toJson(profile)
        }
    }
    
    override fun observeProfile(): Flow<ChildProfile?> {
        return dataStore.data
            .catch {   // 捕获并处理异常
                if (it is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }
            .map { preferences ->
                preferences[PROFILE_KEY]?.let { json ->
                    gson.fromJson(json, ChildProfile::class.java)
                }
            }
    }
    
    override suspend fun updateInterests(interests: List<String>) {
        val currentProfile = getProfile() ?: return
        saveProfile(currentProfile.copy(interests = interests))
    }
    
    override suspend fun updateLearningProgress(topic: String, score: Float) {
        val currentProfile = getProfile() ?: return
        val updatedTopics = currentProfile.recentTopics.toMutableList().apply {
            add(0, topic)
            if (size > 10) removeAt(size - 1)
        }
        
        // Update learning level based on score
        val newLevel = when {
            score > 0.9f && currentProfile.learningLevel == LearningLevel.NORMAL -> LearningLevel.ADVANCED
            score < 0.5f && currentProfile.learningLevel == LearningLevel.NORMAL -> LearningLevel.BEGINNER
            else -> currentProfile.learningLevel
        }
        
        saveProfile(currentProfile.copy(
            recentTopics = updatedTopics,
            learningLevel = newLevel
        ))
    }
}