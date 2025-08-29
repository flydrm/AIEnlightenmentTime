package com.enlightenment.ai.presentation.dialogue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.domain.model.DialogueMessage
import com.enlightenment.ai.domain.usecase.SendDialogueMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * 智能对话ViewModel
 * 
 * 职责说明：
 * 管理AI小熊猫与儿童的对话交互，提供智能聊天功能。
 * 维护对话历史、处理消息发送、管理UI状态。
 * 
 * 核心功能：
 * 1. 对话消息管理（用户消息、AI回复）
 * 2. 智能建议回复
 * 3. 小熊猫情绪状态
 * 4. 对话历史维护
 * 
 * 交互特色：
 * - 友好问候：初始化时自动打招呼
 * - 快捷回复：提供建议选项，方便不会打字的儿童
 * - 情感反馈：小熊猫根据对话内容改变表情
 * - 教育引导：通过对话进行知识传递
 * 
 * 技术特点：
 * - 会话管理：每次进入生成新的会话ID
 * - 即时响应：用户消息立即显示
 * - 异步处理：AI回复不阻塞UI
 * 
 * @property sendDialogueMessageUseCase 发送消息用例，处理AI对话
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@HiltViewModel
/**
 * DialogueViewModel - Dialogue视图模型
 * 
 * 功能职责：
 * - 管理Dialogue界面的业务逻辑
 * - 处理用户交互事件和状态更新
 * - 协调数据层和展示层的通信
 * 
 * 状态管理：
 * - 使用StateFlow管理UI状态
 * - 支持配置变更后的状态保持
 * - 提供状态更新的原子性保证
 * 
 * 生命周期：
 * - 自动处理协程作用域
 * - 支持数据预加载
 * - 优雅的资源清理
 * 
 * @since 1.0.0
 */
class DialogueViewModel @Inject constructor(  // 依赖注入
    private val sendDialogueMessageUseCase: SendDialogueMessageUseCase
) : ViewModel() {
    
    private val conversationId = UUID.randomUUID().toString()
    
    private val _uiState = MutableStateFlow(DialogueUiState())
    val uiState: StateFlow<DialogueUiState> = _uiState.asStateFlow()
    
    init {
        // 初始化时发送欢迎消息，让小熊猫主动打招呼
        addAIMessage(
            content = "嗨！我是小熊猫！今天想聊什么呢？",
            emotion = "happy",
            suggestions = listOf("讲个故事", "教我数数", "唱首歌")
        )
    }
    
    /**
     * 发送用户消息
     * 
     * 处理流程：
     * 1. 验证消息非空
     * 2. 立即添加用户消息到界面
     * 3. 显示加载状态
     * 4. 异步调用AI服务获取回复
     * 5. 展示AI回复或错误提示
     * 
     * 用户体验优化：
     * - 即时显示用户消息，提供反馈
     * - 加载时清空建议回复，避免误点
     * - 失败时提供友好提示，不显示技术错误
     * 
     * @param message 用户输入的消息内容
     */
    fun sendMessage(message: String) {
        // 空消息不处理
        if (message.isBlank()) return
        
        // 立即显示用户消息，提供即时反馈
        addUserMessage(message)
        
        // 设置加载状态，清空建议回复
        _uiState.update { it.copy(isLoading = true, suggestedReplies = emptyList()) }  // 更新UI状态
        
        // 异步发送消息并处理回复
        viewModelScope.launch {  // 启动协程执行异步操作
            sendDialogueMessageUseCase(message, conversationId)
                .onSuccess { aiResponse ->
                    // 成功：添加AI回复
                    addAIMessage(
                        content = aiResponse.content,
                        emotion = aiResponse.emotion,
                        suggestions = aiResponse.suggestedActions
                    )
                }
                .onFailure {
                    // 失败：显示友好的错误提示
                    addAIMessage(
                        content = "哎呀，我没听清楚，能再说一遍吗？",
                        emotion = "confused"
                    )
                }
            
            // 无论成功失败，都要关闭加载状态
            _uiState.update { it.copy(isLoading = false) }  // 更新UI状态
        }
    }
    
    private fun addUserMessage(content: String) {
        val message = MessageDisplayModel(
            id = UUID.randomUUID().toString(),
            content = content,
            isFromUser = true,
            timestamp = System.currentTimeMillis()
        )
        
        _uiState.update { currentState ->  // 更新UI状态
            currentState.copy(
                messages = currentState.messages + message
            )
        }
    }
    
    private fun addAIMessage(
        content: String,
        emotion: String? = null,
        suggestions: List<String> = emptyList()
    ) {
        val message = MessageDisplayModel(
            id = UUID.randomUUID().toString(),
            content = content,
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            emotion = emotion
        )
        
        _uiState.update { currentState ->  // 更新UI状态
            currentState.copy(
                messages = currentState.messages + message,
                suggestedReplies = suggestions
            )
        }
    }
}
/**
 * DialogueUiState
 * 
 * 功能说明：
 * 提供DialogueUiState相关的功能实现。
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
 * DialogueUiState - 对话UI状态
 * 
 * 功能描述：
 * - 提供核心业务功能处理功能
 * - 支持灵活配置、易于扩展、高性能
 * 
 * 设计说明：
 * - 采用数据类设计
 * - 遵循项目统一的架构规范
 * 
 * @since 1.0.0
 */
data class DialogueUiState(
    val messages: List<MessageDisplayModel> = emptyList(),
    val isLoading: Boolean = false,
    val suggestedReplies: List<String> = emptyList()
)
/**
 * MessageDisplayModel
 * 
 * 功能说明：
 * 提供MessageDisplayModel相关的功能实现。
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
 * MessageDisplayModel - MessageDisplayModel
 * 
 * 功能描述：
 * - 提供核心业务功能处理功能
 * - 支持灵活配置、易于扩展、高性能
 * 
 * 设计说明：
 * - 采用数据类设计
 * - 遵循项目统一的架构规范
 * 
 * @since 1.0.0
 */
data class MessageDisplayModel(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val emotion: String? = null
)