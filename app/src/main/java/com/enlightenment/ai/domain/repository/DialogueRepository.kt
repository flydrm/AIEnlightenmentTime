package com.enlightenment.ai.domain.repository

import com.enlightenment.ai.domain.model.ConversationContext
import com.enlightenment.ai.domain.model.DialogueMessage

interface DialogueRepository {
    suspend fun sendMessage(
        message: String,
        context: ConversationContext
    ): Result<DialogueMessage>
    
    suspend fun getConversationHistory(
        conversationId: String
    ): List<DialogueMessage>
    
    suspend fun clearConversation(conversationId: String)
}