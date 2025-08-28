package com.enlightenment.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dialogue_messages")
data class DialogueMessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val emotion: String?,
    val suggestedActions: String? // JSON array
)