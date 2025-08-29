package com.enlightenment.ai.presentation.parent.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
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
 * ContentPreferences功能ViewModel
 * 
 * 职责说明：
 * 管理ContentPreferences界面的UI状态和业务逻辑。
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
 * ContentPreferencesViewModel - ContentPreferences视图模型
 * 
 * 功能职责：
 * - 管理ContentPreferences界面的业务逻辑
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
class ContentPreferencesViewModel @Inject constructor(  // 依赖注入
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
    companion object {
        private val SELECTED_THEMES_KEY = stringSetPreferencesKey("selected_themes")
        private val DIFFICULTY_LEVEL_KEY = intPreferencesKey("difficulty_level")
        private val FILTER_SENSITIVE_KEY = booleanPreferencesKey("filter_sensitive")
        private val AVOID_SCARY_KEY = booleanPreferencesKey("avoid_scary")
    }
    
    private val _uiState = MutableStateFlow(ContentPreferencesUiState())
    val uiState: StateFlow<ContentPreferencesUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentSettings()
    }
    
    private fun loadCurrentSettings() {
        viewModelScope.launch {  // 启动协程执行异步操作
            dataStore.data.collect { preferences ->  // 收集数据流更新
                _uiState.update { state ->  // 更新UI状态
                    state.copy(
                        selectedThemes = preferences[SELECTED_THEMES_KEY] ?: setOf("animal", "fairy_tale"),
                        difficultyLevel = preferences[DIFFICULTY_LEVEL_KEY] ?: 1,
                        filterSensitiveContent = preferences[FILTER_SENSITIVE_KEY] ?: true,
                        avoidScaryContent = preferences[AVOID_SCARY_KEY] ?: true
                    )
                }
            }
        }
    }
    
    fun toggleTheme(theme: String) {
        _uiState.update { state ->  // 更新UI状态
            val newThemes = if (state.selectedThemes.contains(theme)) {
                state.selectedThemes - theme
            } else {
                state.selectedThemes + theme
            }
            state.copy(selectedThemes = newThemes)
        }
    }
    
    fun updateDifficulty(level: Int) {
        _uiState.update { state ->  // 更新UI状态
            state.copy(difficultyLevel = level)
        }
    }
    
    fun toggleSensitiveFilter() {
        _uiState.update { state ->  // 更新UI状态
            state.copy(filterSensitiveContent = !state.filterSensitiveContent)
        }
    }
    
    fun toggleScaryFilter() {
        _uiState.update { state ->  // 更新UI状态
            state.copy(avoidScaryContent = !state.avoidScaryContent)
        }
    }
    
    fun saveSettings() {
        viewModelScope.launch {  // 启动协程执行异步操作
            val currentState = _uiState.value
            dataStore.edit { preferences ->
                preferences[SELECTED_THEMES_KEY] = currentState.selectedThemes
                preferences[DIFFICULTY_LEVEL_KEY] = currentState.difficultyLevel
                preferences[FILTER_SENSITIVE_KEY] = currentState.filterSensitiveContent
                preferences[AVOID_SCARY_KEY] = currentState.avoidScaryContent
            }
        }
    }
}
/**
 * ContentPreferencesUiState
 * 
 * 功能说明：
 * 提供ContentPreferencesUiState相关的功能实现。
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
 * ContentPreferencesUiState - ContentPreferencesUi状态
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
data class ContentPreferencesUiState(
    val selectedThemes: Set<String> = setOf("animal", "fairy_tale"),
    val difficultyLevel: Int = 1, // 0: Easy, 1: Medium, 2: Hard
    val filterSensitiveContent: Boolean = true,
    val avoidScaryContent: Boolean = true
)