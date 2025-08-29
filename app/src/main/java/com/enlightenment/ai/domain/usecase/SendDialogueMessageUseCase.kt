package com.enlightenment.ai.domain.usecase

import com.enlightenment.ai.domain.model.ConversationContext
import com.enlightenment.ai.domain.model.DialogueMessage
import com.enlightenment.ai.domain.repository.DialogueRepository
import com.enlightenment.ai.domain.repository.ProfileRepository
import javax.inject.Inject

/**
 * 发送对话消息用例
 * 
 * 功能说明：
 * 处理儿童与AI小熊猫的对话交互，生成智能回复。
 * 根据儿童年龄和对话上下文，提供个性化的教育性回答。
 * 
 * 业务逻辑：
 * 1. 获取儿童档案信息（年龄、兴趣等）
 * 2. 获取最近对话历史（保持上下文连贯）
 * 3. 构建对话上下文
 * 4. 调用AI生成回复
 * 
 * 设计特点：
 * - 上下文感知：基于历史对话保持连贯性
 * - 年龄适配：根据儿童年龄调整回复难度
 * - 教育导向：回复包含教育价值
 * 
 * 使用场景：
 * - 儿童提问问题
 * - 日常对话交流
 * - 学习互动问答
 * 
 * @property dialogueRepository 对话仓库，管理消息的发送和存储
 * @property profileRepository 档案仓库，获取儿童信息
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
/**
 * SendDialogueMessageUseCase - SendDialogueMessage用例
 * 
 * 业务用例类，封装特定的业务操作流程
 * 
 * 用例职责：
 * - 协调多个仓库的数据操作
 * - 实现复杂的业务规则
 * - 保证业务事务的一致性
 * 
 * 设计原则：
 * - 单一职责原则
 * - 与UI层解耦
 * - 可测试性设计
 * 
 * @since 1.0.0
 */
class SendDialogueMessageUseCase @Inject constructor(  // 依赖注入
    private val dialogueRepository: DialogueRepository,
    private val profileRepository: ProfileRepository
) {
    /**
     * 发送对话消息
     * 
     * 处理流程：
     * 1. 获取儿童档案（用于个性化回复）
     * 2. 获取对话历史（保持上下文）
     * 3. 构建包含最近5条消息的上下文
     * 4. 发送消息并获取AI回复
     * 
     * 上下文策略：
     * - 保留最近5条消息，平衡上下文和性能
     * - 包含儿童年龄，调整回复复杂度
     * - 维护会话ID，支持多轮对话
     * 
     * @param message 用户发送的消息内容
     * @param conversationId 会话ID，用于跟踪对话历史
     * @return AI生成的回复消息
     * 
     * 示例：
     * ```kotlin
     * val result = sendDialogueMessageUseCase(
     *     message = "小熊猫，天空为什么是蓝色的？",
     *     conversationId = currentConversationId
     * )
     * ```
     */
    suspend operator fun invoke(
        message: String,
        conversationId: String
    ): Result<DialogueMessage> {
        // 获取儿童档案，用于个性化回复
        val profile = profileRepository.getProfile()
        
        // 获取历史对话，保持上下文连贯
        val history = dialogueRepository.getConversationHistory(conversationId)
        
        // 构建对话上下文，包含必要信息
        val context = ConversationContext(
            conversationId = conversationId,
            childAge = profile?.age ?: 4,         // 默认4岁
            recentMessages = history.takeLast(5)   // 最近5条消息
        )
        
        // 发送消息并返回AI回复
        return dialogueRepository.sendMessage(message, context)
    }
}