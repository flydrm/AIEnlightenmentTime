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

@HiltViewModel
class PrivacySettingsViewModel @Inject constructor(
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
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _uiState.update { state ->
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
        _uiState.update { it.copy(collectUsageData = !it.collectUsageData) }
    }
    
    fun togglePersonalizedRecommendations() {
        updateSetting(PERSONALIZED_KEY) { !uiState.value.personalizedRecommendations }
        _uiState.update { it.copy(personalizedRecommendations = !it.personalizedRecommendations) }
    }
    
    fun toggleLocalBackup() {
        updateSetting(LOCAL_BACKUP_KEY) { !uiState.value.localBackup }
        _uiState.update { it.copy(localBackup = !it.localBackup) }
    }
    
    fun toggleCloudSync() {
        updateSetting(CLOUD_SYNC_KEY) { !uiState.value.cloudSync }
        _uiState.update { it.copy(cloudSync = !it.cloudSync) }
    }
    
    fun toggleCameraPermission() {
        updateSetting(CAMERA_PERMISSION_KEY) { !uiState.value.cameraPermission }
        _uiState.update { it.copy(cameraPermission = !it.cameraPermission) }
    }
    
    fun toggleMicrophonePermission() {
        updateSetting(MICROPHONE_PERMISSION_KEY) { !uiState.value.microphonePermission }
        _uiState.update { it.copy(microphonePermission = !it.microphonePermission) }
    }
    
    private fun updateSetting(key: Preferences.Key<Boolean>, value: () -> Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[key] = value()
            }
        }
    }
    
    fun clearCache() {
        viewModelScope.launch {
            // Clear app cache
            getApplication<Application>().cacheDir.deleteRecursively()
        }
    }
    
    fun deleteAllData() {
        viewModelScope.launch {
            // Clear all preferences
            dataStore.edit { it.clear() }
            // Clear databases and cache
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
        } catch (e: Exception) {
            // If no browser is available, show the URL in UI
            _uiState.update { it.copy(showPrivacyPolicyUrl = true) }
        }
    }
}

data class PrivacySettingsUiState(
    val collectUsageData: Boolean = false,
    val personalizedRecommendations: Boolean = true,
    val localBackup: Boolean = true,
    val cloudSync: Boolean = false,
    val cameraPermission: Boolean = true,
    val microphonePermission: Boolean = false,
    val showPrivacyPolicyUrl: Boolean = false
)