package com.enlightenment.ai.data.local.dao

import androidx.room.*
import com.enlightenment.ai.data.local.entity.DialogueMessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * 数据访问对象 - 对话消息表
 * 
 * 职责说明：
 * 定义对话消息数据的本地数据库操作接口。
 * 管理儿童与AI的对话历史记录。
 * 
 * 核心功能：
 * 1. 消息的增删改查
 * 2. 会话历史管理
 * 3. 数据清理操作
 * 
 * 数据策略：
 * - 按会话ID组织消息
 * - 支持批量操作
 * - 定期清理过期数据
 * 
 * @Dao Room数据访问对象标注
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@Dao
/**
 * DialogueMessageDao - DialogueMessageDao
 * 
 * Room数据访问对象，提供类型安全的数据库操作接口
 * 
 * 支持的操作：
 * - 增删改查基础操作
 * - 复杂查询和关联查询
 * - 事务支持和批量操作
 * 
 * @since 1.0.0
 */
interface DialogueMessageDao {
    @Query("SELECT * FROM dialogue_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")  // 查询数据库
    suspend fun getConversationHistory(conversationId: String): List<DialogueMessageEntity>
    
    @Query("SELECT * FROM dialogue_messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 10")  // 查询数据库
    fun getRecentMessages(conversationId: String): Flow<List<DialogueMessageEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DialogueMessageEntity)
    
    @Query("DELETE FROM dialogue_messages WHERE conversationId = :conversationId")  // 删除数据
    suspend fun clearConversation(conversationId: String)
    
    @Query("DELETE FROM dialogue_messages WHERE timestamp < :threshold")  // 删除数据
    suspend fun deleteOldMessages(threshold: Long)
}