package com.enlightenment.ai.presentation.dialogue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.domain.model.DialogueMessage
import com.enlightenment.ai.domain.usecase.SendDialogueMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DialogueViewModel @Inject constructor(
    private val sendDialogueMessageUseCase: SendDialogueMessageUseCase
) : ViewModel() {
    
    private val conversationId = UUID.randomUUID().toString()
    
    private val _uiState = MutableStateFlow(DialogueUiState())
    val uiState: StateFlow<DialogueUiState> = _uiState.asStateFlow()
    
    init {
        // Send initial greeting
        addAIMessage(
            content = "嗨！我是小熊猫！今天想聊什么呢？",
            emotion = "happy",
            suggestions = listOf("讲个故事", "教我数数", "唱首歌")
        )
    }
    
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        // Add user message immediately
        addUserMessage(message)
        
        // Set loading state
        _uiState.update { it.copy(isLoading = true, suggestedReplies = emptyList()) }
        
        viewModelScope.launch {
            sendDialogueMessageUseCase(message, conversationId)
                .onSuccess { aiResponse ->
                    addAIMessage(
                        content = aiResponse.content,
                        emotion = aiResponse.emotion,
                        suggestions = aiResponse.suggestedActions
                    )
                }
                .onFailure {
                    addAIMessage(
                        content = "哎呀，我没听清楚，能再说一遍吗？",
                        emotion = "confused"
                    )
                }
            
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    private fun addUserMessage(content: String) {
        val message = MessageDisplayModel(
            id = UUID.randomUUID().toString(),
            content = content,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        
        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages + message
            )
        }
    }
    
    private fun addAIMessage(
        content: String,
        emotion: String? = null,
        suggestions: List<String> = emptyList()
    ) {
        val message = MessageDisplayModel(
            id = UUID.randomUUID().toString(),
            content = content,
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            emotion = emotion
        )
        
        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages + message,
                suggestedReplies = suggestions
            )
        }
    }
}

data class DialogueUiState(
    val messages: List<MessageDisplayModel> = emptyList(),
    val isLoading: Boolean = false,
    val suggestedReplies: List<String> = emptyList()
)

data class MessageDisplayModel(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val emotion: String? = null
)