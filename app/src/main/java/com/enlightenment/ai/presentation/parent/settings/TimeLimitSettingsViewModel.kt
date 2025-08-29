package com.enlightenment.ai.presentation.parent.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * TimeLimitSettings功能ViewModel
 * 
 * 职责说明：
 * 管理TimeLimitSettings界面的UI状态和业务逻辑。
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
 * TimeLimitSettingsViewModel - TimeLimitSettings视图模型
 * 
 * 功能职责：
 * - 管理TimeLimitSettings界面的业务逻辑
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
class TimeLimitSettingsViewModel @Inject constructor(  // 依赖注入
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
    companion object {
        private val DAILY_LIMIT_KEY = intPreferencesKey("daily_limit_minutes")
        private val BREAK_REMINDER_KEY = booleanPreferencesKey("break_reminder_enabled")
        private val BREAK_INTERVAL_KEY = intPreferencesKey("break_interval_minutes")
        private val SLEEP_PROTECTION_KEY = booleanPreferencesKey("sleep_protection_enabled")
    }
    
    private val _uiState = MutableStateFlow(TimeLimitSettingsUiState())
    val uiState: StateFlow<TimeLimitSettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentSettings()
    }
    
    private fun loadCurrentSettings() {
        viewModelScope.launch {  // 启动协程执行异步操作
            dataStore.data.collect { preferences ->  // 收集数据流更新
                _uiState.update { state ->  // 更新UI状态
                    state.copy(
                        dailyLimitMinutes = preferences[DAILY_LIMIT_KEY] ?: 15,
                        breakReminderEnabled = preferences[BREAK_REMINDER_KEY] ?: true,
                        breakIntervalMinutes = preferences[BREAK_INTERVAL_KEY] ?: 10,
                        sleepProtectionEnabled = preferences[SLEEP_PROTECTION_KEY] ?: true
                    )
                }
            }
        }
    }
    
    fun updateDailyLimit(minutes: Int) {
        _uiState.update { state ->  // 更新UI状态
            state.copy(dailyLimitMinutes = minutes)
        }
    }
    
    fun toggleBreakReminder() {
        _uiState.update { state ->  // 更新UI状态
            state.copy(breakReminderEnabled = !state.breakReminderEnabled)
        }
    }
    
    fun updateBreakInterval(minutes: Int) {
        _uiState.update { state ->  // 更新UI状态
            state.copy(breakIntervalMinutes = minutes)
        }
    }
    
    fun toggleSleepProtection() {
        _uiState.update { state ->  // 更新UI状态
            state.copy(sleepProtectionEnabled = !state.sleepProtectionEnabled)
        }
    }
    
    fun saveSettings() {
        viewModelScope.launch {  // 启动协程执行异步操作
            val currentState = _uiState.value
            dataStore.edit { preferences ->
                preferences[DAILY_LIMIT_KEY] = currentState.dailyLimitMinutes
                preferences[BREAK_REMINDER_KEY] = currentState.breakReminderEnabled
                preferences[BREAK_INTERVAL_KEY] = currentState.breakIntervalMinutes
                preferences[SLEEP_PROTECTION_KEY] = currentState.sleepProtectionEnabled
            }
        }
    }
}
/**
 * TimeLimitSettingsUiState
 * 
 * 功能说明：
 * 提供TimeLimitSettingsUiState相关的功能实现。
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
 * TimeLimitSettingsUiState - TimeLimitSettingsUi状态
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
data class TimeLimitSettingsUiState(
    val dailyLimitMinutes: Int = 15,
    val breakReminderEnabled: Boolean = true,
    val breakIntervalMinutes: Int = 10,
    val sleepProtectionEnabled: Boolean = true
)