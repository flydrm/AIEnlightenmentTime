package com.enlightenment.ai.presentation.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.domain.repository.LearningStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ParentDashboard功能ViewModel
 * 
 * 职责说明：
 * 管理ParentDashboard界面的UI状态和业务逻辑。
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
 * ParentDashboardViewModel - ParentDashboard视图模型
 * 
 * 功能职责：
 * - 管理ParentDashboard界面的业务逻辑
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
class ParentDashboardViewModel @Inject constructor(  // 依赖注入
    private val learningStatsRepository: LearningStatsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ParentDashboardUiState())
    val uiState: StateFlow<ParentDashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadStats()
    }
    
    private fun loadStats() {
        viewModelScope.launch {  // 启动协程执行异步操作
            // Get today's 数据
            val todayMinutes = learningStatsRepository.getTodayMinutes()
            val todayStories = learningStatsRepository.getTodayStories()
            
            // Observe overall stats
            learningStatsRepository.observeLearningStats().collect { stats ->  // 收集数据流更新
                _uiState.value = _uiState.value.copy(
                    learningStats = ParentDashboardStats(
                        todayMinutes = todayMinutes,
                        todayStories = todayStories,
                        streak = stats.currentStreak,
                        todayProgress = calculateTodayProgress(todayMinutes)
                    )
                )
            }
        }
    }
    
    private fun calculateTodayProgress(todayMinutes: Int): Float {
        // Target: 15 minutes per day
        val targetMinutes = 15f
        return (todayMinutes / targetMinutes).coerceIn(0f, 1f)
    }
    
    fun onTimeLimitClick() {
        _uiState.update { currentState ->  // 更新UI状态
            currentState.copy(
                navigationEvent = NavigationEvent.TimeLimitSettings
            )
        }
    }
    
    fun onContentPreferenceClick() {
        _uiState.update { currentState ->  // 更新UI状态
            currentState.copy(
                navigationEvent = NavigationEvent.ContentPreferences
            )
        }
    }
    
    fun onReportClick() {
        _uiState.update { currentState ->  // 更新UI状态
            currentState.copy(
                navigationEvent = NavigationEvent.DetailedReports
            )
        }
    }
    
    fun onPrivacyClick() {
        _uiState.update { currentState ->  // 更新UI状态
            currentState.copy(
                navigationEvent = NavigationEvent.PrivacySettings
            )
        }
    }
    
    fun onSettingsClick() {
        _uiState.update { currentState ->  // 更新UI状态
            currentState.copy(
                navigationEvent = NavigationEvent.AppSettings
            )
        }
    }
    
    fun clearNavigationEvent() {
        _uiState.update { currentState ->  // 更新UI状态
            currentState.copy(navigationEvent = null)
        }
    }
}
/**
 * ParentDashboardUiState
 * 
 * 功能说明：
 * 提供ParentDashboardUiState相关的功能实现。
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
 * ParentDashboardUiState - 家长仪表板UI状态
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
data class ParentDashboardUiState(
    val learningStats: ParentDashboardStats = ParentDashboardStats(),
    val navigationEvent: NavigationEvent? = null
)
/**
 * ParentDashboardStats
 * 
 * 功能说明：
 * 提供ParentDashboardStats相关的功能实现。
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
 * ParentDashboardStats - ParentDashboardStats
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
data class ParentDashboardStats(
    val todayMinutes: Int = 0,
    val todayStories: Int = 0,
    val streak: Int = 0,
    val todayProgress: Float = 0f
)
/**
 * NavigationEvent
 * 
 * 功能说明：
 * 提供NavigationEvent相关的功能实现。
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
 * NavigationEvent - NavigationEvent
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
sealed class NavigationEvent {
/**
 * TimeLimitSettings
 * 
 * 功能说明：
 * 提供TimeLimitSettings相关的功能实现。
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
     * TimeLimitSettings - TimeLimitSettings
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
    object TimeLimitSettings : NavigationEvent()
/**
 * ContentPreferences
 * 
 * 功能说明：
 * 提供ContentPreferences相关的功能实现。
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
     * ContentPreferences - ContentPreferences
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
    object ContentPreferences : NavigationEvent()
/**
 * DetailedReports
 * 
 * 功能说明：
 * 提供DetailedReports相关的功能实现。
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
     * DetailedReports - DetailedReports
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
    object DetailedReports : NavigationEvent()
/**
 * PrivacySettings
 * 
 * 功能说明：
 * 提供PrivacySettings相关的功能实现。
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
     * PrivacySettings - PrivacySettings
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
    object PrivacySettings : NavigationEvent()
/**
 * AppSettings
 * 
 * 功能说明：
 * 提供AppSettings相关的功能实现。
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
     * AppSettings - AppSettings
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
    object AppSettings : NavigationEvent()
}