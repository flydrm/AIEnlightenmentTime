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
class ContentPreferencesViewModel @Inject constructor(
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
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _uiState.update { state ->
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
        _uiState.update { state ->
            val newThemes = if (state.selectedThemes.contains(theme)) {
                state.selectedThemes - theme
            } else {
                state.selectedThemes + theme
            }
            state.copy(selectedThemes = newThemes)
        }
    }
    
    fun updateDifficulty(level: Int) {
        _uiState.update { state ->
            state.copy(difficultyLevel = level)
        }
    }
    
    fun toggleSensitiveFilter() {
        _uiState.update { state ->
            state.copy(filterSensitiveContent = !state.filterSensitiveContent)
        }
    }
    
    fun toggleScaryFilter() {
        _uiState.update { state ->
            state.copy(avoidScaryContent = !state.avoidScaryContent)
        }
    }
    
    fun saveSettings() {
        viewModelScope.launch {
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

data class ContentPreferencesUiState(
    val selectedThemes: Set<String> = setOf("animal", "fairy_tale"),
    val difficultyLevel: Int = 1, // 0: Easy, 1: Medium, 2: Hard
    val filterSensitiveContent: Boolean = true,
    val avoidScaryContent: Boolean = true
)