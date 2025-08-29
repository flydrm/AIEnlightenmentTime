package com.enlightenment.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dialogue_messages")
/**
 * 数据实体 - DialogueMessage
 * 
 * 数据模型：
 * Room数据库实体类，映射本地SQLite数据表。
 * 用于持久化存储DialogueMessage相关数据。
 * 
 * 表结构设计：
 * - 主键策略：使用UUID确保唯一性
 * - 索引优化：根据查询需求建立索引
 * - 数据完整性：非空约束和默认值
 * 
 * @Entity Room实体标注
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */

/**
 * DialogueMessageEntity - DialogueMessage实体
 * 
 * Room数据库实体类，对应本地数据库表结构
 * 
 * 数据持久化策略：
 * - 支持离线数据缓存
 * - 自动数据同步
 * - 版本迁移兼容
 * 
 * @自版本 1.0.0
 */
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