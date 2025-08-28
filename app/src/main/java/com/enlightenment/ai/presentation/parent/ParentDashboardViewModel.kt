package com.enlightenment.ai.presentation.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.domain.repository.LearningStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            learningStatsRepository.observeLearningStats().collect { stats ->
                _uiState.value = _uiState.value.copy(
                    learningStats = ParentDashboardStats(
                        todayMinutes = calculateTodayMinutes(stats.totalLearningMinutes),
                        todayStories = calculateTodayStories(stats.totalStoriesCompleted),
                        streak = stats.currentStreak,
                        todayProgress = calculateTodayProgress()
                    )
                )
            }
        }
    }
    
    private fun calculateTodayMinutes(totalMinutes: Int): Int {
        // In real app, would filter by today's date
        return minOf(totalMinutes, 15) // Mock: max 15 minutes today
    }
    
    private fun calculateTodayStories(totalStories: Int): Int {
        // In real app, would filter by today's date
        return minOf(totalStories, 2) // Mock: max 2 stories today
    }
    
    private fun calculateTodayProgress(): Float {
        // Target: 15 minutes per day
        return 0.6f // Mock: 60% complete
    }
    
    fun onTimeLimitClick() {
        // Navigate to time limit settings
    }
    
    fun onContentPreferenceClick() {
        // Navigate to content preferences
    }
    
    fun onReportClick() {
        // Navigate to detailed reports
    }
    
    fun onPrivacyClick() {
        // Navigate to privacy settings
    }
    
    fun onSettingsClick() {
        // Navigate to app settings
    }
}

data class ParentDashboardUiState(
    val learningStats: ParentDashboardStats = ParentDashboardStats()
)

data class ParentDashboardStats(
    val todayMinutes: Int = 0,
    val todayStories: Int = 0,
    val streak: Int = 0,
    val todayProgress: Float = 0f
)