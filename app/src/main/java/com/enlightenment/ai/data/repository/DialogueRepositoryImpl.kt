package com.enlightenment.ai.data.repository

import com.enlightenment.ai.data.remote.api.AIApiService
import com.enlightenment.ai.data.remote.api.DialogueContextRequest
import com.enlightenment.ai.data.remote.api.DialogueRequest
import com.enlightenment.ai.data.remote.api.MessageHistory
import com.enlightenment.ai.domain.model.ConversationContext
import com.enlightenment.ai.domain.model.DialogueMessage
import com.enlightenment.ai.domain.repository.DialogueRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DialogueRepositoryImpl @Inject constructor(
    private val apiService: AIApiService
) : DialogueRepository {
    
    // In-memory storage for conversation history (in production, use Room)
    private val conversationHistory = mutableMapOf<String, MutableList<DialogueMessage>>()
    
    override suspend fun sendMessage(
        message: String,
        context: ConversationContext
    ): Result<DialogueMessage> = withContext(Dispatchers.IO) {
        try {
            // Create user message
            val userMessage = DialogueMessage(
                id = UUID.randomUUID().toString(),
                content = message,
                isFromUser = true
            )
            
            // Add to history
            conversationHistory.getOrPut(context.conversationId) { mutableListOf() }
                .add(userMessage)
            
            // Prepare API request
            val request = DialogueRequest(
                message = message,
                context = DialogueContextRequest(
                    conversationId = context.conversationId,
                    childAge = context.childAge,
                    history = context.recentMessages.map {
                        MessageHistory(
                            text = it.content,
                            isFromUser = it.isFromUser,
                            timestamp = it.timestamp
                        )
                    }
                )
            )
            
            // Call API
            val response = apiService.sendDialogueMessage(request)
            
            // Create AI response message
            val aiMessage = DialogueMessage(
                id = UUID.randomUUID().toString(),
                content = response.reply.text,
                isFromUser = false,
                emotion = response.reply.emotion,
                suggestedActions = response.reply.suggestions ?: emptyList()
            )
            
            // Add to history
            conversationHistory[context.conversationId]?.add(aiMessage)
            
            Result.success(aiMessage)
        } catch (e: Exception) {
            // Fallback response
            val fallbackMessage = DialogueMessage(
                id = UUID.randomUUID().toString(),
                content = "让我想想...你能再说一遍吗？",
                isFromUser = false,
                emotion = "thoughtful"
            )
            Result.success(fallbackMessage)
        }
    }
    
    override suspend fun getConversationHistory(
        conversationId: String
    ): List<DialogueMessage> = withContext(Dispatchers.IO) {
        conversationHistory[conversationId] ?: emptyList()
    }
    
    override suspend fun clearConversation(conversationId: String) = withContext(Dispatchers.IO) {
        conversationHistory.remove(conversationId)
    }
}