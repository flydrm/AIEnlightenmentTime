package com.enlightenment.ai.domain.repository

import com.enlightenment.ai.domain.model.ConversationContext
import com.enlightenment.ai.domain.model.DialogueMessage

/**
 * 领域层 - 对话仓库接口
 * 
 * 职责说明：
 * 定义AI对话功能的数据操作契约。
 * 管理儿童与AI小熊猫之间的对话交互和历史记录。
 * 
 * 核心功能：
 * 1. 发送消息并获取AI回复
 * 2. 管理对话历史记录
 * 3. 清除对话内容
 * 
 * 设计原则：
 * - 上下文感知：支持多轮对话
 * - 历史追溯：保存完整对话记录
 * - 隐私保护：支持清除敏感对话
 * 
 * 实现要求：
 * - 必须处理AI服务异常
 * - 必须过滤不当内容
 * - 必须限制对话长度防止滥用
 * - 建议本地缓存对话历史
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
interface DialogueRepository {
    /**
     * 发送消息并获取AI回复
     * 
     * 功能说明：
     * 将用户消息发送给AI服务，并获取智能回复。
     * 回复会根据儿童年龄和上下文进行个性化调整。
     * 
     * 内容安全：
     * - 过滤用户输入的不当内容
     * - 确保AI回复适合儿童
     * - 检测并拦截敏感话题
     * 
     * @param message 用户发送的消息
     * @param context 对话上下文，包含历史消息和儿童信息
     * @return AI生成的回复消息，失败时返回错误
     */
    suspend fun sendMessage(
        message: String,
        context: ConversationContext
    ): Result<DialogueMessage>
    
    /**
     * 获取对话历史记录
     * 
     * 用于：
     * - 展示历史对话
     * - 构建对话上下文
     * - 学习记录分析
     * 
     * @param conversationId 会话唯一标识
     * @return 该会话的所有历史消息，按时间顺序排列
     */
    suspend fun getConversationHistory(
        conversationId: String
    ): List<DialogueMessage>
    
    /**
     * 清除对话记录
     * 
     * 使用场景：
     * - 用户主动清除
     * - 隐私保护需要
     * - 开始新的对话主题
     * 
     * @param conversationId 要清除的会话ID
     */
    suspend fun clearConversation(conversationId: String)
}