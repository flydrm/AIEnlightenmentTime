package com.enlightenment.ai.presentation.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.domain.model.Question
import com.enlightenment.ai.domain.model.Story
import com.enlightenment.ai.domain.usecase.GenerateStoryUseCase
import com.enlightenment.ai.presentation.common.TextToSpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 故事界面ViewModel
 * 
 * 职责说明：
 * 管理故事界面的UI状态和业务逻辑，协调用户交互和数据处理。
 * 负责故事的生成、播放、问答互动等功能。
 * 
 * 架构职责：
 * 1. 管理UI状态（加载中、成功、失败）
 * 2. 处理用户操作（生成、播放、答题）
 * 3. 协调业务逻辑和UI更新
 * 4. 管理语音播放功能
 * 
 * 状态管理：
 * - 使用StateFlow保证UI状态的一致性
 * - 所有状态更新都是原子操作
 * - 支持配置变更（如屏幕旋转）
 * 
 * @property generateStoryUseCase 故事生成用例，处理业务逻辑
 * @property ttsManager 文字转语音管理器，用于故事朗读
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@HiltViewModel
class StoryViewModel @Inject constructor(
    private val generateStoryUseCase: GenerateStoryUseCase,
    private val ttsManager: TextToSpeechManager
) : ViewModel() {
    
    // UI状态管理 - 使用StateFlow确保线程安全
    private val _uiState = MutableStateFlow<StoryUiState>(StoryUiState.Loading)
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()
    
    init {
        // 初始化时自动生成一个故事
        generateStory()
    }
    
    /**
     * 生成AI故事
     * 
     * 用户流程：
     * 1. 用户进入故事界面或点击"换一个"按钮
     * 2. 显示加载动画
     * 3. 调用AI服务生成故事
     * 4. 成功后显示故事内容
     * 5. 失败后显示友好的错误提示
     * 
     * 错误处理：
     * - 网络错误：提示检查网络
     * - 服务器错误：提示稍后再试
     * - 其他错误：通用错误提示
     * 
     * @param theme 故事主题，可选。如"冒险"、"友谊"等
     */
    fun generateStory(theme: String? = null) {
        viewModelScope.launch {
            // 更新为加载状态，显示加载动画
            _uiState.value = StoryUiState.Loading
            
            // 调用用例生成故事
            generateStoryUseCase(theme)
                .onSuccess { story ->
                    // 成功：转换为显示模型并更新UI
                    _uiState.value = StoryUiState.Success(story.toDisplayModel())
                }
                .onFailure { error ->
                    // 失败：显示用户友好的错误信息
                    _uiState.value = StoryUiState.Error(
                        message = "生成故事失败，请检查网络连接"
                    )
                }
        }
    }
    
    /**
     * 播放故事语音
     * 
     * 功能说明：
     * 使用TTS（文字转语音）技术朗读故事内容。
     * 适合不识字或喜欢听故事的儿童。
     * 
     * 前置条件：
     * - 故事已成功加载
     * - TTS服务可用
     * 
     * 使用场景：
     * - 用户点击播放按钮
     * - 自动播放设置开启
     */
    fun playStory() {
        val currentState = _uiState.value
        if (currentState is StoryUiState.Success) {
            // 只有在成功状态下才能播放
            ttsManager.speak(currentState.story.content)
        }
    }
    
    fun pauseStory() {
        ttsManager.pause()
    }
    
    override fun onCleared() {
        super.onCleared()
        ttsManager.stop()
    }
    
    fun answerQuestion(questionId: String, answerIndex: Int) {
        val currentState = _uiState.value
        if (currentState is StoryUiState.Success) {
            val updatedQuestions = currentState.story.questions.map { question ->
                if (question.id == questionId) {
                    question.copy(
                        isAnswered = true,
                        selectedAnswer = answerIndex,
                        feedback = if (answerIndex == question.correctAnswerIndex) {
                            "太棒了！回答正确！🎉"
                        } else {
                            "再想想，${question.explanation ?: "你可以做到的！"}"
                        }
                    )
                } else {
                    question
                }
            }
            
            _uiState.value = currentState.copy(
                story = currentState.story.copy(questions = updatedQuestions)
            )
        }
    }
    
    private fun Story.toDisplayModel(): StoryDisplayModel {
        return StoryDisplayModel(
            id = id,
            title = title,
            content = content,
            imageUrl = imageUrl,
            duration = duration,
            questions = questions.map { it.toDisplayModel() }
        )
    }
    
    private fun Question.toDisplayModel(): QuestionDisplayModel {
        return QuestionDisplayModel(
            id = id,
            text = text,
            options = options,
            correctAnswerIndex = correctAnswerIndex,
            explanation = explanation
        )
    }
}

sealed class StoryUiState {
    object Loading : StoryUiState()
    data class Success(val story: StoryDisplayModel) : StoryUiState()
    data class Error(val message: String) : StoryUiState()
}

data class StoryDisplayModel(
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val duration: Int,
    val questions: List<QuestionDisplayModel>
)

data class QuestionDisplayModel(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String?,
    val isAnswered: Boolean = false,
    val selectedAnswer: Int? = null,
    val feedback: String? = null
)