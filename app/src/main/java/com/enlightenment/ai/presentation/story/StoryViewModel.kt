package com.enlightenment.ai.presentation.story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.domain.model.Question
import com.enlightenment.ai.domain.model.Story
import com.enlightenment.ai.domain.usecase.GenerateStoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoryViewModel @Inject constructor(
    private val generateStoryUseCase: GenerateStoryUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<StoryUiState>(StoryUiState.Loading)
    val uiState: StateFlow<StoryUiState> = _uiState.asStateFlow()
    
    init {
        generateStory()
    }
    
    fun generateStory(theme: String? = null) {
        viewModelScope.launch {
            _uiState.value = StoryUiState.Loading
            
            generateStoryUseCase(theme)
                .onSuccess { story ->
                    _uiState.value = StoryUiState.Success(story.toDisplayModel())
                }
                .onFailure { error ->
                    _uiState.value = StoryUiState.Error(
                        message = "生成故事失败，请检查网络连接"
                    )
                }
        }
    }
    
    fun playStory() {
        // TODO: Implement TTS
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