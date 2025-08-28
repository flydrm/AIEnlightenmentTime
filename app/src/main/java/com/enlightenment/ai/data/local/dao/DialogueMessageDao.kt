package com.enlightenment.ai.data.local.dao

import androidx.room.*
import com.enlightenment.ai.data.local.entity.DialogueMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DialogueMessageDao {
    @Query("SELECT * FROM dialogue_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getConversationHistory(conversationId: String): List<DialogueMessageEntity>
    
    @Query("SELECT * FROM dialogue_messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 10")
    fun getRecentMessages(conversationId: String): Flow<List<DialogueMessageEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DialogueMessageEntity)
    
    @Query("DELETE FROM dialogue_messages WHERE conversationId = :conversationId")
    suspend fun clearConversation(conversationId: String)
    
    @Query("DELETE FROM dialogue_messages WHERE timestamp < :threshold")
    suspend fun deleteOldMessages(threshold: Long)
}