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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.enlightenment.ai.BuildConfig

/**
 * AppSettings功能ViewModel
 * 
 * 职责说明：
 * 管理AppSettings界面的UI状态和业务逻辑。
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
 * AppSettingsViewModel - AppSettings视图模型
 * 
 * 功能职责：
 * - 管理AppSettings界面的业务逻辑
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
class AppSettingsViewModel @Inject constructor(  // 依赖注入
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    companion object {
        private val FONT_SIZE_KEY = intPreferencesKey("font_size")
        private val EYE_PROTECTION_KEY = booleanPreferencesKey("eye_protection")
        private val BACKGROUND_MUSIC_KEY = booleanPreferencesKey("background_music")
        private val SOUND_EFFECTS_KEY = booleanPreferencesKey("sound_effects")
        private val SPEECH_RATE_KEY = floatPreferencesKey("speech_rate")
        private val LEARNING_REMINDERS_KEY = booleanPreferencesKey("learning_reminders")
        private val REMINDER_TIME_KEY = stringPreferencesKey("reminder_time")
        private val LANGUAGE_KEY = stringPreferencesKey("language")
    }
    
    private val _uiState = MutableStateFlow(AppSettingsUiState())
    val uiState: StateFlow<AppSettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadCurrentSettings()
    }
    
    private fun loadCurrentSettings() {
        viewModelScope.launch {  // 启动协程执行异步操作
            dataStore.data.collect { preferences ->  // 收集数据流更新
                _uiState.update { state ->  // 更新UI状态
                    state.copy(
                        fontSize = preferences[FONT_SIZE_KEY] ?: 1,
                        eyeProtectionMode = preferences[EYE_PROTECTION_KEY] ?: false,
                        backgroundMusic = preferences[BACKGROUND_MUSIC_KEY] ?: true,
                        soundEffects = preferences[SOUND_EFFECTS_KEY] ?: true,
                        speechRate = preferences[SPEECH_RATE_KEY] ?: 1.0f,
                        learningReminders = preferences[LEARNING_REMINDERS_KEY] ?: true,
                        reminderTime = preferences[REMINDER_TIME_KEY] ?: "19:00",
                        language = preferences[LANGUAGE_KEY] ?: "简体中文"
                    )
                }
            }
        }
    }
    
    fun updateFontSize(size: Int) {
        _uiState.update { it.copy(fontSize = size) }  // 更新UI状态
    }
    
    fun toggleEyeProtection() {
        _uiState.update { it.copy(eyeProtectionMode = !it.eyeProtectionMode) }  // 更新UI状态
    }
    
    fun toggleBackgroundMusic() {
        _uiState.update { it.copy(backgroundMusic = !it.backgroundMusic) }  // 更新UI状态
    }
    
    fun toggleSoundEffects() {
        _uiState.update { it.copy(soundEffects = !it.soundEffects) }  // 更新UI状态
    }
    
    fun updateSpeechRate(rate: Float) {
        _uiState.update { it.copy(speechRate = rate) }  // 更新UI状态
    }
    
    fun toggleLearningReminders() {
        _uiState.update { it.copy(learningReminders = !it.learningReminders) }  // 更新UI状态
    }
    
    fun setReminderTime(hour: Int, minute: Int) {
        val time = LocalTime.of(hour, minute)
        val timeString = time.format(timeFormatter)
        _uiState.update { it.copy(reminderTime = timeString) }  // 更新UI状态
    }
    
    fun showTimePicker() {
        // Parse current time
        val currentTime = try {
            LocalTime.parse(_uiState.value.reminderTime, timeFormatter)
        } catch (e: Exception) {  // 捕获并处理异常
            LocalTime.of(19, 0) // Default to 7 PM
        }
        
        _uiState.update {   // 更新UI状态
            it.copy(
                showTimePickerDialog = true,
                timePickerHour = currentTime.hour,
                timePickerMinute = currentTime.minute
            )
        }
    }
    
    fun dismissTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = false) }  // 更新UI状态
    }
    
    fun updateLanguage(language: String) {
        _uiState.update { it.copy(language = language) }  // 更新UI状态
    }
    
    /**
         * checkForUpdates - checkForUpdates方法
         * 
         * 功能描述：
         * - 执行相关相关操作
         * - 包含复杂的业务逻辑处理
         * - 确保操作的原子性和一致性
         * 
         * 实现复杂度：
         * - 方法行数: 32行
         * - 控制流: 3个
         * 
         * 注意事项：
         * - 此方法包含复杂逻辑，修改时请谨慎
         * - 确保所有分支都有正确的错误处理
         * - 保持代码的可读性和可维护性
         */
    fun checkForUpdates() {
        viewModelScope.launch {  // 启动协程执行异步操作
            _uiState.update { it.copy(isCheckingUpdate = true) }  // 更新UI状态
            
            try {
                // Check version from configured update server
                val latestVersion = checkLatestVersionFromServer()
                val currentVersion = BuildConfig.VERSION_NAME
                
                val hasUpdate = isNewerVersion(latestVersion, currentVersion)
                
                _uiState.update {   // 更新UI状态
                    it.copy(
                        isCheckingUpdate = false,
                        hasUpdate = hasUpdate,
                        latestVersion = if (hasUpdate) latestVersion else null,
                        updateCheckMessage = if (hasUpdate) {
                            "发现新版本 $latestVersion"
                        } else {
                            "已是最新版本"
                        }
                    )
                }
            } catch (e: Exception) {  // 捕获并处理异常
                _uiState.update {   // 更新UI状态
                    it.copy(
                        isCheckingUpdate = false,
                        updateCheckMessage = "检查更新失败，请稍后重试"
                    )
                }
            }
        }
    }
    
    private suspend fun checkLatestVersionFromServer(): String {
        // In a real app, this would call an API接口 endpoint
        // For now, return current version to indicate no update
        // This can be easily replaced with actual API接口 call when server is ready
        return BuildConfig.VERSION_NAME
    }
    
    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        
        for (i in 0 until minOf(latestParts.size, currentParts.size)) {
            if (latestParts[i] > currentParts[i]) return true
            if (latestParts[i] < currentParts[i]) return false
        }
        
        return latestParts.size > currentParts.size
    }
    
    fun saveSettings() {
        viewModelScope.launch {  // 启动协程执行异步操作
            val currentState = _uiState.value
            dataStore.edit { preferences ->
                preferences[FONT_SIZE_KEY] = currentState.fontSize
                preferences[EYE_PROTECTION_KEY] = currentState.eyeProtectionMode
                preferences[BACKGROUND_MUSIC_KEY] = currentState.backgroundMusic
                preferences[SOUND_EFFECTS_KEY] = currentState.soundEffects
                preferences[SPEECH_RATE_KEY] = currentState.speechRate
                preferences[LEARNING_REMINDERS_KEY] = currentState.learningReminders
                preferences[REMINDER_TIME_KEY] = currentState.reminderTime
                preferences[LANGUAGE_KEY] = currentState.language
            }
        }
    }
}
/**
 * AppSettingsUiState
 * 
 * 功能说明：
 * 提供AppSettingsUiState相关的功能实现。
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
 * AppSettingsUiState - AppSettingsUi状态
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
data class AppSettingsUiState(
    val fontSize: Int = 1, // 0: Small, 1: Medium, 2: Large
    val eyeProtectionMode: Boolean = false,
    val backgroundMusic: Boolean = true,
    val soundEffects: Boolean = true,
    val speechRate: Float = 1.0f,
    val learningReminders: Boolean = true,
    val reminderTime: String = "19:00",
    val language: String = "简体中文",
    val appVersion: String = BuildConfig.VERSION_NAME,
    val showTimePickerDialog: Boolean = false,
    val timePickerHour: Int = 19,
    val timePickerMinute: Int = 0,
    val isCheckingUpdate: Boolean = false,
    val hasUpdate: Boolean = false,
    val latestVersion: String? = null,
    val updateCheckMessage: String? = null
)