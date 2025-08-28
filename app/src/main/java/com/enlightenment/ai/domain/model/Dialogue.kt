package com.enlightenment.ai.domain.model

data class DialogueMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val emotion: String? = null,
    val suggestedActions: List<String> = emptyList()
)

data class ConversationContext(
    val conversationId: String,
    val childAge: Int,
    val recentMessages: List<DialogueMessage> = emptyList(),
    val currentTopic: String? = null,
    val mood: String? = null
)