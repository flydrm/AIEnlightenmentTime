package com.enlightenment.ai.presentation.parent.settings

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PrivacySettings功能ViewModel
 * 
 * 职责说明：
 * 管理PrivacySettings界面的UI状态和业务逻辑。
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
 * PrivacySettingsViewModel - PrivacySettings视图模型
 * 
 * 功能职责：
 * - 管理PrivacySettings界面的业务逻辑
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
class PrivacySettingsViewModel @Inject constructor(  // 依赖注入
    application: Application,
    private val dataStore: DataStore<Preferences>
) : AndroidViewModel(application) {
    
    companion object {
        private const val PRIVACY_POLICY_URL = "https://ai-edu.cloud.tencent.com/privacy-policy"
        private val COLLECT_USAGE_KEY = booleanPreferencesKey("collect_usage_data")
        private val PERSONALIZED_KEY = booleanPreferencesKey("personalized_recommendations")
        private val LOCAL_BACKUP_KEY = booleanPreferencesKey("local_backup")
        private val CLOUD_SYNC_KEY = booleanPreferencesKey("cloud_sync")
        private val CAMERA_PERMISSION_KEY = booleanPreferencesKey("camera_permission")
        private val MICROPHONE_PERMISSION_KEY = booleanPreferencesKey("microphone_permission")
    }
    
    private val _uiState = MutableStateFlow(PrivacySettingsUiState())
    val uiState: StateFlow<PrivacySettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentSettings()
    }
    
    private fun loadCurrentSettings() {
        viewModelScope.launch {  // 启动协程执行异步操作
            dataStore.data.collect { preferences ->  // 收集数据流更新
                _uiState.update { state ->  // 更新UI状态
                    state.copy(
                        collectUsageData = preferences[COLLECT_USAGE_KEY] ?: false,
                        personalizedRecommendations = preferences[PERSONALIZED_KEY] ?: true,
                        localBackup = preferences[LOCAL_BACKUP_KEY] ?: true,
                        cloudSync = preferences[CLOUD_SYNC_KEY] ?: false,
                        cameraPermission = preferences[CAMERA_PERMISSION_KEY] ?: true,
                        microphonePermission = preferences[MICROPHONE_PERMISSION_KEY] ?: false
                    )
                }
            }
        }
    }
    
    fun toggleUsageDataCollection() {
        updateSetting(COLLECT_USAGE_KEY) { !uiState.value.collectUsageData }
        _uiState.update { it.copy(collectUsageData = !it.collectUsageData) }  // 更新UI状态
    }
    
    fun togglePersonalizedRecommendations() {
        updateSetting(PERSONALIZED_KEY) { !uiState.value.personalizedRecommendations }
        _uiState.update { it.copy(personalizedRecommendations = !it.personalizedRecommendations) }  // 更新UI状态
    }
    
    fun toggleLocalBackup() {
        updateSetting(LOCAL_BACKUP_KEY) { !uiState.value.localBackup }
        _uiState.update { it.copy(localBackup = !it.localBackup) }  // 更新UI状态
    }
    
    fun toggleCloudSync() {
        updateSetting(CLOUD_SYNC_KEY) { !uiState.value.cloudSync }
        _uiState.update { it.copy(cloudSync = !it.cloudSync) }  // 更新UI状态
    }
    
    fun toggleCameraPermission() {
        updateSetting(CAMERA_PERMISSION_KEY) { !uiState.value.cameraPermission }
        _uiState.update { it.copy(cameraPermission = !it.cameraPermission) }  // 更新UI状态
    }
    
    fun toggleMicrophonePermission() {
        updateSetting(MICROPHONE_PERMISSION_KEY) { !uiState.value.microphonePermission }
        _uiState.update { it.copy(microphonePermission = !it.microphonePermission) }  // 更新UI状态
    }
    
    private fun updateSetting(key: Preferences.Key<Boolean>, value: () -> Boolean) {
        viewModelScope.launch {  // 启动协程执行异步操作
            dataStore.edit { preferences ->
                preferences[key] = value()
            }
        }
    }
    
    fun clearCache() {
        viewModelScope.launch {  // 启动协程执行异步操作
            // Clear app 缓存
            getApplication<Application>().cacheDir.deleteRecursively()
        }
    }
    
    fun deleteAllData() {
        viewModelScope.launch {  // 启动协程执行异步操作
            // Clear all preferences
            dataStore.edit { it.clear() }
            // Clear databases and 缓存
            getApplication<Application>().cacheDir.deleteRecursively()
            getApplication<Application>().filesDir.deleteRecursively()
        }
    }
    
    fun openPrivacyPolicy() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(PRIVACY_POLICY_URL)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        try {
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {  // 捕获并处理异常
            // If no browser is available, show the URL in UI
            _uiState.update { it.copy(showPrivacyPolicyUrl = true) }  // 更新UI状态
        }
    }
}
/**
 * PrivacySettingsUiState
 * 
 * 功能说明：
 * 提供PrivacySettingsUiState相关的功能实现。
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
 * PrivacySettingsUiState - PrivacySettingsUi状态
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
data class PrivacySettingsUiState(
    val collectUsageData: Boolean = false,
    val personalizedRecommendations: Boolean = true,
    val localBackup: Boolean = true,
    val cloudSync: Boolean = false,
    val cameraPermission: Boolean = true,
    val microphonePermission: Boolean = false,
    val showPrivacyPolicyUrl: Boolean = false
)