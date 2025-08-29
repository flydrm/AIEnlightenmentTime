package com.enlightenment.ai.presentation.parent.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.domain.repository.LearningStatsRepository
import com.enlightenment.ai.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import android.content.Intent

/**
 * DetailedReports功能ViewModel
 * 
 * 职责说明：
 * 管理DetailedReports界面的UI状态和业务逻辑。
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
 * DetailedReportsViewModel - DetailedReports视图模型
 * 
 * 功能职责：
 * - 管理DetailedReports界面的业务逻辑
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
class DetailedReportsViewModel @Inject constructor(  // 依赖注入
    private val learningStatsRepository: LearningStatsRepository,
    private val storyRepository: StoryRepository,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
    companion object {
        // Keys for daily learning 数据
        private fun dailyMinutesKey(date: String) = intPreferencesKey("daily_minutes_$date")
        private fun dailyStoriesKey(date: String) = intPreferencesKey("daily_stories_$date")
        private fun storyThemeKey(storyId: String) = stringPreferencesKey("story_theme_$storyId")
    }
    
    private val _uiState = MutableStateFlow(DetailedReportsUiState())
    val uiState: StateFlow<DetailedReportsUiState> = _uiState.asStateFlow()
    
    init {
        loadReportData()
    }
    
    private fun loadReportData() {
        viewModelScope.launch {  // 启动协程执行异步操作
            learningStatsRepository.getLearningStats().collect { stats ->  // 收集数据流更新
                val weeklyProgress = calculateWeeklyProgress()
                val interestAnalysis = analyzeInterests()
                val achievements = checkAchievements(stats.totalStories, stats.learningDays)
                
                // Calculate actual total minutes from daily 数据
                val totalMinutes = calculateTotalMinutes()
                
                _uiState.value = DetailedReportsUiState(
                    totalDays = stats.learningDays,
                    totalStories = stats.totalStories,
                    totalMinutes = totalMinutes,
                    weeklyProgress = weeklyProgress,
                    interestAnalysis = interestAnalysis,
                    achievements = achievements
                )
            }
        }
    }
    
    private suspend fun calculateTotalMinutes(): Int {
        return dataStore.data.map { preferences ->
            preferences.asMap().entries
                .filter { it.key.name.startsWith("daily_minutes_") }
                .sumOf { (it.value as? Int) ?: 0 }
        }.first()
    }
    
    private suspend fun calculateWeeklyProgress(): List<DayProgress> {
        val today = LocalDate.now()
        val startOfWeek = today.with(DayOfWeek.MONDAY)
        
        return (0..6).map { dayOffset ->
            val date = startOfWeek.plusDays(dayOffset.toLong())
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA)
            val dateKey = date.toString()
            
            // Fetch actual 数据 from DataStore
            val minutes = dataStore.data.map { preferences ->
                preferences[dailyMinutesKey(dateKey)] ?: 0
            }.first()
            
            DayProgress(
                dayName = dayName,
                minutes = minutes,
                progress = minutes / 15f
            )
        }
    }
    
    private suspend fun analyzeInterests(): List<InterestCategory> {
        // Analyze actual 故事 themes from recent stories
        val recentStories = storyRepository.getRecentStories().first()
        val themeCount = mutableMapOf<String, Int>()
        
        // Count themes from DataStore
        dataStore.data.map { preferences ->
            recentStories.forEach { story ->
                val theme = preferences[storyThemeKey(story.id)] ?: "其他"
                themeCount[theme] = themeCount.getOrDefault(theme, 0) + 1
            }
        }.first()
        
        // Calculate percentages
        val total = themeCount.values.sum().coerceAtLeast(1)
        val categories = listOf("动物故事", "科学探索", "童话故事", "日常生活", "历史文化")
        
        return categories.map { category ->
            val count = themeCount[category] ?: 0
            val percentage = (count * 100) / total
            InterestCategory(category, percentage)
        }.sortedByDescending { it.percentage }
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
        viewModelScope.launch {  // 启动协程执行异步操作
            val reportContent = generateReportContent()
            shareReport(reportContent)
        }
    }
    
    private suspend fun generateReportContent(): String {
        val state = _uiState.value
        val dateRange = LocalDate.now().minusDays(30).toString() + " 至 " + LocalDate.now().toString()
        
        return buildString {
            appendLine("AI启蒙时光 - 学习报告")
            appendLine("===================")
            appendLine()
            appendLine("报告期间: $dateRange")
            appendLine()
            appendLine("学习总览")
            appendLine("---------")
            appendLine("总学习天数: ${state.totalDays}天")
            appendLine("总完成故事: ${state.totalStories}个")
            appendLine("总学习时长: ${state.totalMinutes}分钟")
            appendLine()
            appendLine("本周学习情况")
            appendLine("-----------")
            state.weeklyProgress.forEach { day ->
                appendLine("${day.dayName}: ${day.minutes}分钟")
            }
            appendLine()
            appendLine("兴趣分析")
            appendLine("--------")
            state.interestAnalysis.forEach { interest ->
                appendLine("${interest.category}: ${interest.percentage}%")
            }
            appendLine()
            appendLine("获得成就")
            appendLine("--------")
            state.achievements.filter { it.unlocked }.forEach { achievement ->
                appendLine("✓ ${achievement.name}")
            }
        }
    }
    
    private fun shareReport(content: String) {
        // Create share intent
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "AI启蒙时光学习报告")
            putExtra(Intent.EXTRA_TEXT, content)
        }
        
        // Share intent created - requires activity context to execute
        // This can be triggered from UI layer with proper context
    }
}
/**
 * DetailedReportsUiState
 * 
 * 功能说明：
 * 提供DetailedReportsUiState相关的功能实现。
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
 * DetailedReportsUiState - DetailedReportsUi状态
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
data class DetailedReportsUiState(
    val totalDays: Int = 0,
    val totalStories: Int = 0,
    val totalMinutes: Int = 0,
    val weeklyProgress: List<DayProgress> = emptyList(),
    val interestAnalysis: List<InterestCategory> = emptyList(),
    val achievements: List<Achievement> = emptyList()
)
/**
 * DayProgress
 * 
 * 功能说明：
 * 提供DayProgress相关的功能实现。
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
 * DayProgress - DayProgress
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
data class DayProgress(
    val dayName: String,
    val minutes: Int,
    val progress: Float
)
/**
 * InterestCategory
 * 
 * 功能说明：
 * 提供InterestCategory相关的功能实现。
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
 * InterestCategory - InterestCategory
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
data class InterestCategory(
    val category: String,
    val percentage: Int
)
/**
 * Achievement
 * 
 * 功能说明：
 * 提供Achievement相关的功能实现。
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
 * Achievement - Achievement
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
data class Achievement(
    val id: String,
    val name: String,
    val icon: String,
    val unlocked: Boolean
)