package com.enlightenment.ai.domain.model

/**
 * DialogueMessage - DialogueMessage
 * 
 * 领域数据模型，表示业务概念和规则
 * 
 * 模型特点：
 * - 不可变数据设计
 * - 业务逻辑封装
 * - 领域规则验证
 * 
 * @自版本 1.0.0
 */
data class DialogueMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val emotion: String? = null,
    val suggestedActions: List<String> = emptyList()
)
/**
 * ConversationContext
 * 
 * 功能说明：
 * 提供ConversationContext相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */


/**
 * ConversationContext - ConversationContext
 * 
 * 领域数据模型，表示业务概念和规则
 * 
 * 模型特点：
 * - 不可变数据设计
 * - 业务逻辑封装
 * - 领域规则验证
 * 
 * @自版本 1.0.0
 */
data class ConversationContext(
    val conversationId: String,
    val childAge: Int,
    val recentMessages: List<DialogueMessage> = emptyList(),
    val currentTopic: String? = null,
    val mood: String? = null
)