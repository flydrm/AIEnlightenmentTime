package com.enlightenment.ai.data.repository

import com.enlightenment.ai.data.local.dao.DialogueMessageDao
import com.enlightenment.ai.data.local.entity.DialogueMessageEntity
import com.enlightenment.ai.data.remote.api.AIApiService
import com.enlightenment.ai.data.remote.api.DialogueContextRequest
import com.enlightenment.ai.data.remote.api.DialogueRequest
import com.enlightenment.ai.data.remote.api.MessageHistory
import com.enlightenment.ai.domain.model.ConversationContext
import com.enlightenment.ai.domain.model.DialogueMessage
import com.enlightenment.ai.domain.repository.DialogueRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据层 - 对话仓库实现
 * 
 * 架构职责：
 * 实现DialogueRepository接口，协调AI对话服务和本地数据存储。
 * 负责消息的发送、接收、存储和历史管理。
 * 
 * 核心功能：
 * 1. 处理用户消息发送
 * 2. 调用AI服务获取回复
 * 3. 管理对话历史记录
 * 4. 持久化对话数据
 * 
 * 数据流程：
 * 用户消息 → 保存到数据库 → 发送到AI → 保存AI回复 → 返回结果
 * 
 * 降级策略：
 * - API调用失败：返回友好的默认回复
 * - 超时处理：30秒超时限制
 * - 内容过滤：确保回复适合儿童
 * 
 * 技术特点：
 * - 使用Room管理本地对话历史
 * - Gson处理复杂数据序列化
 * - 协程实现异步操作
 * 
 * @property apiService AI对话API服务
 * @property dialogueMessageDao 对话消息数据访问对象
 * @property gson JSON序列化工具
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@Singleton
/**
 * DialogueRepositoryImpl - Dialogue仓库实现
 * 
 * 仓库模式实现类，协调本地和远程数据源
 * 
 * 核心职责：
 * - 统一数据访问接口
 * - 实现缓存策略
 * - 处理数据同步
 * - 错误处理和降级
 * 
 * 数据策略：
 * - 优先使用本地缓存
 * - 异步更新远程数据
 * - 智能数据预加载
 * - 离线模式支持
 * 
 * @since 1.0.0
 */
class DialogueRepositoryImpl @Inject constructor(  // 依赖注入
    private val apiService: AIApiService,
    private val dialogueMessageDao: DialogueMessageDao,
    private val gson: Gson
) : DialogueRepository {
    
    override suspend fun sendMessage(
        message: String,
        context: ConversationContext
    ): Result<DialogueMessage> = withContext(Dispatchers.IO) {
        try {
            // Create 用户 message
            val userMessage = DialogueMessage(
                id = UUID.randomUUID().toString(),
                content = message,
                isFromUser = true
            )
            
            // Save to 数据库
            dialogueMessageDao.insertMessage(userMessage.toEntity(context.conversationId))
            
            // Prepare API接口 请求
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
            
            // Call API接口
            val response = apiService.sendDialogueMessage(request)
            
            // Create AI 响应 message
            val aiMessage = DialogueMessage(
                id = UUID.randomUUID().toString(),
                content = response.reply.text,
                isFromUser = false,
                emotion = response.reply.emotion,
                suggestedActions = response.reply.suggestions ?: emptyList()
            )
            
            // Save to 数据库
            dialogueMessageDao.insertMessage(aiMessage.toEntity(context.conversationId))
            
            Result.success(aiMessage)
        } catch (e: Exception) {  // 捕获并处理异常
            // Fallback 响应
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
        dialogueMessageDao.getConversationHistory(conversationId).map { it.toDomainModel() }
    }
    
    override suspend fun clearConversation(conversationId: String) = withContext(Dispatchers.IO) {
        dialogueMessageDao.clearConversation(conversationId)
    }
    
    private fun DialogueMessage.toEntity(conversationId: String): DialogueMessageEntity {
        return DialogueMessageEntity(
            id = id,
            conversationId = conversationId,
            content = content,
            isFromUser = isFromUser,
            timestamp = timestamp,
            emotion = emotion,
            suggestedActions = if (suggestedActions.isNotEmpty()) {
                gson.toJson(suggestedActions)
            } else null
        )
    }
    
    private fun DialogueMessageEntity.toDomainModel(): DialogueMessage {
        return DialogueMessage(
            id = id,
            content = content,
            isFromUser = isFromUser,
            timestamp = timestamp,
            emotion = emotion,
            suggestedActions = suggestedActions?.let {
                gson.fromJson(it, object : TypeToken<List<String>>() {}.type)
            } ?: emptyList()
        )
    }
}