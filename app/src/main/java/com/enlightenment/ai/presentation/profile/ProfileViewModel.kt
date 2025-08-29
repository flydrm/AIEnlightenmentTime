package com.enlightenment.ai.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.domain.model.ChildProfile
import com.enlightenment.ai.domain.repository.ProfileRepository
import com.enlightenment.ai.domain.repository.LearningStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Profile功能ViewModel
 * 
 * 职责说明：
 * 管理Profile界面的UI状态和业务逻辑。
 * 负责数据的获取、处理和状态管理。
 * 
 * 核心功能：
 * 1. UI状态管理
 * 2. 业务逻辑处理  
 * 3. 数据获取和更新
 * 4. 用户交互响应
 * 
 * 技术特点：
 * - 使用StateFlow管理状态
 * - 协程处理异步操作
 * - 生命周期感知
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@HiltViewModel
/**
 * ProfileViewModel - Profile视图模型
 * 
 * 功能职责：
 * - 管理Profile界面的业务逻辑
 * - 处理用户交互事件和状态更新
 * - 协调数据层和展示层的通信
 * 
 * 状态管理：
 * - 使用StateFlow管理UI状态
 * - 支持配置变更后的状态保持
 * - 提供状态更新的原子性保证
 * 
 * 生命周期：
 * - 自动处理协程作用域
 * - 支持数据预加载
 * - 优雅的资源清理
 * 
 * @since 1.0.0
 */
class ProfileViewModel @Inject constructor(  // 依赖注入
    private val profileRepository: ProfileRepository,
    private val learningStatsRepository: LearningStatsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadProfile()
    }
    
    private fun loadProfile() {
        viewModelScope.launch {  // 启动协程执行异步操作
            val profile = profileRepository.getProfile()
            _uiState.value = if (profile != null) {
                ProfileUiState.HasProfile(profile.toDisplayModel())
            } else {
                ProfileUiState.NoProfile
            }
        }
    }
    
    fun createProfile(name: String, age: Int) {
        viewModelScope.launch {  // 启动协程执行异步操作
            val profile = ChildProfile(
                id = UUID.randomUUID().toString(),
                name = name,
                age = age
            )
            profileRepository.saveProfile(profile)
            _uiState.value = ProfileUiState.HasProfile(profile.toDisplayModel())
        }
    }
    
    fun updateInterests(interests: List<String>) {
        viewModelScope.launch {  // 启动协程执行异步操作
            profileRepository.updateInterests(interests)
            loadProfile() // Reload to get updated 档案
        }
    }
    
    private suspend fun ChildProfile.toDisplayModel(): ProfileDisplayModel {
        val stats = learningStatsRepository.observeLearningStats()
            .collect { learningStats ->  // 收集数据流更新
                return@collect ProfileDisplayModel(
                    id = id,
                    name = name,
                    age = age,
                    interests = interests,
                    learningLevel = learningLevel.toDisplayString(),
                    daysLearned = learningStats.totalLearningDays,
                    storiesCompleted = learningStats.totalStoriesCompleted
                )
            }
        
        // Fallback if 数据流 collection fails
        return ProfileDisplayModel(
            id = id,
            name = name,
            age = age,
            interests = interests,
            learningLevel = learningLevel.toDisplayString(),
            daysLearned = 0,
            storiesCompleted = 0
        )
    }
    
    private fun com.enlightenment.ai.domain.model.LearningLevel.toDisplayString(): String {
        return when (this) {
            com.enlightenment.ai.domain.model.LearningLevel.BEGINNER -> "初学者"
            com.enlightenment.ai.domain.model.LearningLevel.NORMAL -> "进阶学习者"
            com.enlightenment.ai.domain.model.LearningLevel.ADVANCED -> "小专家"
        }
    }
}
/**
 * ProfileUiState
 * 
 * 功能说明：
 * 提供ProfileUiState相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */


/**
 * ProfileUiState - 个人档案UI状态
 * 
 * 功能描述：
 * - 提供核心业务功能处理功能
 * - 支持灵活配置、易于扩展、高性能
 * 
 * 设计说明：
 * - 采用密封类层次设计
 * - 遵循项目统一的架构规范
 * 
 * @since 1.0.0
 */
sealed class ProfileUiState {
/**
 * 加载中
 * 
 * 功能说明：
 * 提供Loading相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * 加载中 - 加载中
     * 
     * 功能描述：
     * - 提供核心业务功能处理功能
     * - 支持灵活配置、易于扩展、高性能
     * 
     * 设计说明：
     * - 采用单例模式设计
     * - 遵循项目统一的架构规范
     * 
     * @自版本 1.0.0
     */
    object Loading : ProfileUiState()
/**
 * NoProfile
 * 
 * 功能说明：
 * 提供NoProfile相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * NoProfile - NoProfile
     * 
     * 功能描述：
     * - 提供核心业务功能处理功能
     * - 支持灵活配置、易于扩展、高性能
     * 
     * 设计说明：
     * - 采用单例模式设计
     * - 遵循项目统一的架构规范
     * 
     * @自版本 1.0.0
     */
    object NoProfile : ProfileUiState()
/**
 * HasProfile
 * 
 * 功能说明：
 * 提供HasProfile相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * HasProfile - HasProfile
     * 
     * 功能描述：
     * - 提供核心业务功能处理功能
     * - 支持灵活配置、易于扩展、高性能
     * 
     * 设计说明：
     * - 采用数据类设计
     * - 遵循项目统一的架构规范
     * 
     * @自版本 1.0.0
     */
    data class HasProfile(val profile: ProfileDisplayModel) : ProfileUiState()
}
/**
 * ProfileDisplayModel
 * 
 * 功能说明：
 * 提供ProfileDisplayModel相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */


/**
 * ProfileDisplayModel - ProfileDisplayModel
 * 
 * 功能描述：
 * - 提供核心业务功能处理功能
 * - 支持灵活配置、易于扩展、高性能
 * 
 * 设计说明：
 * - 采用数据类设计
 * - 遵循项目统一的架构规范
 * 
 * @since 1.0.0
 */
data class ProfileDisplayModel(
    val id: String,
    val name: String,
    val age: Int,
    val interests: List<String>,
    val learningLevel: String,
    val daysLearned: Int,
    val storiesCompleted: Int
)