package com.enlightenment.ai.presentation.parent.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.domain.repository.LearningStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DetailedReportsViewModel @Inject constructor(
    private val learningStatsRepository: LearningStatsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DetailedReportsUiState())
    val uiState: StateFlow<DetailedReportsUiState> = _uiState.asStateFlow()
    
    init {
        loadReportData()
    }
    
    private fun loadReportData() {
        viewModelScope.launch {
            learningStatsRepository.getLearningStats().collect { stats ->
                val weeklyProgress = calculateWeeklyProgress()
                val interestAnalysis = analyzeInterests()
                val achievements = checkAchievements(stats.totalStories, stats.learningDays)
                
                _uiState.value = DetailedReportsUiState(
                    totalDays = stats.learningDays,
                    totalStories = stats.totalStories,
                    totalMinutes = stats.learningDays * 15, // Assuming 15 min per day average
                    weeklyProgress = weeklyProgress,
                    interestAnalysis = interestAnalysis,
                    achievements = achievements
                )
            }
        }
    }
    
    private fun calculateWeeklyProgress(): List<DayProgress> {
        val today = LocalDate.now()
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        
        return (0..6).map { dayOffset ->
            val date = startOfWeek.plusDays(dayOffset.toLong())
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA)
            
            // In production, would fetch actual data for each day
            val minutes = if (date <= today) {
                if (date == today) 10 else 15
            } else 0
            
            DayProgress(
                dayName = dayName,
                minutes = minutes,
                progress = minutes / 15f
            )
        }
    }
    
    private fun analyzeInterests(): List<InterestCategory> {
        // In production, would analyze actual story viewing data
        return listOf(
            InterestCategory("动物故事", 35),
            InterestCategory("科学探索", 25),
            InterestCategory("童话故事", 20),
            InterestCategory("日常生活", 15),
            InterestCategory("历史文化", 5)
        )
    }
    
    private fun checkAchievements(totalStories: Int, totalDays: Int): List<Achievement> {
        return listOf(
            Achievement(
                id = "first_story",
                name = "初次探索",
                icon = "🌟",
                unlocked = totalStories >= 1
            ),
            Achievement(
                id = "week_streak",
                name = "周坚持者",
                icon = "🔥",
                unlocked = totalDays >= 7
            ),
            Achievement(
                id = "story_master",
                name = "故事大师",
                icon = "📚",
                unlocked = totalStories >= 20
            ),
            Achievement(
                id = "month_streak",
                name = "月度学霸",
                icon = "🏆",
                unlocked = totalDays >= 30
            )
        )
    }
    
    fun exportReport() {
        viewModelScope.launch {
            // In production, would generate PDF or share report
            // For now, just log the action
        }
    }
}

data class DetailedReportsUiState(
    val totalDays: Int = 0,
    val totalStories: Int = 0,
    val totalMinutes: Int = 0,
    val weeklyProgress: List<DayProgress> = emptyList(),
    val interestAnalysis: List<InterestCategory> = emptyList(),
    val achievements: List<Achievement> = emptyList()
)

data class DayProgress(
    val dayName: String,
    val minutes: Int,
    val progress: Float
)

data class InterestCategory(
    val category: String,
    val percentage: Int
)

data class Achievement(
    val id: String,
    val name: String,
    val icon: String,
    val unlocked: Boolean
)