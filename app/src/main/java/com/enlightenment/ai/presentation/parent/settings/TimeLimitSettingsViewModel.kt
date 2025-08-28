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

@HiltViewModel
class TimeLimitSettingsViewModel @Inject constructor(
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
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _uiState.update { state ->
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
        _uiState.update { state ->
            state.copy(dailyLimitMinutes = minutes)
        }
    }
    
    fun toggleBreakReminder() {
        _uiState.update { state ->
            state.copy(breakReminderEnabled = !state.breakReminderEnabled)
        }
    }
    
    fun updateBreakInterval(minutes: Int) {
        _uiState.update { state ->
            state.copy(breakIntervalMinutes = minutes)
        }
    }
    
    fun toggleSleepProtection() {
        _uiState.update { state ->
            state.copy(sleepProtectionEnabled = !state.sleepProtectionEnabled)
        }
    }
    
    fun saveSettings() {
        viewModelScope.launch {
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

data class TimeLimitSettingsUiState(
    val dailyLimitMinutes: Int = 15,
    val breakReminderEnabled: Boolean = true,
    val breakIntervalMinutes: Int = 10,
    val sleepProtectionEnabled: Boolean = true
)