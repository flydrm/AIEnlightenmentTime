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

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
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
    
    fun setReminderTime() {
        // In production, would show time picker dialog
        _uiState.update { it.copy(reminderTime = "20:00") }
    }
    
    fun updateLanguage(language: String) {
        _uiState.update { it.copy(language = language) }
    }
    
    fun checkForUpdates() {
        viewModelScope.launch {
            // In production, would check for app updates
        }
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
    val appVersion: String = "1.0.0"
)