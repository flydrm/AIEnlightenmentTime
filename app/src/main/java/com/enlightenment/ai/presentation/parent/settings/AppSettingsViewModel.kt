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

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
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
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _uiState.update { state ->
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
        _uiState.update { it.copy(fontSize = size) }
    }
    
    fun toggleEyeProtection() {
        _uiState.update { it.copy(eyeProtectionMode = !it.eyeProtectionMode) }
    }
    
    fun toggleBackgroundMusic() {
        _uiState.update { it.copy(backgroundMusic = !it.backgroundMusic) }
    }
    
    fun toggleSoundEffects() {
        _uiState.update { it.copy(soundEffects = !it.soundEffects) }
    }
    
    fun updateSpeechRate(rate: Float) {
        _uiState.update { it.copy(speechRate = rate) }
    }
    
    fun toggleLearningReminders() {
        _uiState.update { it.copy(learningReminders = !it.learningReminders) }
    }
    
    fun setReminderTime(hour: Int, minute: Int) {
        val time = LocalTime.of(hour, minute)
        val timeString = time.format(timeFormatter)
        _uiState.update { it.copy(reminderTime = timeString) }
    }
    
    fun showTimePicker() {
        // Parse current time
        val currentTime = try {
            LocalTime.parse(_uiState.value.reminderTime, timeFormatter)
        } catch (e: Exception) {
            LocalTime.of(19, 0) // Default to 7 PM
        }
        
        _uiState.update { 
            it.copy(
                showTimePickerDialog = true,
                timePickerHour = currentTime.hour,
                timePickerMinute = currentTime.minute
            )
        }
    }
    
    fun dismissTimePicker() {
        _uiState.update { it.copy(showTimePickerDialog = false) }
    }
    
    fun updateLanguage(language: String) {
        _uiState.update { it.copy(language = language) }
    }
    
    fun checkForUpdates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUpdate = true) }
            
            try {
                // Simulate version check - in production would call update API
                val latestVersion = "1.1.0" // Would come from API
                val currentVersion = BuildConfig.VERSION_NAME
                
                val hasUpdate = isNewerVersion(latestVersion, currentVersion)
                
                _uiState.update { 
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
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isCheckingUpdate = false,
                        updateCheckMessage = "检查更新失败，请稍后重试"
                    )
                }
            }
        }
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
        viewModelScope.launch {
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