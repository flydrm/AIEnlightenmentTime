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

@HiltViewModel
class ParentDashboardViewModel @Inject constructor(
    private val learningStatsRepository: LearningStatsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ParentDashboardUiState())
    val uiState: StateFlow<ParentDashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadStats()
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            // Get today's data
            val todayMinutes = learningStatsRepository.getTodayMinutes()
            val todayStories = learningStatsRepository.getTodayStories()
            
            // Observe overall stats
            learningStatsRepository.observeLearningStats().collect { stats ->
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
        _uiState.update { currentState ->
            currentState.copy(
                navigationEvent = NavigationEvent.TimeLimitSettings
            )
        }
    }
    
    fun onContentPreferenceClick() {
        _uiState.update { currentState ->
            currentState.copy(
                navigationEvent = NavigationEvent.ContentPreferences
            )
        }
    }
    
    fun onReportClick() {
        _uiState.update { currentState ->
            currentState.copy(
                navigationEvent = NavigationEvent.DetailedReports
            )
        }
    }
    
    fun onPrivacyClick() {
        _uiState.update { currentState ->
            currentState.copy(
                navigationEvent = NavigationEvent.PrivacySettings
            )
        }
    }
    
    fun onSettingsClick() {
        _uiState.update { currentState ->
            currentState.copy(
                navigationEvent = NavigationEvent.AppSettings
            )
        }
    }
    
    fun clearNavigationEvent() {
        _uiState.update { currentState ->
            currentState.copy(navigationEvent = null)
        }
    }
}

data class ParentDashboardUiState(
    val learningStats: ParentDashboardStats = ParentDashboardStats(),
    val navigationEvent: NavigationEvent? = null
)

data class ParentDashboardStats(
    val todayMinutes: Int = 0,
    val todayStories: Int = 0,
    val streak: Int = 0,
    val todayProgress: Float = 0f
)

sealed class NavigationEvent {
    object TimeLimitSettings : NavigationEvent()
    object ContentPreferences : NavigationEvent()
    object DetailedReports : NavigationEvent()
    object PrivacySettings : NavigationEvent()
    object AppSettings : NavigationEvent()
}